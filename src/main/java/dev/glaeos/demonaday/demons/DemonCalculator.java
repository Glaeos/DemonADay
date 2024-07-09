package dev.glaeos.demonaday.demons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DemonCalculator {

    private static final @NotNull Function<DemonDifficulty, Integer> POINTS_FORMULA =
        difficulty -> (difficulty.ordinal() + 1) * 2;

    private static final double STREAK_MULTIPLIER = 0.5D;

    private DemonCalculator() {

    }

    private static @NotNull Collection<DemonCompletion> sortCompletions(@NotNull Collection<DemonCompletion> completions) {
        return completions.stream()
            .filter(DemonCompletion::isEnabled)
            .sorted(Comparator.comparingInt(completion -> (int) completion.getDayOfYear()))
            .collect(Collectors.toList());
    }

    private static @NotNull List<Streak> findStreaks(@NotNull Collection<DemonCompletion> completions) {
        completions = sortCompletions(completions);

        List<Streak> streaks = new ArrayList<>();
        short startDay = -1;
        short prevDayOfYear = -1;
        DemonCompletion lastVisited = null;

        for (DemonCompletion completion : completions) {
            lastVisited = completion;
            if (startDay == -1 || prevDayOfYear == -1) {
                startDay = completion.getDayOfYear();
                prevDayOfYear = completion.getDayOfYear();
                continue;
            }

            if (prevDayOfYear != completion.getDayOfYear() - 1) {
                streaks.add(new Streak(startDay, prevDayOfYear));
                startDay = completion.getDayOfYear();
            }
            prevDayOfYear = completion.getDayOfYear();
        }

        if (lastVisited != null) {
            streaks.add(new Streak(startDay, prevDayOfYear));
        }
        return streaks;
    }

    public static @Nullable Streak findStreakIncluding(@NotNull Collection<DemonCompletion> completions, short dayOfYear) {
        return findStreaks(completions).stream()
            .filter(streak -> streak.startDay() <= dayOfYear && streak.endDay() >= dayOfYear)
            .findFirst().orElse(null);
    }

    public static @Nullable Streak findLongestStreak(@NotNull Collection<DemonCompletion> completions) {
        return findStreaks(completions).stream()
            .max(Comparator.comparingInt(Streak::getSize))
            .orElse(null);
    }

    public static int calculatePoints(@NotNull Collection<DemonCompletion> completions) {
        completions = sortCompletions(completions);

        int streak = 0;
        int total = 0;
        int prevDayOfYear = -1;
        for (DemonCompletion completion : completions) {
            DemonDifficulty difficulty = completion.getDifficulty();
            if (difficulty == null) {
                throw new NullPointerException("Verified demon completion does not have difficulty set");
            }
            int basePoints = POINTS_FORMULA.apply(difficulty);

            if (prevDayOfYear != completion.getDayOfYear() - 1) {
                streak = 0;
                total += basePoints;
            } else {
                streak++;
                total += (int) (basePoints * (1 + STREAK_MULTIPLIER * streak));
            }
            prevDayOfYear = completion.getDayOfYear();
        }

        return total;
    }

}
