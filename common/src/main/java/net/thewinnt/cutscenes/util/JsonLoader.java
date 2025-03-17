package net.thewinnt.cutscenes.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonLoader extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Gson gson;
    protected final String folder;

    public JsonLoader(Gson gson, String folder) {
        this.gson = gson;
        this.folder = folder;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> map = new HashMap<>();
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(this.folder);

        for(Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            ResourceLocation elementId = fileToIdConverter.fileToId(resourceId);

            try (Reader reader = (entry.getValue()).openAsReader()) {
                map.putIfAbsent(elementId, JsonParser.parseReader(reader));
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                LOGGER.error("Couldn't parse data file '{}' from '{}'", elementId, resourceId, exception);
            }
        }
        return map;
    }
}