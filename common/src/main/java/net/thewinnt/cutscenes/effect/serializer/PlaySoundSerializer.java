package net.thewinnt.cutscenes.effect.serializer;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.PlaySoundConfiguration;
import net.thewinnt.cutscenes.effect.type.PlaySoundEffect;
import net.thewinnt.cutscenes.util.JsonHelper;
import net.thewinnt.cutscenes.util.LoadingContext;

import java.util.Locale;
import java.util.Optional;

public class PlaySoundSerializer implements CutsceneEffectSerializer<PlaySoundConfiguration> {
    public static final PlaySoundSerializer INSTANCE = new PlaySoundSerializer();

    private PlaySoundSerializer() {}

    @Override
    public PlaySoundConfiguration fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation sound = buf.readResourceLocation();
        SoundSource source = buf.readEnum(SoundSource.class);
        float volume = buf.readFloat();
        float pitch = buf.readFloat();
        Optional<Vec3> pos = buf.readOptional(object -> object.readVec3());
        return new PlaySoundConfiguration(sound, source, volume, pitch, pos);
    }

    @Override
    public PlaySoundConfiguration fromJSON(JsonObject json, LoadingContext context) {
        ResourceLocation sound = ResourceLocation.parse(context.wrapLoading("sound", () -> GsonHelper.getAsString(json, "sound"), "loading_error"));
        SoundSource source = SoundSource.valueOf(GsonHelper.getAsString(json, "source", "master").toUpperCase(Locale.ROOT));
        float volume = GsonHelper.getAsFloat(json, "volume", 1);
        float pitch = GsonHelper.getAsFloat(json, "pitch", 1);
        Optional<Vec3> pos = Optional.ofNullable(JsonHelper.vec3FromJson(json, "pos"));
        return new PlaySoundConfiguration(sound, source, volume, pitch, pos);
    }

    @Override
    public void toNetwork(PlaySoundConfiguration data, FriendlyByteBuf buf) {
        buf.writeResourceLocation(data.event());
        buf.writeEnum(data.source());
        buf.writeFloat(data.volume());
        buf.writeFloat(data.pitch());
        buf.writeOptional(data.pos(), (object, object2) -> object.writeVec3(object2));
    }

    @Override
    public CutsceneEffectFactory<PlaySoundConfiguration> factory() {
        return PlaySoundEffect::new;
    }
}
