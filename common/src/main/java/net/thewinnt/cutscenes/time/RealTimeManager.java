package net.thewinnt.cutscenes.time;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

/**
 * Creates a constant, realtime-based time manager, running steadily at 1 TPS.
 */
public class RealTimeManager implements TimeManager {
    private long startTime;
    private long lastTime;

    @Override
    public double tick() {
        if (Minecraft.getInstance().isPaused()) {
            long diff = Util.getNanos() - lastTime;
            startTime += diff;
            lastTime += diff;
            return (lastTime - startTime) / 1000000000.0;
        }
        lastTime = Util.getNanos();
        return (lastTime - startTime) / 1000000000.0;
    }

    @Override
    public void start() {
        startTime = Util.getNanos();
    }

    @Override
    public void syncGameTime(long gameTime) {}

    @Override
    public boolean isServerSynched() {
        return false;
    }

    @Override
    public String type() {
        return "seconds";
    }

    @Override
    public double ticksPerUnit() {
        return 20;
    }
}
