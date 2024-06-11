package net.thewinnt.cutscenes.effect.type;

import net.thewinnt.cutscenes.client.overlay.BlitOverlay;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.BlitConfiguration;
import net.thewinnt.cutscenes.effect.serializer.BlitSerializer;

public class BlitEffect extends AbstractOverlayEffect<BlitConfiguration, BlitOverlay> {
    public BlitEffect(double startTime, double endTime, BlitConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    public CutsceneEffectSerializer<BlitConfiguration> getSerializer() {
        return BlitSerializer.INSTANCE;
    }

    @Override
    protected BlitOverlay createOverlay(BlitConfiguration config) {
        return new BlitOverlay(config);
    }
}
