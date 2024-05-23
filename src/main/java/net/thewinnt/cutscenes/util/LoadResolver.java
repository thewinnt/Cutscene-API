package net.thewinnt.cutscenes.util;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.CutsceneAPI;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/** Loads some things that may refer to others of their kind. */
public class LoadResolver<T> {
    public static final Logger LOGGER = LogUtils.getLogger();
    private final BiFunction<JsonElement, LoadResolver<T>, T> reader;
    private final Map<ResourceLocation, JsonElement> saveData;
    private final Map<ResourceLocation, T> resolved = new HashMap<>();
    private final Set<ResourceLocation> resolvingNow = new HashSet<>();
    private final boolean allowExceptions;

    /**
     * @param reader a function that loads an object from JSON
     * @param saveData the saved items
     * @param allowExceptions if true, will skip items with errors.
     *                        Otherwise, will throw the exception that occured when loading.
     */
    public LoadResolver(BiFunction<JsonElement, LoadResolver<T>, T> reader, Map<ResourceLocation, JsonElement> saveData, boolean allowExceptions) {
        this.reader = reader;
        this.saveData = saveData;
        this.allowExceptions = allowExceptions;
    }

    /**
     * Resolves a single object from its ID, also resolving and objects it depends on.
     * If an exception occurs when loading, returns {@code null} if {@code allowExceptions} is {@code true},
     * and throws the exception otherwise.
     * @throws LoopingReferenceException in case of an infinite loop
     */
    public @Nullable T resolve(ResourceLocation id) {
        if (resolved.containsKey(id)) {
            return resolved.get(id);
        } else {
            if (resolvingNow.contains(id)) throw new LoopingReferenceException(id);
            resolvingNow.add(id);
            T object;
            try {
                object = reader.apply(saveData.get(id), this);
            } catch (Exception e) {
                if (e instanceof LoopingReferenceException) throw e;
                if (allowExceptions) {
                    LOGGER.error("Couldn't load object {}: ", id, e);
                    return null;
                }
                throw e;
            }
            resolved.put(id, object);
            resolvingNow.remove(id);
            return object;
        }
    }

    /**
     * Loads all the objects from the save, resolving each object's dependencies.
     * @return a map of ids to objects
     * @throws LoopingReferenceException in case of an infinite loop
     */
    public Map<ResourceLocation, T> load() {
        for (ResourceLocation i : saveData.keySet()) {
            if (!resolved.containsKey(i)) {
                resolve(i);
            }
        }
        return resolved;
    }

    /**
     * An Exception representing a loop in loading objects.
     * <p>
     * For example, an object named {@code cutscenes:a} references an object named {@code cutscenes:b}.
     * That object, in return, referenced {@code cutscenes:a}. When trying to load either of these objects,
     * this exception will be thrown.
     */
    public static class LoopingReferenceException extends RuntimeException {
        private final ResourceLocation cause;

        public LoopingReferenceException(ResourceLocation id) {
            this.cause = id;
        }

        @Override
        public String getMessage() {
            return "Exception loading object " + cause;
        }
    }
}
