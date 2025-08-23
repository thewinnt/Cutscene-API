package net.thewinnt.cutscenes.neoforge;

import net.neoforged.neoforge.common.NeoForge;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.neoforge.event.CutsceneOverEvent;

public class CutsceneAPINeoForgeClient {
    public static final NeoForgeClientPlatform CLIENT_PLATFORM = new NeoForgeClientPlatform();

    public static void init() {
        CutsceneAPI.onInitializeClient(CLIENT_PLATFORM);
        CutsceneEvents.CUTSCENE_OVER_CLIENT.addListener((type, id, player, reason) -> {
            NeoForge.EVENT_BUS.post(new CutsceneOverEvent.Client(player, type, id, reason));
        });
    }
}
