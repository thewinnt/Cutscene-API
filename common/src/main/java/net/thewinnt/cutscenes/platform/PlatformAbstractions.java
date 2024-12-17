package net.thewinnt.cutscenes.platform;

import java.util.Collection;
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
import net.thewinnt.cutscenes.entity.WaypointEntity;

/**
 * Abstracts away the platform-specific APIs found in Minecraft. Both Fabric and NeoForge have their own implementations
 * of this interface, with their respective ways of doing things.
 * <p>
 * I'm doing this instead of using Architectury API, because
 * a) I don't want this mod to have many dependencies, and
 * b) Architectury API doesn't implement everything I need anyway
 * <p>
 * <b>Warning:</b> an instance of this may not exist during mod initialization if your mod loads before
 * Cutscene API.
 */
public interface PlatformAbstractions {
    // reload listeners
    void registerReloadListener(PreparableReloadListener listener, ResourceLocation id);

    // registration
    <T> T register(ResourceKey<T> id, T element);
    <T> void registerRegistry(ResourceKey<Registry<T>> key, Consumer<MappedRegistry<T>> setter);

    // networking
    <T extends AbstractClientboundPacket> void registerClientboundPacket(Class<T> type, AbstractPacket.PacketReader<T> reader, ResourceLocation id);
    void sendPacketToPlayer(AbstractClientboundPacket packet, ServerPlayer player);
    <T extends AbstractServerboundPacket> void registerServerboundPacket(Class<T> type, AbstractPacket.PacketReader<T> reader, ResourceLocation id);
    void sendPacketFromPlayer(AbstractServerboundPacket packet);
    default void sendPacketToPlayers(AbstractClientboundPacket packet, Collection<ServerPlayer> players) {
        players.forEach(player -> sendPacketToPlayer(packet, player));
    }

    // utilities
    MinecraftServer getServer();

    // events
    void submitCameraAngleModifier(Consumer<CameraAngleSetter> modifier);
    void submitOnLogout(Runnable runnable);
    void submitOnClientTick(Runnable runnable);
    void submitOnRegisterCommand(Consumer<CommandDispatcher<CommandSourceStack>> command);

    // user-facing
    EntityType<WaypointEntity> getWaypointEntityType();
}
