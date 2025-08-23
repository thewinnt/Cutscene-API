package net.thewinnt.cutscenes.time;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public record CutsceneLength(double length, TimeManager manager) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(length);
        buf.writeUtf(manager.type());
    }

    public static CutsceneLength fromNetwork(FriendlyByteBuf buf) {
        double length = buf.readDouble();
        TimeManager manager = TimeManager.REGISTRY.get(buf.readUtf()).get();
        return new CutsceneLength(length, manager);
    }

    public static CutsceneLength fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new CutsceneLength(json.getAsInt(), new GameTickManager());
        }
        JsonObject obj = json.getAsJsonObject();
        String type = GsonHelper.getAsString(obj, "type", "ticks");
        TimeManager manager = TimeManager.REGISTRY.get(type).get();
        if (manager == null) {
            LOGGER.warn("Unknown time manager type: {}", type);
            manager = new GameTickManager();
        }
        double length = GsonHelper.getAsDouble(obj, "length");
        return new CutsceneLength(length, manager);
    }
}
