package net.thewinnt.cutscenes.effect.configuration;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record PlaySoundConfiguration(
    Identifier event,
    SoundSource source,
    float volume,
    float pitch,
    Optional<Vec3> pos
) {
}
