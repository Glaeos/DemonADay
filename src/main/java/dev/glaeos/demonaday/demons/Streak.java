package dev.glaeos.demonaday.demons;

import org.jetbrains.annotations.NotNull;

public record Streak(short startDay, short endDay) {

    public Streak {
        if (startDay < 1 || startDay > 365) {
            throw new IllegalArgumentException("Start day must be > 0 and < 366");
        }
        if (endDay < 1 || endDay > 365) {
            throw new IllegalArgumentException("End day must be > 0 and < 366");
        }
        if (startDay > endDay) {
            throw new IllegalArgumentException("End day must be after start day");
        }
    }

    public static @NotNull Streak none(short dayOfYear) {
        return new Streak(dayOfYear, dayOfYear);
    }

    public int getSize() {
        return endDay - startDay;
    }

}
