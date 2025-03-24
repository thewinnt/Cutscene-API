package net.thewinnt.cutscenes.effect;

import net.thewinnt.cutscenes.client.overlay.TextureAnimationOverlay;
import net.thewinnt.cutscenes.effect.configuration.TextureAnimationConfiguration;
import net.thewinnt.cutscenes.effect.serializer.TextureAnimationSerializer;
import net.thewinnt.cutscenes.effect.type.AbstractOverlayEffect;

public class TextureAnimationEffect extends AbstractOverlayEffect<TextureAnimationConfiguration, TextureAnimationOverlay> {
    public TextureAnimationEffect(double startTime, double endTime, TextureAnimationConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    protected TextureAnimationOverlay createOverlay(TextureAnimationConfiguration config) {
        return new TextureAnimationOverlay(config);
    }

    @Override
    public CutsceneEffectSerializer<TextureAnimationConfiguration> getSerializer() {
        return TextureAnimationSerializer.INSTANCE;
    }
}
