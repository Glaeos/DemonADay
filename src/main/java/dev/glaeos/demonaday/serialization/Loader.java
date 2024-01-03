package dev.glaeos.demonaday.serialization;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static com.google.common.base.Preconditions.checkNotNull;

public class Loader {

    protected static Player loadPlayer(Scanner reader) {
        long userId = PrimitiveSerializer.readVarLong(reader);
        if (userId < 1) {
            throw new IllegalArgumentException("deserializing user id failed");
        }
        Player player = new Player(userId);

        int numCompletions = PrimitiveSerializer.readVarInt(reader);
        if (numCompletions < 0) {
            throw new IllegalArgumentException("deserializing number of completions failed");
        }

        for (int i = 0; i < numCompletions; i++) {
            int dayOfYearInt = PrimitiveSerializer.readVarInt(reader);
            if (dayOfYearInt < 1 || dayOfYearInt > 366) {
                throw new IllegalArgumentException("deserializing day of year failed");
            }
            short dayOfYear = (short) dayOfYearInt;

            int levelId = PrimitiveSerializer.readVarInt(reader);
            if (levelId < 1 || levelId > 120000000) {
                throw new IllegalArgumentException("deserializing level id failed");
            }

            byte difficultyByte = reader.nextByte();
            DemonDifficulty difficulty;
            if (difficultyByte == 0) {
                difficulty = null;
            } else {
                difficulty = DemonDifficulty.values()[difficultyByte-1];
            }

            byte verifiedByte = reader.nextByte();
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

    public static PlayerManager load(@NotNull String filename) throws FileNotFoundException {
        checkNotNull(filename);
        Scanner reader = new Scanner(new File(filename));
        PlayerManager manager = new PlayerManager();
        while (reader.hasNext()) {
            manager.addPlayer(loadPlayer(reader));
        }
        return manager;
    }

}
