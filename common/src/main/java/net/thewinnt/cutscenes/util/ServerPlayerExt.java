package net.thewinnt.cutscenes.util;

import net.thewinnt.cutscenes.CutsceneType;
import org.jetbrains.annotations.Nullable;

/**
 * A duck interface for some functions added to {@link net.minecraft.server.level.ServerPlayer}
 */
public interface ServerPlayerExt {
    /**
     * @return the remaining ticks for the cutscene the player's watching, or 0 if they aren't
     */
    int csapi$getCutsceneTicks();

    /**
     * @return the cutscene the player's watching, or {@code null} if they aren't
     */
    @Nullable
    CutsceneType csapi$getRunningCutscene();

    /**
     * Sets the remaining amount of ticks that the player will be watching a cutscene for
     * @param value the amount of cutscene ticks remaining
     */
    void csapi$setCutsceneTicks(int value);

    /**
     * Sets a cutscene that the player is watching. <b>Does not affect the client in any way and doesn't start
     * a cutscene there.</b>
     * @param type the cutscene to start watching
     */
    void csapi$setRunningCutscene(CutsceneType type);
}
