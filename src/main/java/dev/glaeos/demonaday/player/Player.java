package dev.glaeos.demonaday.player;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.util.Acquirable;
import dev.glaeos.demonaday.util.Encodable;
import dev.glaeos.demonaday.util.Toggleable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Player extends Acquirable, Toggleable, Encodable {

    long getUserId();

    @NotNull Collection<DemonCompletion> getCompletions();

    default boolean hasCompleted(int levelId) {
        return getCompletions().stream().anyMatch(completion -> completion.getLevelId() == levelId);
    }

    default @Nullable DemonCompletion getCompletion(int levelId) {
        return getCompletions().stream().filter(completion -> completion.getLevelId() == levelId).findFirst().orElse(null);
    }

    default boolean hasCompletionOn(short dayOfYear) {
        return getCompletions().stream().anyMatch(completion -> completion.getDayOfYear() == dayOfYear);
    }

    default @Nullable DemonCompletion getCompletionOn(short dayOfYear) {
        return getCompletions().stream().filter(completion -> completion.getDayOfYear() == dayOfYear).findFirst().orElse(null);
    }

    void addCompletion(@NotNull DemonCompletion completion);

    void removeCompletion(@NotNull DemonCompletion completion);

    void clearCompletions();

}
