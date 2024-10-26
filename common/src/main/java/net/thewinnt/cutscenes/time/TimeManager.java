package net.thewinnt.cutscenes.time;

import net.minecraft.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A TimeManager controls the speed of the cutscene and its length. It operates with "time units", which
 * may differ in each implementation. It's usually seconds or game ticks.
 */
public interface TimeManager {
    Map<String, Supplier<TimeManager>> REGISTRY = Util.make(new HashMap<>(), map -> {
        map.put("seconds", RealTimeManager::new);
        map.put("ticks", GameTickManager::new);
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
     * If {@code true}, the server player tracks the currently watched cutscene of a player
     * @return whether to track the player cutscene server-side
     * @apiNote meaning and behavior may be changed in the future
     */
    boolean isServerSynched();

    /**
     * Returns the type of this manager. Must be one of the entries in {@link #REGISTRY}.
     * @return the type of this time manager
     */
    String type();

    /**
     * Returns the amount of 1/60ths of a second (one frame on an average monitor) in one unit
     * under typical circumstances (e.g. 20 TPS). Don't think too hard on this one.
     * <p>
     * Used in PathPreviewRenderer to draw reasonably precise previews.
     * @return (unit length) / (1/60)
     */
    double framesPerUnit();
}
