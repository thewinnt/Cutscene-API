package net.thewinnt.cutscenes.time;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import net.minecraft.util.Util;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A TimeManager controls the speed of the cutscene and its length. It operates with "time units", which
 * may differ in each implementation. It's usually seconds or game ticks.
 */
public interface TimeManager {
    /**
     * The registry of time managers. I don't expect anyone to ever use this, so it's just a simple
     * mutable map.
     */
    Map<String, Supplier<TimeManager>> REGISTRY = Util.make(new HashMap<>(), map -> {
        map.put("seconds", RealTimeManager::new);
        map.put("ticks", GameTickManager::new);
    });
    /**
     * The registry of default transitions for time managers. This is because with different units come
     * different scales that may or may not be very compatible with each other.
     */
    Map<String, Boolean2ObjectFunction<Transition>> DEFAULT_TRANSITIONS = Util.make(new HashMap<>(), map -> {
        map.put("seconds", isStart -> new SmoothEaseTransition(2, isStart, isStart));
        map.put("ticks", isStart -> new SmoothEaseTransition(40, isStart, isStart));
    });

    /**
     * Updates the internal state and returns the current time.
     * @return a value representing the current time since a cutscene started, in time units.
     * @apiNote calling this method after {@link #start()} does not guarantee that the return value is exactly zero.
     */
    double tick();

    /**
     * Starts the timer from zero. The next call to {@link #tick()} is not guaranteed to return exactly zero
     * after this.
     */
    void start();

    /**
     * Notifies the time manager that the game tick rate has changed.
     * @param tickrate the new tickrate, in game ticks per second.
     */
    void setGameTickRate(float tickrate);

    /**
     * Notifies the time manager that the game time has been synchronized.
     * @param gameTime the current game time
     */
    void syncGameTime(long gameTime);

    /**
     * If this returns {@code true}, it means that cutscene length is synchronized with the server tickrate
     * and can, therefore, be defined in ticks
     * @return whether the length of the cutscene is related to server tickrate
     */
    boolean isServerSynched();

    /**
     * Returns the type of this manager. Must be one of the entries in {@link #REGISTRY}.
     * @return the type of this time manager
     */
    String type();

    /**
     * Returns the amount of 1/20ths of a second (one game tick) in one unit
     * under typical circumstances (e.g. 20 TPS).
     * @return (unit length in seconds) * 20
     */
    double ticksPerUnit();
}
