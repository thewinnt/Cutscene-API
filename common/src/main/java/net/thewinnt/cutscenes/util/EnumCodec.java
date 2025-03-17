package net.thewinnt.cutscenes.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnumCodec<T extends Enum<T>> implements Codec<T> {
    private final Map<String, T> constants = new HashMap<>();

    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        DataResult<String> result = ops.getStringValue(input);
        if (result.isSuccess()) {
            String name = result.getOrThrow().toLowerCase(Locale.ROOT);
            if (constants.containsKey(name)) {
                return DataResult.success(Pair.of(constants.get(name), input));
            } else {
                return DataResult.error(() -> "Unknown enum constant: " + name);
            }
        } else {
            return DataResult.error(() -> "Not a string: " + input);
        }
    }

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        return ops.mergeToPrimitive(prefix, ops.createString(input.name().toLowerCase(Locale.ROOT)));
    }

    public static <T extends Enum<T>> EnumCodec<T> forClass(Class<T> cls) {
        EnumCodec<T> output = new EnumCodec<>();
        T[] constants = cls.getEnumConstants();
        for (T i : constants) {
            output.constants.put(i.name().toLowerCase(Locale.ROOT), i);
        }
        return output;
    }
}
