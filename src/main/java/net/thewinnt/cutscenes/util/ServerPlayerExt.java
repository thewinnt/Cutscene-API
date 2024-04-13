package net.thewinnt.cutscenes.util;

import net.thewinnt.cutscenes.CutsceneType;

import javax.annotation.Nullable;

public interface ServerPlayerExt {
    int csapi$getCutsceneTicks();
    @Nullable CutsceneType csapi$getRunningCutscene();
    void csapi$setCutsceneTicks(int value);
    void csapi$setRunningCutscene(CutsceneType type);
}
