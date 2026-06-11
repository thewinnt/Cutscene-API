package net.thewinnt.cutscenes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface Overlay {
    void render(Minecraft minecraft, GuiGraphicsExtractor graphics, int width, int height, Object config);
}
