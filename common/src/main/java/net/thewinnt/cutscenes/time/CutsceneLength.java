package net.thewinnt.cutscenes.time;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.CutsceneAPI;
import org.slf4j.Logger;

public record CutsceneLength(int length, TimeManager manager) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(length);
        buf.writeUtf(manager.type());
    }

    public static CutsceneLength fromNetwork(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
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
        int length = GsonHelper.getAsInt(obj, "length");
        return new CutsceneLength(length, manager);
    }
}
