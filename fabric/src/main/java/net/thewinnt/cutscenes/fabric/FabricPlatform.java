package net.thewinnt.cutscenes.fabric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.platform.AbstractClientboundPacket;
import net.thewinnt.cutscenes.platform.AbstractPacket;
import net.thewinnt.cutscenes.platform.AbstractServerboundPacket;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;
import net.thewinnt.cutscenes.platform.PacketType;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;

public class FabricPlatform implements PlatformAbstractions {
    public Map<ResourceLocation, PacketType<? extends AbstractClientboundPacket>> clientboundPackets = new HashMap<>();
    public Map<ResourceLocation, PacketType<? extends AbstractServerboundPacket>> serverboundPackets = new HashMap<>();
    public final List<Consumer<CameraAngleSetter>> angleSetters = new ArrayList<>();
    public final List<Runnable> onLogout = new ArrayList<>();
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
    @SuppressWarnings("unchecked")
    public <T> T register(ResourceKey<T> id, T element) {
        return Registry.register((Registry<T>)BuiltInRegistries.REGISTRY.get(id.registry()), id, element);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void registerRegistry(ResourceKey<Registry<T>> key, Consumer<MappedRegistry<T>> setter) {
        MappedRegistry<T> output = new MappedRegistry<>(key, Lifecycle.stable());
        ((WritableRegistry)BuiltInRegistries.REGISTRY).register(key, output, Lifecycle.stable());
        setter.accept(output);
    }

    @Override
    public <T extends AbstractClientboundPacket> void registerClientboundPacket(Class<T> type, AbstractPacket.PacketReader<T> reader, ResourceLocation id) {
        if (clientboundPackets == null) {
            throw new IllegalStateException("Too late! Clientbound packets should be registered during mod initialization");
        }
        clientboundPackets.put(id, new PacketType<>(type, reader));
    }

    @Override
    public void sendPacketToPlayer(AbstractClientboundPacket packet, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ServerPlayNetworking.send(player, packet.id(), buf);
    }

    @Override
    public <T extends AbstractServerboundPacket> void registerServerboundPacket(Class<T> type, AbstractPacket.PacketReader<T> reader, ResourceLocation id) {
        if (serverboundPackets == null) {
            throw new IllegalStateException("Too late! Serverbound packets should be registered during mod initialization");
        }
        serverboundPackets.put(id, new PacketType<>(type, reader));
    }

    @Override
    public void sendPacketFromPlayer(AbstractServerboundPacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(packet.id(), buf);
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
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
}
