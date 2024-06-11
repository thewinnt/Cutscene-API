package net.thewinnt.cutscenes.effect.type;

import net.thewinnt.cutscenes.client.overlay.RectangleOverlay;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.RectangleConfiguration;
import net.thewinnt.cutscenes.effect.serializer.RectangleSerializer;

public class RectangleEffect extends AbstractOverlayEffect<RectangleConfiguration, RectangleOverlay> {
    public RectangleEffect(double startTime, double endTime, RectangleConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    protected RectangleOverlay createOverlay(RectangleConfiguration config) {
        return new RectangleOverlay(config);
    }

    @Override
    public CutsceneEffectSerializer<RectangleConfiguration> getSerializer() {
        return RectangleSerializer.INSTANCE;
    }
}
