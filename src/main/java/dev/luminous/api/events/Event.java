package dev.luminous.api.events;

public class Event {
    private final Stage stage;
    private boolean cancel;
    public Event(Stage stage) {
        this.cancel = false;
        this.stage = stage;
    }

    public void cancel() {
        setCancelled(true);
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isPost() {
        return stage == Stage.Post;
    }

    public boolean isPre() {
        return stage == Stage.Pre;
    }

    public enum Stage{
        Pre, Post
    }
}
