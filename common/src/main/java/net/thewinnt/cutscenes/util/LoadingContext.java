package net.thewinnt.cutscenes.util;

import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.easing.Easing;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all the necessary data for loading a cutscene.
 */
public final class LoadingContext {
    public final @Nullable LoadResolver<Easing> easings;
    private int dataVersion;
    private ResourceLocation currentCutscene;

    public LoadingContext(@Nullable LoadResolver<Easing> easings) {
        this.easings = easings;
    }

    public ResourceLocation getCurrentCutscene() {
        return currentCutscene;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public int getDataVersion() {
        return dataVersion;
    }
}
