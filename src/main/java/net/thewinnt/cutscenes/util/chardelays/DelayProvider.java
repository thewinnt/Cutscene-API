package net.thewinnt.cutscenes.util.chardelays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DelayProvider {
    /** Returns the symbol that is needed for this provider to be used. */
    int activationCodepoint();

    /** Returns the delay in ticks for the given character. It will be applied right afterward. */
    double delay(int codepoint);

    /** Returns the default character delay, in ticks */
    double defaultDelay(int codepoint);

    /**
     * Serializes this provider for network.
     * @deprecated use {@link DelayProvider#toNetwork(DelayProvider, FriendlyByteBuf)} instead
     */
    @Deprecated
    void toNetwork(FriendlyByteBuf buf);

    DelayProviderSerializer<?> getSerializer();

    static void toNetwork(DelayProvider delayProvider, FriendlyByteBuf buf) {
        buf.writeInt(CutsceneAPI.DELAY_PROVIDERS.getId(delayProvider.getSerializer()));
        delayProvider.toNetwork(buf);
    }

    static DelayProvider fromJSON(@Nonnull JsonElement json) {
        if (json.isJsonPrimitive()) {
            return fromJSONPrimitive(json.getAsJsonPrimitive());
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            DelayProviderSerializer<?> serializer = CutsceneAPI.DELAY_PROVIDERS.get(new ResourceLocation(obj.get("type").getAsString()));
            return serializer.fromJSON(obj);
        }
        throw new IllegalArgumentException("Cannot get DelayProvider from JSON: " + json);
    }

    static DelayProvider fromJSON(@Nullable JsonElement json, DelayProvider fallback) {
        if (json == null) {
            return fallback;
        }
        try {
            return fromJSON(json);
        } catch (RuntimeException e) {
            CutsceneAPI.LOGGER.error("Exception loading delay provider, returning fallback: ", e);
            return fallback;
        }
    }

    static DelayProvider fromJSONPrimitive(JsonPrimitive json) {
        String value = json.getAsString();
        ResourceLocation test = new ResourceLocation(value);
        if (DelayProviderSerializer.SINGLETONS.containsKey(test)) {
            return DelayProviderSerializer.SINGLETONS.get(test);
        }
        throw new IllegalArgumentException("Unknown singleton delay provider: " + test);
    }

    static DelayProvider fromNetwork(FriendlyByteBuf buf) {
        return CutsceneAPI.DELAY_PROVIDERS.byId(buf.readInt()).fromNetwork(buf);
    }
}
