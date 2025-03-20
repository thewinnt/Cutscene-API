package net.thewinnt.cutscenes.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.thewinnt.cutscenes.CutsceneType;

public class ServerEffectWrapper<T> extends CutsceneEffect<T> {
    public final CutsceneEffectSerializer<T> type;

    public ServerEffectWrapper(double startTime, double endTime, T config, CutsceneEffectSerializer<T> type) {
        super(startTime, endTime, config);
        this.type = type;
    }
    
    @Override public void onStart(ClientLevel level, CutsceneType cutscene) {}
    @Override public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {}
    @Override public void onEnd(ClientLevel level, CutsceneType cutscene) {}

    @Override
    public CutsceneEffectSerializer<T> getSerializer() {
        return type;
    }

    
}
