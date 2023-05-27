package net.thewinnt.cutscenes.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.level.ServerPlayer;
import net.thewinnt.cutscenes.util.ServerPlayerExt;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerExt {
    public int cutsceneTicksRemaining;

    @Override
    public int getCutsceneTicks() {
        return cutsceneTicksRemaining;
    }

    @Override
    public void setCutsceneTicks(int value) {
        cutsceneTicksRemaining = value;
    }
}
