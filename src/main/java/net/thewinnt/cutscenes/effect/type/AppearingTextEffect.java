package net.thewinnt.cutscenes.effect.type;

import net.minecraft.client.multiplayer.ClientLevel;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.client.overlay.AppearingTextOverlay;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.serializer.AppearingTextSerializer;
import net.thewinnt.cutscenes.util.TimeProvider;

public class AppearingTextEffect extends CutsceneEffect<AppearingTextConfiguration> {
    private AppearingTextOverlay overlay;
    private TimeProvider time;

    public AppearingTextEffect(double startTime, double endTime, AppearingTextConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    public void onStart(CutsceneType cutscene) {
        this.overlay = new AppearingTextOverlay(this.config);
        this.time = new TimeProvider(this.endTime - this.startTime);
        CutsceneOverlayManager.addAtIndex(this.overlay, this.time, 0);
    }

    @Override
    public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {
        this.time.setTime(time);
    }

    @Override
    public void onEnd(CutsceneType cutscene) {
        CutsceneOverlayManager.removeOverlay(this.overlay, this.time);
        this.overlay = null;
    }

    @Override
    public CutsceneEffectSerializer<AppearingTextConfiguration> getSerializer() {
        return AppearingTextSerializer.INSTANCE;
    }

}
