package net.thewinnt.cutscenes.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.thewinnt.cutscenes.CutsceneAPI;

@Mod("cutscene_api")
public final class CutsceneAPINeoForge {
    public static final NeoForgePlatform PLATFORM = new NeoForgePlatform();

    public CutsceneAPINeoForge(IEventBus bus, Dist dist) {
        // Run our common setup.
        CutsceneAPI.onInitialize(PLATFORM);
        CutsceneAPIEntities.REGISTRY.register(bus);
    }
}
