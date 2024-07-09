package dev.glaeos.demonaday.test;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.demons.DemonCalculator;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        List<DemonCompletion> completions = new ArrayList<>();
        completions.add(new DemonCompletion((short) 1, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 2, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 3, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 7, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 8, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 12, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 14, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 13, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 15, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 16, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 19, 100, DemonDifficulty.EASY, true));
        completions.add(new DemonCompletion((short) 18, 100, DemonDifficulty.EASY, true));

        System.out.println(DemonCalculator.calculatePoints(completions));
        System.out.println(DemonCalculator.findLongestStreak(completions));
    }

}
