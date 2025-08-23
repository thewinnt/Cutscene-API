package net.thewinnt.cutscenes.effect;

import net.thewinnt.cutscenes.client.overlay.SimpleTextOverlay;
import net.thewinnt.cutscenes.effect.configuration.SimpleTextConfiguration;
import net.thewinnt.cutscenes.effect.serializer.SimpleTextSerializer;
import net.thewinnt.cutscenes.effect.type.AbstractOverlayEffect;

public class SimpleTextEffect extends AbstractOverlayEffect<SimpleTextConfiguration, SimpleTextOverlay> {
    public SimpleTextEffect(double startTime, double endTime, SimpleTextConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    protected SimpleTextOverlay createOverlay(SimpleTextConfiguration config) {
        return new SimpleTextOverlay(config);
    }

    @Override
    public CutsceneEffectSerializer<SimpleTextConfiguration> getSerializer() {
        return SimpleTextSerializer.INSTANCE;
    }
}
