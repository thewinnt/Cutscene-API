package net.thewinnt.cutscenes.rotation;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.rotation.handler.AddToCutsceneRotation;
import net.thewinnt.cutscenes.rotation.handler.CutsceneRotation;
import net.thewinnt.cutscenes.rotation.handler.EaseBackRotation;
import net.thewinnt.cutscenes.rotation.handler.PlayerRotation;
import net.thewinnt.cutscenes.rotation.serializer.EaseBackSerializer;
import net.thewinnt.cutscenes.rotation.serializer.SimpleRotationHandlerSerializer;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.HashMap;
import java.util.Map;

public interface RotationSerializer<T extends RotationHandler> {
    Map<String, RotationHandler> SIMPLE_HANDLERS = new HashMap<>();

    RotationSerializer<CutsceneRotation> CUTSCENE = registerSimple(CutsceneRotation.INSTANCE, Identifier.parse("cutscenes:cutscene"));
    RotationSerializer<PlayerRotation> PLAYER = registerSimple(PlayerRotation.INSTANCE, Identifier.parse("cutscenes:player"));
    RotationSerializer<AddToCutsceneRotation> ADD = registerSimple(AddToCutsceneRotation.INSTANCE, Identifier.parse("cutscenes:add"));
    RotationSerializer<EaseBackRotation> EASE_BACK = register(EaseBackSerializer.INSTANCE, Identifier.parse("cutscenes:ease_back"));

    void toNetwork(FriendlyByteBuf buf, T handler);
    T fromNetwork(FriendlyByteBuf buf);
    T fromJson(JsonObject json, LoadingContext context);

    static <T extends RotationHandler> RotationSerializer<T> registerSimple(T singleton, Identifier id) {
        SimpleRotationHandlerSerializer<T> serializer = new SimpleRotationHandlerSerializer<>(singleton);
        SIMPLE_HANDLERS.put(id.getPath(), singleton);
        return Registry.register(CutsceneAPI.ROTATION_HANDLERS, id, serializer);
    }

    static <T extends RotationSerializer<?>> T register(T serializer, Identifier id) {
        return Registry.register(CutsceneAPI.ROTATION_HANDLERS, id, serializer);
    }

    static void init() {}
}
