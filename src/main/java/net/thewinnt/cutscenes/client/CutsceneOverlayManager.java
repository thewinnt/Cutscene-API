package net.thewinnt.cutscenes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CutsceneOverlayManager {
    private static Overlay currentOverlay;
    private static Object overlayConfig;

    public static void setCurrentOverlay(Overlay currentOverlay) {
        CutsceneOverlayManager.currentOverlay = currentOverlay;
    }

    public static void setOverlayConfig(Object overlayConfig) {
        CutsceneOverlayManager.overlayConfig = overlayConfig;
    }

    public static Overlay getCurrentOverlay() {
        return currentOverlay;
    }

    public static Object getOverlayConfig() {
        return overlayConfig;
    }

    public static void render(Minecraft minecraft, GuiGraphics graphics, int width, int height) {
        currentOverlay.render(minecraft, graphics, width, height, overlayConfig);
    }
}
