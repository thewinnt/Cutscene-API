package net.thewinnt.cutscenes.easing.impl;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.thewinnt.cutscenes.easing.types.IndependentCoordinateEasing;

public class CoordinateSupplierImpl implements IndependentCoordinateEasing.CoordinateSupplier {
    private final Window window = Minecraft.getInstance().getWindow();

    @Override
    public int getWidth() {
        return window.getGuiScaledWidth();
    }

    @Override
    public int getHeight() {
        return window.getGuiScaledHeight();
    }
}
