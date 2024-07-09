package net.thewinnt.cutscenes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CutsceneOverlayManager {
    private static List<Overlay> overlays = new ArrayList<>();
    private static List<Object> configs = new ArrayList<>();

    public static void addOverlay(Overlay overlay, Object config) {
        overlays.add(overlay);
        configs.add(config);
    }

    public static void addAtIndex(Overlay overlay, Object config, int index) {
        overlays.add(index, overlay);
        configs.add(index, config);
    }

    /** Returns an immutable view of all active overlays */
    public static List<Overlay> getOverlays() {
        return Collections.unmodifiableList(overlays);
    }

    public static void removeOverlay(Overlay overlay, Object config) {
        overlays.remove(overlay);
        configs.remove(config);
    }

    public static void clearOverlays() {
        overlays.clear();
        configs.clear();
    }

    public static void render(Minecraft minecraft, GuiGraphics graphics, int width, int height) {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).render(minecraft, graphics, width, height, configs.get(i));
        }
    }
}
