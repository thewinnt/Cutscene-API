package net.thewinnt.cutscenes.effect.type;

import net.thewinnt.cutscenes.client.overlay.AppearingTextOverlay;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.serializer.AppearingTextSerializer;

public class AppearingTextEffect extends AbstractOverlayEffect<AppearingTextConfiguration, AppearingTextOverlay> {
    public AppearingTextEffect(double startTime, double endTime, AppearingTextConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    protected AppearingTextOverlay createOverlay(AppearingTextConfiguration config) {
        return new AppearingTextOverlay(config);
    }

    @Override
    public CutsceneEffectSerializer<AppearingTextConfiguration> getSerializer() {
        return AppearingTextSerializer.INSTANCE;
    }

}
