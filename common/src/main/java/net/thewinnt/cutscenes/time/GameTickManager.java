package net.thewinnt.cutscenes.time;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.util.MathHelper;

public class GameTickManager implements TimeManager {
    private long startGameTime;
    private double prevRealTime;
    private double currentRealTime;
    private double nextRealTime;
    private long prevGameTime;
    private double currentGameTime;
    private long nextGameTime;

    @Override
    public double tick() {
        if (Minecraft.getInstance().isPaused()) {
            double diff = now() - currentRealTime;
            prevRealTime += diff;
            currentRealTime += diff;
            nextRealTime += diff;
            return currentGameTime - startGameTime;
        }
        this.currentRealTime = now();
        this.currentGameTime = Mth.clampedMap(currentRealTime, prevRealTime, nextRealTime, prevGameTime, nextGameTime);
        return currentGameTime - startGameTime;
    }

    @Override
    public void start() {
        this.startGameTime = ClientCutsceneManager.getStartGameTime();
        currentGameTime = startGameTime;
        this.prevGameTime = startGameTime;
        this.nextGameTime = prevGameTime + 20;
        currentRealTime = now();
        this.prevRealTime = currentRealTime;
        this.nextRealTime = prevRealTime + 1;
    }

    @Override
    public void syncGameTime(long gameTime) {
        this.prevRealTime = currentRealTime;
        this.nextRealTime = now() + 1;
        this.prevGameTime = (long)currentGameTime;
        this.nextGameTime = gameTime + 20;
    }

    @Override
    public boolean isServerSynched() {
        return true;
    }

    @Override
    public String type() {
        return "ticks";
    }

    private double now() {
        return Util.getNanos() / 1000000000.0;
    }

    @Override
    public double ticksPerUnit() {
        return 1;
    }
}
