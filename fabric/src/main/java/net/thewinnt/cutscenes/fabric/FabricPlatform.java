package net.thewinnt.cutscenes.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.fabric.util.duck.MinecraftExt;
import net.thewinnt.cutscenes.platform.AbstractPacket;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricPlatform implements PlatformAbstractions {
    public Map<ResourceLocation, Pair<AbstractPacket.PacketReader, BiConsumer<AbstractPacket, FriendlyByteBuf>>> packets = new HashMap<>();
    public final List<Consumer<CameraAngleSetter>> angleSetters = new ArrayList<>();
    public final List<Runnable> onLogout = new ArrayList<>();
    private final List<Runnable> clientTick = new ArrayList<>();
    public MinecraftServer server;

    @Override
    public void registerReloadListener(PreparableReloadListener listener, ResourceLocation id) {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                return listener.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
            }
        });
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
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ServerPlayNetworking.send(player, packet.id(), buf);
    }

    @Override
    public float getPartialTick() {
        return ((MinecraftExt) Minecraft.getInstance()).csapi$getPartialTick();
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public void submitCameraAngleModifier(Consumer<CameraAngleSetter> modifier) {
        angleSetters.add(modifier);
    }

    @Override
    public void submitOnLogout(Runnable runnable) {
        onLogout.add(runnable);
    }

    @Override
    public void submitOnClientTick(Runnable runnable) {
        ClientTickEvents.START_CLIENT_TICK.register(client -> runnable.run());
    }

    @Override
    public void submitOnRegisterCommand(Consumer<CommandDispatcher<CommandSourceStack>> command) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> command.accept(dispatcher));
    }

    @Override
    public EntityType<WaypointEntity> getWaypointEntityType() {
        return CutsceneAPIFabric.WAYPOINT;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }
}
