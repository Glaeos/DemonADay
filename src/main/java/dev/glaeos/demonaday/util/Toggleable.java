package dev.glaeos.demonaday.util;

public interface Toggleable {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    default void enable() {
        if (!isEnabled()) {
            setEnabled(true);
        }
    }

    default void disable() {
        if (isEnabled()) {
            setEnabled(false);
        }
    }

}
