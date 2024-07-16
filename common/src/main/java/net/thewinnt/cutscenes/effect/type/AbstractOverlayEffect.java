package net.thewinnt.cutscenes.effect.type;

import net.minecraft.client.multiplayer.ClientLevel;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.util.TimeProvider;

/**
 * A base class for effects using overlays
 * @param <C> the configuration type
 * @param <O> the overlay type
 */
public abstract class AbstractOverlayEffect<C, O extends Overlay> extends CutsceneEffect<C> {
    protected O overlay;
    protected TimeProvider time;

    public AbstractOverlayEffect(double startTime, double endTime, C config) {
        super(startTime, endTime, config);
    }

    protected abstract O createOverlay(C config);

    @Override
    public void onStart(ClientLevel level, CutsceneType cutscene) {
        this.overlay = createOverlay(config);
        this.time = new TimeProvider(this.endTime - this.startTime);
        CutsceneOverlayManager.addOverlay(overlay, time);
    }

    @Override
    public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {
        this.time.setTime(time);
    }

    @Override
    public void onEnd(ClientLevel level, CutsceneType cutscene) {
        CutsceneOverlayManager.removeOverlay(overlay, time);
        this.overlay = null;
        this.time = null;
    }
}
