package net.thewinnt.cutscenes.platform;

import java.util.Collection;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.thewinnt.cutscenes.entity.WaypointEntity;

/**
 * Abstracts away the platform-specific APIs found in Minecraft. Both Fabric and NeoForge have their own implementations
 * of this interface, with their respective ways of doing things.
 * <p>
 * I'm doing this instead of using Architectury API, because
 * a) I don't want this mod to have many dependencies, and
 * b) Architectury API doesn't implement everything I need anyway
 */
public interface PlatformAbstractions {
    // reload listeners
    void registerReloadListener(PreparableReloadListener listener, ResourceLocation id);

    // networking
    <T extends AbstractPacket> void registerClientboundPacket(CustomPacketPayload.Type<T> type, AbstractPacket.PacketReader<T> reader, Consumer<T> handler);
    void sendPacketToPlayer(AbstractPacket packet, ServerPlayer player);
    default void sendPacketToPlayers(AbstractPacket packet, Collection<ServerPlayer> players) {
        players.forEach(player -> sendPacketToPlayer(packet, player));
    }

    // utilities
    float getPartialTick();
    MinecraftServer getServer();

    // events
    void submitCameraAngleModifier(Consumer<CameraAngleSetter> modifier);
    void submitOnLogout(Runnable runnable);
    void submitOnClientTick(Runnable runnable);
    void submitOnRegisterCommand(Consumer<CommandDispatcher<CommandSourceStack>> command);

    EntityType<WaypointEntity> getWaypointEntityType();
}
