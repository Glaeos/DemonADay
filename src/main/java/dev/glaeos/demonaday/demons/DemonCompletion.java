package dev.glaeos.demonaday.demons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class DemonCompletion {

    private final long userId;

    private final short dayOfYear;

    private final int levelId;

    private final @Nullable DemonDifficulty difficulty;

    private boolean verified;

    public DemonCompletion(long userId, short dayOfYear, int levelId, @Nullable DemonDifficulty difficulty, boolean verified) {
        if (userId < 1) {
            throw new IllegalArgumentException("user ID cannot be < 1");
        }
        if (dayOfYear < 1 || dayOfYear > 366) {
            throw new IllegalArgumentException("day of year cannot be < 1 or > 366");
        }

        this.userId = userId;
        this.dayOfYear = dayOfYear;
        this.levelId = levelId;
        this.difficulty = difficulty;
        this.verified = verified;
    }

    public long getUserId() {
        return userId;
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
