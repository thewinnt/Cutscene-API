package net.thewinnt.cutscenes.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for all events.
 * @param <T> the type of the listener
 */
// very much inspired by Fabric API
public class Event<T> {
    private final List<T> listeners = new ArrayList<>();

    public Event() {}

    public void addListener(T listener) {
        listeners.add(listener);
    }

    public void invoke(Consumer<T> handler) {
        listeners.forEach(handler);
    }
}
