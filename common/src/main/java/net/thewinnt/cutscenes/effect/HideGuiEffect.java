package net.thewinnt.cutscenes.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.thewinnt.cutscenes.CutsceneType;

public class HideGuiEffect extends CutsceneEffect<Void> {
    public HideGuiEffect(double startTime, double endTime, Void config) {
        super(startTime, endTime, config);
    }

    @Override
    public void onStart(ClientLevel level, CutsceneType cutscene) {
        Minecraft.getInstance().options.hideGui = true;
    }

    @Override
    public void onFrame(double time, ClientLevel level, CutsceneType cutscene) {
        Minecraft.getInstance().options.hideGui = true;
    }

    @Override
    public void onEnd(ClientLevel level, CutsceneType cutscene) {
        Minecraft.getInstance().options.hideGui = false;
    }

    @Override
    public CutsceneEffectSerializer<Void> getSerializer() {
        return CutsceneEffectSerializer.HIDE_GUI;
    }
}
