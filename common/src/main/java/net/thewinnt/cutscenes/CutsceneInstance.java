package net.thewinnt.cutscenes;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.networking.packets.CutsceneOverPacket;
import net.thewinnt.cutscenes.time.TimeManager;
import net.thewinnt.cutscenes.transition.Transition;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CutsceneInstance {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final CutsceneType cutscene;
    private final TimeManager timeManager;
    private final double length;
    private double time = 0;
    private boolean initialized = false;
    private int phase = 0;
    private final List<CutsceneEffect<?>> startedEffects = new ArrayList<>();
    private final List<CutsceneEffect<?>> endedEffects = new ArrayList<>();
    private boolean endedStartTransition;
    private boolean endedEndTransition;

    public CutsceneInstance(CutsceneType cutscene) {
        this.cutscene = cutscene;
        this.timeManager = cutscene.length.manager();
        this.length = cutscene.length.length();
    }

    /**
     * Ticks the cutscene logic, mainly the {@code onStart}/{@code onFrame}/{@code onEnd} methods
     * of {@linkplain Transition transitions} and {@linkplain CutsceneEffect cutscene effects}
     * @return {@code true} if the cutscene should continue
     */
    public boolean tick() {
        if (!initialized) {
            this.timeManager.start();
            this.initialized = true;
        }
        this.time = this.timeManager.tick();
        if (this.time < 0) {
            LOGGER.warn("Negative time: {}", this.time);
            this.time = 0;
        } else if (this.time > this.getEndTime()) {
            LOGGER.warn("Suspicious time: {}", this.time);
        }
        if (isTimeForStart()) {
            Transition transition = cutscene.startTransition;
            double progress = time / transition.getLength();
            if (phase == 0) {
                phase++;
                transition.onStart(cutscene);
            }
            transition.onFrame(progress, cutscene);
        } else if (isTimeForEnd()) {
            Transition transition = cutscene.endTransition;
            double progress = getEndProress();
            // this is here in case the else branch never executes (e.g. the time between transitions is zero)
            if (phase == 0) {
                cutscene.startTransition.onStart(cutscene); // hold our promise!
                phase++;
            }
            if (phase == 1) {
                cutscene.startTransition.onEnd(cutscene);
                endedStartTransition = true;
                phase++;
            }
            if (phase == 2) {
                phase++;
                transition.onStart(cutscene);
            }
            transition.onFrame(progress, cutscene);
            if (progress >= 1) {
                ClientCutsceneManager.stopCutsceneImmediate(EndingReason.FINISH);
                if (!timeManager.isServerSynched()) {
                    CutsceneAPI.platform().sendPacketFromPlayer(new CutsceneOverPacket());
                }
                cutscene.endTransition.onEnd(cutscene);
                endedEndTransition = true;
            }
        } else {
            // if the starting transition was too quick, we may end up never ticking it. no_op usually does it.
            if (phase == 0) {
                cutscene.startTransition.onStart(cutscene); // hold our promise!
                phase++;
            }
            if (phase == 1) {
                cutscene.startTransition.onEnd(cutscene);
                phase++;
                endedStartTransition = true;
            }
        }
        for (CutsceneEffect<?> i : cutscene.effects) {
            if (time >= i.startTime) {
                if (!startedEffects.contains(i)) {
                    i.onStart(Minecraft.getInstance().level, cutscene);
                    startedEffects.add(i);
                }
                if (time < i.endTime) {
                    i.onFrame(time - i.startTime, Minecraft.getInstance().level, cutscene);
                } else if (!endedEffects.contains(i)) {
                    i.onEnd(Minecraft.getInstance().level, cutscene);
                    endedEffects.add(i);
                }
            }
        }
        return !endedEndTransition; // this effectively means "is cutscene over?"
    }

    public double getTime() {
        return time;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public double getEndTime() {
        double output = length;
        output += cutscene.startTransition.getOffCutsceneTime();
        output += cutscene.endTransition.getOffCutsceneTime();
        return output;
    }

    public boolean isTimeForStart() {
        return getTime() < cutscene.startTransition.getLength();
    }

    public boolean isTimeForEnd() {
        double endTime = getEndTime();
        return endTime - time < cutscene.endTransition.getLength();
    }

    public double getEndProress() {
        double endTime = getEndTime();
        return (cutscene.endTransition.getLength() - (endTime - time)) / (double)cutscene.endTransition.getLength();
    }

    public boolean endedStartTransition() {
        return endedStartTransition;
    }

    public boolean endedEndTransition() {
        return endedEndTransition;
    }
}
