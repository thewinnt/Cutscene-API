package net.thewinnt.cutscenes.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.easing.EasingSerializer;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.chardelays.DelayProviderSerializer;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.platform.AbstractPacket;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NeoForgePlatform implements PlatformAbstractions {
    private List<PreparableReloadListener> reloadListeners = new ArrayList<>();
    protected final List<Consumer<CameraAngleSetter>> angleSetters = new ArrayList<>();
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> commandMakers = new ArrayList<>();
    private final List<Runnable> onLogout = new ArrayList<>();
    protected final List<Runnable> clientTick = new ArrayList<>();
    public Map<ResourceLocation, Pair<AbstractPacket.PacketReader, BiConsumer<AbstractPacket, FriendlyByteBuf>>> packets = new HashMap<>();

    @Override
    public void registerReloadListener(PreparableReloadListener listener, ResourceLocation id) {
        if (this.reloadListeners == null) {
            throw new IllegalStateException("Too late! The registration of reload listeners has ended.");
        }
        reloadListeners.add(listener);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        NeoForgePlatform platform = CutsceneAPINeoForge.PLATFORM;
        platform.reloadListeners.forEach(event::addListener);
    }

    @Override
    public void registerClientboundPacket(ResourceLocation id, AbstractPacket.PacketReader reader, BiConsumer<AbstractPacket, FriendlyByteBuf> writer) {
        if (packets == null) {
            throw new IllegalStateException("Too late! Clientbound packets should be registered during mod initialization");
        }
        packets.put(id, new Pair<>(reader, writer));
    }

    @Override
    public void sendPacketToPlayer(AbstractPacket packet, ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(packet);
    }

    @Override
    public float getPartialTick() {
        return Minecraft.getInstance().getPartialTick();
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

    // --- EVENT LISTENERS ---

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CutsceneAPINeoForge.PLATFORM.onLogout.forEach(Runnable::run);
    }

    /** Sends the cutscene registry to client */
    @SubscribeEvent
    public static void sendRegistry(OnDatapackSyncEvent event) {
        if (event != null && event.getPlayer() != null) {
            PacketDistributor.PLAYER.with(event.getPlayer()).send(new UpdateCutscenesPacket(CutsceneManager.REGISTRY));
        } else {
            PacketDistributor.ALL.noArg().send(new UpdateCutscenesPacket(CutsceneManager.REGISTRY));
        }
    }

    @SubscribeEvent
    public static void sendPreviewToNewPlayers(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && CutsceneManager.getPreviewedCutscene() != null && CutsceneManager.previewOffset != null) {
            PacketDistributor.PLAYER.with(player).send(new PreviewCutscenePacket(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()), CutsceneManager.previewOffset, CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll));
        }
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CutsceneAPINeoForge.PLATFORM.commandMakers.forEach(consumer -> consumer.accept(event.getDispatcher()));
    }
}
