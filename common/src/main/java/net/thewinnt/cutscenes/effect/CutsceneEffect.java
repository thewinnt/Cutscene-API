package net.thewinnt.cutscenes.effect;

import com.google.gson.JsonObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneType;

/**
 * A CutsceneEffect does all the visuals not related to the camera during a cutscene. It can be an overlay,
 * or an operation in the world, or something else entirely.
 * @param <T> The class for this effect's configuration
 */
public abstract class CutsceneEffect<T> {
    public final double startTime;
    public final double endTime;
    protected final T config;

    /**
     * Constructs a new {@link CutsceneEffect}.
     * @param startTime the starting time, in ticks since the cutscene started
     * @param endTime the ending time, in ticks since the cutscene started
     * @param config a config object that a superclass uses to display the effect
     */
    public CutsceneEffect(double startTime, double endTime, T config) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.config = config;
    }

    /**
     * Called before the first tick of the effect
     * @param cutscene the cutscene type that this effect is used from
     */
    public abstract void onStart(ClientLevel level, CutsceneType cutscene);

    /**
     * Called every frame that the cutscene is rendered
     * @param time the time in ticks since the effect started, in range [0; endTime - startTime]
     * @param level the client level that the effect is running in
     * @param cutscene the cutscene type that this effect is used from
     */
    public abstract void onFrame(double time, ClientLevel level, CutsceneType cutscene);

    /**
     * Called after the last tick of the effect
     * @param cutscene the cutscene type that this effect is used from
     */
    public abstract void onEnd(ClientLevel level, CutsceneType cutscene);

    public abstract CutsceneEffectSerializer<T> getSerializer();

    public final void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS.getId(this.getSerializer()));
        buf.writeDouble(startTime);
        buf.writeDouble(endTime);
        this.getSerializer().toNetwork(config, buf);
    }

    public static CutsceneEffect<?> fromNetwork(FriendlyByteBuf buf) {
        CutsceneEffectSerializer<?> serializer = CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS.byId(buf.readInt());
        double start = buf.readDouble();
        double end = buf.readDouble();
        return serializer.factory().unchecked(start, end, serializer.fromNetwork(buf));
    }

    public static CutsceneEffect<?> fromJSON(JsonObject json) {
        ResourceLocation type = ResourceLocation.parse(GsonHelper.getAsString(json, "type"));
        CutsceneEffectSerializer<?> serializer = CutsceneAPI.CUTSCENE_EFFECT_SERIALIZERS.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown cutscene type effect: " + type);
        }
        double start = GsonHelper.getAsDouble(json, "start");
        double end = GsonHelper.getAsDouble(json, "end");
        return serializer.factory().unchecked(start, end, serializer.fromJSON(json));
    }
}
