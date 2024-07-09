package dev.glaeos.demonaday.player.impl;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.util.PrimitiveSerializer;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultPlayer implements Player {

    private final @NotNull Lock lock;
    private boolean enabled;
    private final long userId;
    private final @NotNull List<DemonCompletion> completions;

    public DefaultPlayer(long userId) {
        this.lock = new ReentrantLock();
        this.enabled = true;
        this.userId = userId;
        this.completions = new ArrayList<>();
    }

    public static @NotNull DefaultPlayer load(@NotNull ByteBuffer buffer) {
        long userId = PrimitiveSerializer.readVarLong(buffer);
        if (userId < 1) {
            throw new IllegalArgumentException("deserializing user id failed");
        }
        DefaultPlayer player = new DefaultPlayer(userId);

        int numCompletions = PrimitiveSerializer.readVarInt(buffer);
        if (numCompletions < 0) {
            throw new IllegalArgumentException("deserializing number of completions failed");
        }

        for (int i = 0; i < numCompletions; i++) {
            int dayOfYearInt = PrimitiveSerializer.readVarInt(buffer);
            if (dayOfYearInt < 1 || dayOfYearInt > 366) {
                throw new IllegalArgumentException("deserializing day of year failed");
            }
            short dayOfYear = (short) dayOfYearInt;

            int levelId = PrimitiveSerializer.readVarInt(buffer);
            if (levelId < 1 || levelId > 120000000) {
                throw new IllegalArgumentException("deserializing level id failed");
            }

            byte difficultyByte = buffer.get();
            DemonDifficulty difficulty;
            if (difficultyByte == 0) {
                difficulty = null;
            } else {
                difficulty = DemonDifficulty.values()[difficultyByte-1];
            }

            byte verifiedByte = buffer.get();
            boolean verified;
            if (verifiedByte == 0) {
                verified = false;
            } else if (verifiedByte == 1) {
                verified = true;
            } else {
                throw new IllegalArgumentException("deserializing verified failed");
            }
            player.addCompletion(new DemonCompletion(dayOfYear, levelId, difficulty, verified));
        }
        return player;
    }

    @Override
    public void acquire() {
        lock.lock();
    }

    @Override
    public void release() {
        lock.unlock();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public @NotNull Collection<DemonCompletion> getCompletions() {
        return completions;
    }

    public void addCompletion(@NotNull DemonCompletion completion) throws IllegalStateException, IllegalArgumentException {
        if (!enabled) {
            throw new IllegalStateException("Cannot add completion to disabled player");
        }
        if (hasCompleted(completion.getLevelId())) {
            throw new IllegalArgumentException("Cannot add completion as player has already beaten level");
        }
        completions.add(completion);
    }

    @Override
    public void removeCompletion(@NotNull DemonCompletion completion) {
        if (!enabled) {
            throw new IllegalStateException("Cannot remove completion of disabled player");
        }
        completions.remove(completion);
    }

    @Override
    public void clearCompletions() {
        if (!enabled) {
            throw new IllegalStateException("Cannot remove completions of disabled player");
        }
        completions.clear();
    }

    @Override
    public @NotNull Collection<Byte> encode() {
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

            if (completion.isEnabled()) {
                data.add((byte) 1);
            } else {
                data.add((byte) 0);
            }
        }
        return data;
    }

}
