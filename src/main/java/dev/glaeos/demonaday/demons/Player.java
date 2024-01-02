package dev.glaeos.demonaday.demons;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class Player {

    private final long userId;

    private final List<DemonCompletion> completions;

    private final boolean disabled;

    private final Lock lock;

    public Player(long userId, boolean disabled) {
        this.userId = userId;
        completions = new ArrayList<>();
        this.disabled = disabled;
        lock = new ReentrantLock();
    }

    public long getUserId() {
        return userId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean hasCompleted(int levelId) throws IllegalStateException {
        if (disabled) {
            throw new IllegalStateException("shouldn't attempt to access completions of disabled player");
        }
        lock.lock();
        boolean result;
        try {
            result = completions.stream().anyMatch(completion -> completion.getLevelId() == levelId);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public @NotNull DemonCompletion getCompletion(int levelId) throws IllegalStateException, IllegalArgumentException {
        if (disabled) {
            throw new IllegalStateException("shouldn't attempt to access completions of disabled player");
        }
        lock.lock();
        DemonCompletion result = null;
        try {
            for (DemonCompletion completion : completions) {
                if (completion.getLevelId() == levelId) {
                    result = completion;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        if (result == null) {
            throw new IllegalArgumentException("completion not found");
        }
        return result;
    }

    public void addCompletion(@NotNull DemonCompletion completion) throws IllegalStateException, IllegalArgumentException {
        checkNotNull(completion);
        if (disabled) {
            throw new IllegalStateException("cannot add completion to disabled player");
        }
        lock.lock();
        try {
            if (completion.getUserId() != userId) {
                throw new IllegalArgumentException("cannot add completion by a different player");
            }
            if (hasCompleted(completion.getLevelId())) {
                throw new IllegalArgumentException("cannot add completion as player has already beaten level");
            }
            completions.add(completion);
        } finally {
            lock.unlock();
        }
    }

    public void removeCompletion(@NotNull DemonCompletion completion) throws IllegalStateException, IllegalArgumentException {
        if (disabled) {
            throw new IllegalStateException("cannot remove completion of disabled player");
        }
        lock.lock();
        try {
            if (!completions.contains(completion)) {
                throw new IllegalArgumentException("cannot remove completion which player does not have");
            }
            completions.remove(completion);
        } finally {
            lock.unlock();
        }
    }

    public void clearCompletions() {
        if (disabled) {
            throw new IllegalStateException("cannot remove completions of disabled player");
        }
        lock.lock();
        try {
            completions.clear();
        } finally {
            lock.unlock();
        }
    }

    public int calculatePoints() {
        // TODO: streaks
        lock.lock();
        int points = 0;
        try {
            for (DemonCompletion completion : completions) {
                if (!completion.isVerified()) {
                    continue;
                }
                points += completion.getDifficulty().getPoints();
            }
        } finally {
            lock.unlock();
        }
        return points;
    }

}
