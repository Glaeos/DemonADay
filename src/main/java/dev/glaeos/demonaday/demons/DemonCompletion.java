package dev.glaeos.demonaday.demons;

public class DemonCompletion {

    private final long userId;

    private final short dayOfYear;

    private final int levelId;

    private final DemonDifficulty difficulty;

    private boolean verified;

    public DemonCompletion(long userId, short dayOfYear, int levelId, DemonDifficulty difficulty, boolean verified) {
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

    public DemonDifficulty getDifficulty() {
        return difficulty;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

}
