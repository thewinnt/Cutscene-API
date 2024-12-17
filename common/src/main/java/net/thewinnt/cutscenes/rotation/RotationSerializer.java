package net.thewinnt.cutscenes.rotation;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.rotation.handler.AddToCutsceneRotation;
import net.thewinnt.cutscenes.rotation.handler.CutsceneRotation;
import net.thewinnt.cutscenes.rotation.handler.EaseBackRotation;
import net.thewinnt.cutscenes.rotation.handler.PlayerRotation;
import net.thewinnt.cutscenes.rotation.serializer.EaseBackSerializer;
import net.thewinnt.cutscenes.rotation.serializer.SimpleRotationHandlerSerializer;

import java.util.HashMap;
import java.util.Map;

public interface RotationSerializer<T extends RotationHandler> {
    Map<String, RotationHandler> SIMPLE_HANDLERS = new HashMap<>();

    RotationSerializer<CutsceneRotation> CUTSCENE = registerSimple(CutsceneRotation.INSTANCE, new ResourceLocation("cutscenes:cutscene"));
    RotationSerializer<PlayerRotation> PLAYER = registerSimple(PlayerRotation.INSTANCE, new ResourceLocation("cutscenes:player"));
    RotationSerializer<AddToCutsceneRotation> ADD = registerSimple(AddToCutsceneRotation.INSTANCE, new ResourceLocation("cutscenes:add"));
    RotationSerializer<EaseBackRotation> EASE_BACK = register(EaseBackSerializer.INSTANCE, new ResourceLocation("cutscenes:ease_back"));

    void toNetwork(FriendlyByteBuf buf, T handler);
    T fromNetwork(FriendlyByteBuf buf);
    T fromJson(JsonObject json);

    static <T extends RotationHandler> RotationSerializer<T> registerSimple(T singleton, ResourceLocation id) {
        SimpleRotationHandlerSerializer<T> serializer = new SimpleRotationHandlerSerializer<>(singleton);
        SIMPLE_HANDLERS.put(id.getPath(), singleton);
        return Registry.register(CutsceneAPI.ROTATION_HANDLERS, id, serializer);
    }

    static <T extends RotationSerializer<?>> T register(T serializer, ResourceLocation id) {
        return Registry.register(CutsceneAPI.ROTATION_HANDLERS, id, serializer);
    }

    static void init() {}
}
