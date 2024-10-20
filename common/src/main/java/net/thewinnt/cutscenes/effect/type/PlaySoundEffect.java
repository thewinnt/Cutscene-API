package net.thewinnt.cutscenes.effect.type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.effect.CutsceneEffectSerializer;
import net.thewinnt.cutscenes.effect.configuration.PlaySoundConfiguration;
import net.thewinnt.cutscenes.effect.serializer.PlaySoundSerializer;

public class PlaySoundEffect extends CutsceneEffect<PlaySoundConfiguration> {
    public PlaySoundEffect(double startTime, double endTime, PlaySoundConfiguration config) {
        super(startTime, endTime, config);
    }

    @Override
    public void onStart(ClientLevel level, CutsceneType cutscene) {
        if (this.config.pos().isPresent()) {
            Vec3 pos = this.config.pos().get();
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                this.config.event(),
                this.config.source(),
                this.config.volume(),
                this.config.pitch(),
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.LINEAR,
                pos.x,
                pos.y,
                pos.z,
                true
            ));
        } else {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                this.config.event(),
                this.config.source(),
                this.config.volume(),
                this.config.pitch(),
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.LINEAR,
                0,
                0,
                0,
                true
            ));
        }
    }

    @Override
    public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {}

    @Override
    public void onEnd(ClientLevel level, CutsceneType cutscene) {}

    @Override
    public CutsceneEffectSerializer<PlaySoundConfiguration> getSerializer() {
        return PlaySoundSerializer.INSTANCE;
    }
}
