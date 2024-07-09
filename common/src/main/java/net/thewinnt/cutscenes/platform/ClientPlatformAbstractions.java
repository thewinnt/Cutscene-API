package net.thewinnt.cutscenes.platform;

import net.minecraft.client.renderer.RenderType;

public interface ClientPlatformAbstractions extends PlatformAbstractions {
    RenderType getTriangleStrip();
}
