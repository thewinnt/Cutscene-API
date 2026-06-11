package net.thewinnt.cutscenes.neoforge.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.event.EndingReason;

/**
 * Fired when a cutscene ends on either side.
 * <p>
 * Fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 */
public abstract class CutsceneOverEvent extends CutsceneEvent {
    private final EndingReason reason;

    public CutsceneOverEvent(Player player, CutsceneType type, Identifier id, EndingReason reason) {
        super(player, type, id);
        this.reason = reason;
    }

    public EndingReason getReason() {
        return reason;
    }

    /**
     * {@link CutsceneOverEvent.Server} is fired when a cutscene ends for a player
     * on the logical server side.
     */
    public static class Server extends CutsceneOverEvent {
        public Server(ServerPlayer player, CutsceneType type, Identifier id, EndingReason reason) {
            super(player, type, id, reason);
        }

        public ServerPlayer getPlayer() {
            return (ServerPlayer) getEntity();
        }
    }

    /**
     * {@link CutsceneOverEvent.Client} is fired when a cutscene ends for a player
     * on the physical client side.
     */
    public static class Client extends CutsceneOverEvent {
        public Client(Player player, CutsceneType type, Identifier id, EndingReason reason) {
            super(player, type, id, reason);
        }
    }
}
