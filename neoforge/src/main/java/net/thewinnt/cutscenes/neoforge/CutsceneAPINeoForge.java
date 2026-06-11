package net.thewinnt.cutscenes.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.event.CutsceneEvents;
import net.thewinnt.cutscenes.neoforge.event.CutsceneOverEvent;
import net.thewinnt.cutscenes.platform.Services;

@Mod("cutscene_api")
public final class CutsceneAPINeoForge {
    public static final NeoForgePlatform PLATFORM = ((NeoForgePlatform) Services.PLATFORM);

    public CutsceneAPINeoForge(IEventBus bus) {
        // Run our common setup.
        CutsceneAPI.onInitialize();
        CutsceneAPIEntities.REGISTRY.register(bus);
        CutsceneAPIArgumentTypes.REGISTRY.register(bus);
        CutsceneEvents.CUTSCENE_OVER_SERVER.addListener((type, id, player, reason) -> {
            NeoForge.EVENT_BUS.post(new CutsceneOverEvent.Server(player, type, id, reason));
        });
    }
}
