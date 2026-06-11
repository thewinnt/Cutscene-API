package net.thewinnt.cutscenes.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.thewinnt.cutscenes.entity.WaypointEntity;
import net.thewinnt.cutscenes.platform.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FabricPlatform implements PlatformAbstractions {
    public List<PacketType<? extends AbstractClientboundPacket>> clientboundPackets = new ArrayList<>();
    public List<PacketType<? extends AbstractServerboundPacket>> serverboundPackets = new ArrayList<>();
    public final List<Consumer<CameraAngleSetter>> angleSetters = new ArrayList<>();
    public final List<Runnable> onLogout = new ArrayList<>();
    public MinecraftServer server;

    @Override
    public void registerReloadListener(PreparableReloadListener listener, Identifier id) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, listener);
    }

    @Override
    public void addListenerOrdering(Identifier first, Identifier second) {
        ResourceLoader.get(PackType.SERVER_DATA).addListenerOrdering(first, second);
    }

    @Override
    public <T extends AbstractClientboundPacket> void registerClientboundPacket(CustomPacketPayload.Type<T> type, AbstractPacket.PacketReader<T> reader) {
        if (clientboundPackets == null) {
            throw new IllegalStateException("Too late! Clientbound packets should be registered during mod initialization");
        }
        clientboundPackets.add(new PacketType<>(type, reader));
    }

    @Override
    public void sendPacketToPlayer(AbstractClientboundPacket packet, ServerPlayer player) {
        FriendlyByteBuf buf = FriendlyByteBufs.create();
        packet.write(buf);
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public <T extends AbstractServerboundPacket> void registerServerboundPacket(CustomPacketPayload.Type<T> type, AbstractPacket.PacketReader<T> reader) {
        if (serverboundPackets == null) {
            throw new IllegalStateException("Too late! Serverbound packets should be registered during mod initialization");
        }
        serverboundPackets.add(new PacketType<>(type, reader));
    }

    @Override
    public void sendPacketFromPlayer(AbstractServerboundPacket packet) {
        FriendlyByteBuf buf = FriendlyByteBufs.create();
        packet.write(buf);
        ClientPlayNetworking.send(packet);
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

    public static <T extends AbstractClientboundPacket> void registerClientboundPacket(PacketType<T> type) {
        PayloadTypeRegistry.clientboundPlay().register(type.type(), type.codec());
    }

    public static <T extends AbstractServerboundPacket> void registerServerboundPacket(PacketType<T> type) {
        PayloadTypeRegistry.serverboundPlay().register(type.type(), type.codec());
    }
}
