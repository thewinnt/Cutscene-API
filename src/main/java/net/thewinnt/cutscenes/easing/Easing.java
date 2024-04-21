package net.thewinnt.cutscenes.easing;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.easing.serializers.SimpleEasingSerializer;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;

import java.util.Optional;

public interface Easing {
    Codec<Easing> NO_LEGACY_CODEC = CutsceneAPI.EASING_SERIALIZERS.byNameCodec().dispatch(Easing::getSerializer, EasingSerializer::codec);
    Codec<Either<String, Easing>> LEGACY_OR_NEW_CODEC = Codec.either(
        Codec.STRING,
        NO_LEGACY_CODEC
    );
    /**
     * Returns the eased value from given t
     * @param t the initial progress (linear)
     * @return the eased value
     */
    double get(double t);

    EasingSerializer<?> getSerializer();
}
