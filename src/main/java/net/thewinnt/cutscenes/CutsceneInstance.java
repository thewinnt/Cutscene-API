package net.thewinnt.cutscenes;

import net.minecraft.client.Minecraft;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.transition.Transition;

import java.util.ArrayList;
import java.util.List;

public class CutsceneInstance {
    public final CutsceneType cutscene;
    public final double startTime;
    private double lastTick;
    private int phase = 0;
    private final List<CutsceneEffect<?>> startedEffects = new ArrayList<>();
    private final List<CutsceneEffect<?>> endedEffects = new ArrayList<>();
    private boolean endedStartTransition;
    private boolean endedEndTransition;

    public CutsceneInstance(CutsceneType cutscene) {
        this.cutscene = cutscene;
        this.startTime = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getPartialTick();
        this.lastTick = startTime;
    }

    /**
     * Ticks the cutscene logic, mainly the {@code onStart}/{@code onFrame}/{@code onEnd} methods
     * of {@link Transition transitions} and {@link CutsceneEffect cutscene effects}
     */
    public void tick(double time) {
        if (time < lastTick) time = lastTick;
        lastTick = time;
        double localTime = getTime();
        if (isTimeForStart()) {
            Transition transition = cutscene.startTransition;
            double progress = localTime / (double)transition.getLength();
            if (phase == 0) {
                phase++;
                transition.onStart(cutscene);
            }
            transition.onFrame(progress, cutscene);
        } else if (isTimeForEnd()) {
            Transition transition = cutscene.endTransition;
            double progress = getEndProress();
            if (phase == 2) {
                phase++;
                transition.onStart(cutscene);
            }
            transition.onFrame(progress, cutscene);
            if (progress >= 1) {
                ClientCutsceneManager.stopCutsceneImmediate();
                cutscene.endTransition.onEnd(cutscene);
            }
        } else if (phase == 1) {
            phase++;
            cutscene.startTransition.onEnd(cutscene);
        }
        for (CutsceneEffect<?> i : cutscene.effects) {
            if (localTime >= i.startTime) {
                if (!startedEffects.contains(i)) {
                    i.onStart(cutscene);
                    startedEffects.add(i);
                }
                if (localTime < i.endTime) {
                    i.onFrame(localTime - i.startTime, Minecraft.getInstance().level, cutscene);
                } else if (!endedEffects.contains(i)) {
                    i.onEnd(cutscene);
                    endedEffects.add(i);
                }
            }

        }
    }

    public double getTime() {
        return lastTick - startTime;
    }

    public double getLastTime() {
        return lastTick;
    }

    public double getEndTime() {
        double output = startTime + cutscene.length;
        output += cutscene.startTransition.getOffCutsceneTime();
        output += cutscene.endTransition.getOffCutsceneTime();
        return output;
    }

    public boolean isTimeForStart() {
        return getTime() < cutscene.startTransition.getLength();
    }

    public boolean isTimeForEnd() {
        double endTime = getEndTime();
        return endTime - lastTick < cutscene.endTransition.getLength();
    }

    public double getEndProress() {
        double endTime = getEndTime();
        return (cutscene.endTransition.getLength() - (endTime - lastTick)) / (double)cutscene.endTransition.getLength();
    }

    public boolean endedStartTransition() {
        return endedStartTransition;
    }

    public boolean endedEndTransition() {
        return endedEndTransition;
    }
}
