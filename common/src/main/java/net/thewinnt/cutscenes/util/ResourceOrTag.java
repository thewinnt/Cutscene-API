package net.thewinnt.cutscenes.util;

import net.minecraft.resources.Identifier;

public record ResourceOrTag(Identifier id, boolean isTag) {
    public static ResourceOrTag parse(String string) {
        if (string == null || string.isEmpty()) return null;
        if (string.startsWith("#")) {
            return new ResourceOrTag(Identifier.parse(string.substring(1)), true);
        } else {
            return new ResourceOrTag(Identifier.parse(string), false);
        }
    }
}
