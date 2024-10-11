package net.thewinnt.cutscenes.neoforge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.platform.AbstractPacket;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;
import net.thewinnt.cutscenes.platform.PacketType;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class NeoForgePlatform implements PlatformAbstractions {
    private final List<PreparableReloadListener> reloadListeners = new ArrayList<>();
    protected final List<Consumer<CameraAngleSetter>> angleSetters = new ArrayList<>();
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> commandMakers = new ArrayList<>();
    public final List<Runnable> onLogout = new ArrayList<>();
    protected final List<Runnable> clientTick = new ArrayList<>();
    public List<PacketType<?>> packets = new ArrayList<>();

    @Override
    public void registerReloadListener(PreparableReloadListener listener, ResourceLocation id) {
        reloadListeners.add(listener);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        NeoForgePlatform platform = CutsceneAPINeoForge.PLATFORM;
        platform.reloadListeners.forEach(event::addListener);
    }

    @Override
    public <T extends AbstractPacket> void registerClientboundPacket(CustomPacketPayload.Type<T> type, AbstractPacket.PacketReader<T> reader, Consumer<T> handler) {
        if (packets == null) {
            throw new IllegalStateException("Too late! Clientbound packets should be registered during mod initialization");
        }
        packets.add(new PacketType<>(type, reader, handler));
    }

    @Override
    public void sendPacketToPlayer(AbstractPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public float getPartialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
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
        return CutsceneAPIEntities.WAYPOINT.value();
    }

    public static <T extends AbstractPacket> IPayloadHandler<T> createHandler(PacketType<T> type) {
        return (payload, context) -> context.enqueueWork(() -> type.handler().accept(payload));
    }

    public static <T extends AbstractPacket> void registerPacket(PayloadRegistrar registrar, PacketType<T> type) {
        registrar.playToClient(type.type(), type.codec(), createHandler(type));
    }

    // --- EVENT LISTENERS ---

    /** Sends the cutscene registry to client */
    @SubscribeEvent
    public static void sendRegistry(OnDatapackSyncEvent event) {
        if (event != null && event.getPlayer() != null) {
            PacketDistributor.sendToPlayer(event.getPlayer(), new UpdateCutscenesPacket(CutsceneManager.REGISTRY));
        } else {
            PacketDistributor.sendToAllPlayers(new UpdateCutscenesPacket(CutsceneManager.REGISTRY));
        }
    }

    @SubscribeEvent
    public static void sendPreviewToNewPlayers(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && CutsceneManager.getPreviewedCutscene() != null && CutsceneManager.previewOffset != null) {
            PacketDistributor.sendToPlayer(player, new PreviewCutscenePacket(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()), CutsceneManager.previewOffset, CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll));
        }
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CutsceneAPINeoForge.PLATFORM.commandMakers.forEach(consumer -> consumer.accept(event.getDispatcher()));
    }
}
