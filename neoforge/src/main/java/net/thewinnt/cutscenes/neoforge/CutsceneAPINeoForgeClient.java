package net.thewinnt.cutscenes.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;
import net.thewinnt.cutscenes.CutsceneAPI;

public class CutsceneAPINeoForgeClient {
    public static final NeoForgeClientPlatform CLIENT_PLATFORM = new NeoForgeClientPlatform();

    public static void init() {
        CutsceneAPI.onInitializeClient(CLIENT_PLATFORM);
    }
}
