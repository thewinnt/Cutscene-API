package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.easing.Easing;
import net.thewinnt.cutscenes.easing.types.ConstantEasing;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.AppearingTextConfiguration;
import net.thewinnt.cutscenes.effect.type.AppearingTextEffect;

import java.util.ArrayList;
import java.util.List;

public class AppearingTextSerializer implements CutsceneEffectSerializer<AppearingTextConfiguration> {
    public static final AppearingTextSerializer INSTANCE = new AppearingTextSerializer();

    private AppearingTextSerializer() {}

    @Override
    public AppearingTextConfiguration fromNetwork(FriendlyByteBuf buf) {
        Component text = buf.readComponent();
        Easing rx = Easing.fromNetwork(buf);
        Easing ry = Easing.fromNetwork(buf);
        Easing lineWidth = Easing.fromNetwork(buf);
        return new AppearingTextConfiguration(text, rx, ry, lineWidth);
    }

    @Override
    public AppearingTextConfiguration fromJSON(JsonObject json) {
        Component text = Component.Serializer.fromJson(json.get("text"));
        Easing rx = Easing.fromJSON(json.get("x"));
        Easing ry = Easing.fromJSON(json.get("y"));
        Easing lineWidth = Easing.fromJSON(json.get("line_width"), ConstantEasing.ONE);
        return new AppearingTextConfiguration(text, rx, ry, lineWidth);
    }

    @Override
    public void toNetwork(AppearingTextConfiguration config, FriendlyByteBuf buf) {
        buf.writeComponent(config.text());
        Easing.toNetwork(config.rx(), buf);
        Easing.toNetwork(config.ry(), buf);
        Easing.toNetwork(config.width(), buf);
    }

    @Override
    public CutsceneEffectFactory<AppearingTextConfiguration> factory() {
        return AppearingTextEffect::new;
    }
}
