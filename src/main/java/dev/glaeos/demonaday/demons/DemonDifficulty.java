package dev.glaeos.demonaday.demons;

public enum DemonDifficulty {

    EASY,
    MEDIUM,
    HARD,
    INSANE,
    EXTREME;

    int getPoints() {
        return (this.ordinal()+1) * 2;
    }

}
