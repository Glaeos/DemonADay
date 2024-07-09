package dev.glaeos.demonaday.demons;

import dev.glaeos.demonaday.util.Toggleable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DemonCompletion implements Toggleable {

    private final int levelId;
    private final short dayOfYear;
    private boolean verified;
    private @Nullable DemonDifficulty difficulty;

    public DemonCompletion(short dayOfYear, int levelId, @Nullable DemonDifficulty difficulty, boolean verified) {
        if (dayOfYear < 1 || dayOfYear > 366) {
            throw new IllegalArgumentException("day of year cannot be < 1 or > 366");
        }

        this.levelId = levelId;
        this.dayOfYear = dayOfYear;
        this.verified = verified;
        this.difficulty = difficulty;
    }

    public @NotNull String toString() {
        return "DemonCompletion{dayOfYear=" + dayOfYear + ", levelId=" + levelId + ", difficulty=" + difficulty + ", verified=" + verified + "}";
    }

    @Override
    public boolean isEnabled() {
        return verified;
    }

    @Override
    public void setEnabled(boolean enabled) {
        verified = enabled;
    }

    public short getDayOfYear() {
        return dayOfYear;
    }

    public int getLevelId() {
        return levelId;
    }

    public @Nullable DemonDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(@NotNull DemonDifficulty difficulty) {
        this.difficulty = difficulty;
    }

}
