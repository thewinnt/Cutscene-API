package net.thewinnt.cutscenes.effect.type;

import net.thewinnt.cutscenes.client.overlay.TriangleStripOverlay;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.effect.serializer.TriangleStripSerializer;

public class TriangleStripEffect extends AbstractOverlayEffect<TriangleStripConfiguration, TriangleStripOverlay> {
    public TriangleStripEffect(double startTime, double endTime, TriangleStripConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    protected TriangleStripOverlay createOverlay(TriangleStripConfiguration config) {
        return new TriangleStripOverlay(config);
    }

    @Override
    public CutsceneEffectSerializer<TriangleStripConfiguration> getSerializer() {
        return TriangleStripSerializer.INSTANCE;
    }
}
