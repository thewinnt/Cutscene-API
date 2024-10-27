package net.thewinnt.cutscenes.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.thewinnt.cutscenes.CutsceneType;

public class CutsceneEvents {
    /** Called whenever a cutscene is over for the client player. */
    public static final Event<CutsceneOverClient> CUTSCENE_OVER_CLIENT = new Event<>();

    /** Called every time a cutscene ends for a player on the server. */
    public static final Event<CutsceneOverServer> CUTSCENE_OVER_SERVER = new Event<>();

    @FunctionalInterface
    public interface CutsceneOverClient {
        void accept(CutsceneType type, ResourceLocation id, LocalPlayer player, EndingReason reason);
    }

    @FunctionalInterface
    public interface CutsceneOverServer {
        void accept(CutsceneType type, ResourceLocation id, ServerPlayer player, EndingReason reason);
    }
}
