package net.thewinnt.cutscenes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public interface Overlay {
    void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config);
}
