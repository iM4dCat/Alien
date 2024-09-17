package dev.luminous.api.events.eventbus;

import java.util.function.Consumer;

/**
 * Listener that takes in a {@link java.util.function.Consumer}.
 */
public class ConsumerListener<T> implements IListener {
    private final Class<?> target;
    private final int priority;
    private final Consumer<T> executor;

    public ConsumerListener(Class<?> target, int priority, Consumer<T> executor) {
        this.target = target;
        this.priority = priority;
        this.executor = executor;
    }
    
    public ConsumerListener(Class<?> target, Consumer<T> executor) {
        this(target, EventPriority.MEDIUM, executor);
    }

    //@SuppressWarnings("unchecked")
    @Override
    public void call(Object event) {
        executor.accept((T) event);
    }

    @Override
    public Class<?> getTarget() {
        return target;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
