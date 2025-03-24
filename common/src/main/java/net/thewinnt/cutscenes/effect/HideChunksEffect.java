package net.thewinnt.cutscenes.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.thewinnt.cutscenes.CutsceneType;

public class HideChunksEffect extends CutsceneEffect<Void> {
    private static boolean ACTIVE;

    public HideChunksEffect(double startTime, double endTime, Void config) {
        super(startTime, endTime, config);
    }

    @Override
    public void onStart(ClientLevel level, CutsceneType cutscene) {
        ACTIVE = true;
    }

    @Override
    public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {

    }

    @Override
    public void onEnd(ClientLevel level, CutsceneType cutscene) {
        ACTIVE = false;
    }

    public static boolean active() {
        return ACTIVE;
    }

    @Override
    public CutsceneEffectSerializer<Void> getSerializer() {
        return CutsceneEffectSerializer.HIDE_CHUNKS;
    }
}
