package dev.glaeos.demonaday.serialization;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public class Loader {

    protected static Player loadPlayer(SimpleBuffer buffer) {
        long userId = PrimitiveSerializer.readVarLong(buffer);
        if (userId < 1) {
            throw new IllegalArgumentException("deserializing user id failed");
        }
        Player player = new Player(userId);

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

            byte difficultyByte = buffer.next();
            DemonDifficulty difficulty;
            if (difficultyByte == 0) {
                difficulty = null;
            } else {
                difficulty = DemonDifficulty.values()[difficultyByte-1];
            }

            byte verifiedByte = buffer.next();
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

    public static PlayerManager load(@NotNull String filename) throws IOException, InterruptedException {
        checkNotNull(filename);
        PlayerManager manager = new PlayerManager();
        manager.acquire();
        File file = new File(filename);
        if (!file.exists()) {
            manager.release();
            return manager;
        }
        FileInputStream reader = new FileInputStream(file);
        SimpleBuffer buffer = new SimpleBuffer(reader.readAllBytes());
        reader.close();

        while (buffer.hasNext()) {
            manager.addPlayer(loadPlayer(buffer));
        }
        manager.release();
        return manager;
    }

}
