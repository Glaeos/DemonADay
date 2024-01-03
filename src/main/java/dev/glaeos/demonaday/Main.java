package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Commands;
import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogHandler;
import dev.glaeos.demonaday.serialization.Loader;
import dev.glaeos.demonaday.serialization.PrimitiveSerializer;
import dev.glaeos.demonaday.serialization.SimpleBuffer;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        PlayerManager playerManager = Loader.load("players.dat");
        DemonLogHandler logHandler = new DemonLogHandler(playerManager);
        Commands commands = new Commands(playerManager);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                playerManager.save("players.dat");
            } catch (Exception err) {
                LOGGER.error("Failed to save: " + err);
                LOGGER.info("DUMPING FAILED SAVE DATA");
                for (Player player : playerManager.getPlayers()) {
                    LOGGER.info(player.getUserId() + ": [" + player.serialize() + "]");
                }
                LOGGER.info("FAILED SAVE DATA DUMP FINISHED");
            }
        }));

        DiscordBot.start(Env.TOKEN, Env.GUILD, commands.COMMANDS, List.of(logHandler));
    }

}