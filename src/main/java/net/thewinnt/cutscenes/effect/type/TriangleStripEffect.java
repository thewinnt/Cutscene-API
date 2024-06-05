package net.thewinnt.cutscenes.effect.type;

import net.minecraft.client.multiplayer.ClientLevel;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.CutsceneOverlayManager;
import net.thewinnt.cutscenes.client.overlay.TriangleStripOverlay;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.effect.serializer.TriangleStripSerializer;
import net.thewinnt.cutscenes.util.TimeProvider;

public class TriangleStripEffect extends CutsceneEffect<TriangleStripConfiguration> {
    private TriangleStripOverlay overlay;
    private TimeProvider time;

    public TriangleStripEffect(double startTime, double endTime, TriangleStripConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    public void onStart(CutsceneType cutscene) {
        this.overlay = new TriangleStripOverlay(config);
        this.time = new TimeProvider(this.endTime - this.startTime);
        CutsceneOverlayManager.addOverlay(overlay, time);
    }

    @Override
    public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {
        this.time.setTime(time);
    }

    @Override
    public void onEnd(CutsceneType cutscene) {
        CutsceneOverlayManager.removeOverlay(overlay, time);
    }

    @Override
    public CutsceneEffectSerializer<TriangleStripConfiguration> getSerializer() {
        return TriangleStripSerializer.INSTANCE;
    }
}
