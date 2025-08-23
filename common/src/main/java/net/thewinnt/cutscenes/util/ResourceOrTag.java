package net.thewinnt.cutscenes.util;

import net.minecraft.resources.ResourceLocation;

public record ResourceOrTag(ResourceLocation id, boolean isTag) {
    public static ResourceOrTag parse(String string) {
        if (string == null || string.isEmpty()) return null;
        if (string.startsWith("#")) {
            return new ResourceOrTag(ResourceLocation.parse(string.substring(1)), true);
        } else {
            return new ResourceOrTag(ResourceLocation.parse(string), false);
        }
    }
}
