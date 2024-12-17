package net.thewinnt.cutscenes.forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.platform.AbstractClientboundPacket;
import net.thewinnt.cutscenes.platform.AbstractPacket;
import net.thewinnt.cutscenes.platform.AbstractServerboundPacket;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;
import net.thewinnt.cutscenes.platform.PacketType;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgePlatform implements PlatformAbstractions {
    public static final String NETWORK_VERSION = "1.6";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("cutscenes", "network"),
        () -> NETWORK_VERSION,
        NETWORK_VERSION::equals,
        NETWORK_VERSION::equals
    );
    private static int idCounter;
    private final List<PreparableReloadListener> reloadListeners = new ArrayList<>();
    protected final List<Consumer<CameraAngleSetter>> angleSetters = new ArrayList<>();
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> commandMakers = new ArrayList<>();
    public final List<Runnable> onLogout = new ArrayList<>();
    public final Map<ResourceKey<?>, Object> registryObjects = new HashMap<>();
    public final Map<ResourceKey<?>, Consumer<?>> registries = new HashMap<>();
    protected final List<Runnable> clientTick = new ArrayList<>();
    public List<PacketType<? extends AbstractClientboundPacket>> clientboundPackets = new ArrayList<>();
    public List<PacketType<? extends AbstractServerboundPacket>> serverboundPackets = new ArrayList<>();

    @Override
    public void registerReloadListener(PreparableReloadListener listener, ResourceLocation id) {
        reloadListeners.add(listener);
    }

    @Override
    public <T> T register(ResourceKey<T> id, T element) {
        this.registryObjects.put(id, element);
        return element;
    }

    @Override
    public <T> void registerRegistry(ResourceKey<Registry<T>> key, Consumer<MappedRegistry<T>> setter) {
        this.registries.put(key, setter);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        ForgePlatform platform = CutsceneAPIForge.PLATFORM;
        platform.reloadListeners.forEach(event::addListener);
    }

    @Override
    public <T extends AbstractClientboundPacket> void registerClientboundPacket(Class<T> type, AbstractPacket.PacketReader<T> reader, ResourceLocation id) {
        if (clientboundPackets == null) {
            throw new IllegalStateException("Too late! Clientbound packets should be registered during mod initialization");
        }
        CHANNEL.registerMessage(idCounter++, type, AbstractPacket::write, reader::read, (t, contextSupplier) -> {
            contextSupplier.get().enqueueWork(t::execute);
            contextSupplier.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    @Override
    public void sendPacketToPlayer(AbstractClientboundPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public <T extends AbstractServerboundPacket> void registerServerboundPacket(Class<T> type, AbstractPacket.PacketReader<T> reader, ResourceLocation id) {
        if (serverboundPackets == null) {
            throw new IllegalStateException("Too late! Serverbound packets should be registered during mod initialization");
        }
        CHANNEL.registerMessage(idCounter++, type, AbstractPacket::write, reader::read, (t, contextSupplier) -> {
            contextSupplier.get().enqueueWork(() -> t.execute(contextSupplier.get().getSender()));
            contextSupplier.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    @Override
    public void sendPacketFromPlayer(AbstractServerboundPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void submitCameraAngleModifier(Consumer<CameraAngleSetter> modifier) {
        this.angleSetters.add(modifier);
    }

    @Override
    public void submitOnLogout(Runnable runnable) {
        onLogout.add(runnable);
    }

    @Override
    public void submitOnClientTick(Runnable runnable) {
        clientTick.add(runnable);
    }

    @Override
    public void submitOnRegisterCommand(Consumer<CommandDispatcher<CommandSourceStack>> command) {
        commandMakers.add(command);
    }

    @Override
    public EntityType<WaypointEntity> getWaypointEntityType() {
        return CutsceneAPIEntities.WAYPOINT.get();
    }

    // --- EVENT LISTENERS ---

    /** Sends the cutscene registry to client */
    @SubscribeEvent
    public static void sendRegistry(OnDatapackSyncEvent event) {
        if (event != null && event.getPlayer() != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), new UpdateCutscenesPacket(CutsceneManager.REGISTRY));
        } else {
            CHANNEL.send(PacketDistributor.ALL.noArg(), new UpdateCutscenesPacket(CutsceneManager.REGISTRY));
        }
    }

    @SubscribeEvent
    public static void sendPreviewToNewPlayers(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && CutsceneManager.getPreviewedCutscene() != null && CutsceneManager.previewOffset != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PreviewCutscenePacket(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()), CutsceneManager.previewOffset, CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll));
        }
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CutsceneAPIForge.PLATFORM.commandMakers.forEach(consumer -> consumer.accept(event.getDispatcher()));
    }

    // TODO networking (see 1.20.1-forge branch!)
    // TODO start porting natives
    // TODO start porting main mod
}
