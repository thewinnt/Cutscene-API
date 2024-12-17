package net.thewinnt.cutscenes.forge;

import net.thewinnt.cutscenes.CutsceneAPI;

public class CutsceneAPIForgeClient {
    public static final ForgeClientPlatform CLIENT_PLATFORM = new ForgeClientPlatform();

    public static void init() {
        CutsceneAPI.onInitializeClient(CLIENT_PLATFORM);
    }
}
