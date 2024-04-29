package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect;

public class AppearingTextSerializer implements CutsceneEffectSerializer<AppearingTextConfiguration> {
    public static final AppearingTextSerializer INSTANCE = new AppearingTextSerializer();

    private AppearingTextSerializer() {}

    @Override
    public AppearingTextConfiguration fromNetwork(FriendlyByteBuf buf) {
        Component text = buf.readComponent();
        Easing rx = Easing.fromNetwork(buf);
        Easing ry = Easing.fromNetwork(buf);
        return new AppearingTextConfiguration(text, rx, ry);
    }

    @Override
    public AppearingTextConfiguration fromJSON(JsonObject json) {
        Component text = Component.Serializer.fromJson(json.get("text"));
        Easing rx = Easing.fromJSON(json.get("x"));
        Easing ry = Easing.fromJSON(json.get("y"));
        return new AppearingTextConfiguration(text, rx, ry);
    }

    @Override
    public void toNetwork(AppearingTextConfiguration config, FriendlyByteBuf buf) {
        buf.writeComponent(config.text());
        Easing.toNetwork(config.rx(), buf);
        Easing.toNetwork(config.ry(), buf);
    }

    @Override
    public CutsceneEffectFactory<AppearingTextConfiguration> factory() {
        return AppearingTextEffect::new;
    }
}
