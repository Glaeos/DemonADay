package dev.glaeos.demonaday.demons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class DemonCompletion {

    private final short dayOfYear;

    private final int levelId;

    private @Nullable DemonDifficulty difficulty;

    private boolean verified;

    public DemonCompletion(short dayOfYear, int levelId, @Nullable DemonDifficulty difficulty, boolean verified) {
        if (dayOfYear < 1 || dayOfYear > 366) {
            throw new IllegalArgumentException("day of year cannot be < 1 or > 366");
        }

        this.dayOfYear = dayOfYear;
        this.levelId = levelId;
        this.difficulty = difficulty;
        this.verified = verified;
    }

    public String toString() {
        return "DemonCompletion{dayOfYear=" + dayOfYear + ", levelId=" + levelId + ", difficulty=" + difficulty + ", verified=" + verified + "}";
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
        checkNotNull(difficulty);
        this.difficulty = difficulty;
    }

    public boolean isVerified() {
        return verified;
    }

    public void verify() {
        verified = true;
    }

    public void reject() {
        verified = false;
    }

}
