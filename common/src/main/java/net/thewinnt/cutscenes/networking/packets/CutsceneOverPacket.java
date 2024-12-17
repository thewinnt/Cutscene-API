package net.thewinnt.cutscenes.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.platform.AbstractServerboundPacket;
import net.thewinnt.cutscenes.util.ServerPlayerExt;

public class CutsceneOverPacket implements AbstractServerboundPacket {
    public static final ResourceLocation ID = new ResourceLocation("cutscenes", "cutscene_over");

    @Override
    public void execute(ServerPlayer player) {
        ((ServerPlayerExt) player).csapi$finishCutscene(EndingReason.FINISH);
    }

    @Override
    public void write(FriendlyByteBuf FriendlyByteBuf) {}

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
