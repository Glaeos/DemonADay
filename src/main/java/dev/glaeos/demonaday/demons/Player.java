package dev.glaeos.demonaday.demons;

import dev.glaeos.demonaday.serialization.PrimitiveSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class Player {

    private final long userId;

    private final List<DemonCompletion> completions;

    private boolean disabled;

    private final Semaphore lock;

    public Player(long userId) {
        this.userId = userId;
        completions = new ArrayList<>();
        disabled = false;
        lock = new Semaphore(1);
    }

    public long getUserId() {
        return userId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void acquire() throws InterruptedException {
        lock.acquire();
    }

    public void release() {
        lock.release();
    }

    public void disable() {
        disabled = true;
    }

    public void enable() {
        disabled = false;
    }

    public List<DemonCompletion> getCompletions() {
        return completions;
    }

    public boolean hasCompleted(int levelId) throws IllegalStateException {
        if (disabled) {
            throw new IllegalStateException("shouldn't attempt to access completions of disabled player");
        }
        return completions.stream().anyMatch(completion -> completion.getLevelId() == levelId);
    }

    public boolean hasCompletionOn(short dayOfYear) throws IllegalStateException, IllegalArgumentException {
        if (disabled) {
            throw new IllegalStateException("shouldn't attempt to access completions of a disabled player");
        }
        if (dayOfYear < 1 || dayOfYear > 366) {
            throw new IllegalArgumentException("day of year cannot be < 1 or > 366");
        }
        return completions.stream().anyMatch(completion -> completion.getDayOfYear() == dayOfYear);
    }

    public @NotNull DemonCompletion getCompletion(int levelId) throws IllegalStateException, IllegalArgumentException {
        if (disabled) {
            throw new IllegalStateException("shouldn't attempt to access completions of disabled player");
        }

        for (DemonCompletion completion : completions) {
            if (completion.getLevelId() == levelId) {
                return completion;
            }
        }
        throw new IllegalArgumentException("completion not found");
    }

    public @NotNull DemonCompletion getCompletionOn(short dayOfYear) throws IllegalStateException, IllegalArgumentException {
        if (disabled) {
            throw new IllegalStateException("shouldn't attempt to access completions of a disabled player");
        }
        if (dayOfYear < 1 || dayOfYear > 366) {
            throw new IllegalArgumentException("day of year cannot be < 1 or > 366");
        }

        for (DemonCompletion completion : completions) {
            if (completion.getDayOfYear() == dayOfYear) {
                return completion;
            }
        }
        throw new IllegalArgumentException("completion not found");
    }

    public void addCompletion(@NotNull DemonCompletion completion) throws IllegalStateException, IllegalArgumentException {
        checkNotNull(completion);
        if (disabled) {
            throw new IllegalStateException("cannot add completion to disabled player");
        }
        if (hasCompleted(completion.getLevelId())) {
            throw new IllegalArgumentException("cannot add completion as player has already beaten level");
        }
        completions.add(completion);
    }

    public void removeCompletion(@NotNull DemonCompletion completion) throws IllegalStateException, IllegalArgumentException {
        if (disabled) {
            throw new IllegalStateException("cannot remove completion of disabled player");
        }
        if (!completions.contains(completion)) {
            throw new IllegalArgumentException("cannot remove completion which player does not have");
        }
        completions.remove(completion);
    }

    public void clearCompletions() {
        if (disabled) {
            throw new IllegalStateException("cannot remove completions of disabled player");
        }
        completions.clear();
    }

    public int calculatePoints() {
        // TODO: streaks
        int points = 0;
        for (DemonCompletion completion : completions) {
            if (!completion.isVerified()) {
                continue;
            }
            points += completion.getDifficulty().getPoints();
        }
        return points;
    }

    public List<Byte> serialize() {
        List<Byte> data = new ArrayList<>();
        PrimitiveSerializer.writeVarLong(data, userId);
        PrimitiveSerializer.writeVarInt(data, completions.size());

        for (DemonCompletion completion : completions) {
            PrimitiveSerializer.writeVarInt(data, completion.getDayOfYear());
            PrimitiveSerializer.writeVarInt(data, completion.getLevelId());

            if (completion.getDifficulty() == null) {
                PrimitiveSerializer.writeVarInt(data, 0);
            } else {
                PrimitiveSerializer.writeVarInt(data, completion.getDifficulty().ordinal()+1);
            }

            if (completion.isVerified()) {
                data.add((byte) 1);
            } else {
                data.add((byte) 0);
            }
        }
        return data;
    }

}
