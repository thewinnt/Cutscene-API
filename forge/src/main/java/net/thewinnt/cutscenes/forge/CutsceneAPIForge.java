package net.thewinnt.cutscenes.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.thewinnt.cutscenes.CutsceneAPI;

@Mod("cutscene_api")
public final class CutsceneAPIForge {
    public static final ForgePlatform PLATFORM = new ForgePlatform();

    public CutsceneAPIForge() {
        // Run our common setup.
        CutsceneAPI.onInitialize(PLATFORM);
        CutsceneAPIEntities.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
