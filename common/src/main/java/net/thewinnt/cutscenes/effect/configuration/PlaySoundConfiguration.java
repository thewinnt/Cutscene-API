package net.thewinnt.cutscenes.effect.configuration;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record PlaySoundConfiguration(
    ResourceLocation event,
    SoundSource source,
    float volume,
    float pitch,
    Optional<Vec3> pos
) {
}
