package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Commands;
import dev.glaeos.demonaday.env.Env;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogHandler;
import dev.glaeos.demonaday.player.impl.DefaultPlayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        File infile = new File("players.dat");
        FileInputStream inputStream = new FileInputStream(infile);
        ByteBuffer buffer = ByteBuffer.wrap(inputStream.readAllBytes());
        inputStream.close();

        PlayerManager playerManager = DefaultPlayerManager.load(buffer);
        DemonLogHandler logHandler = new DemonLogHandler(playerManager);
        Commands commands = new Commands(playerManager);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Collection<Byte> data = playerManager.encode();
                byte[] finalData = new byte[data.size()];
                int i = 0;
                for (Byte dataByte : data) {
                    finalData[i] = dataByte;
                    i++;
                }

                File outfile = new File("players.dat");
                boolean ignored = outfile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(outfile);
                outputStream.write(finalData);
                outputStream.close();
            } catch (Exception err) {
                LOGGER.error("Failed to save: " + err);
                LOGGER.info("DUMPING FAILED SAVE DATA");
                for (Player player : playerManager.getPlayers()) {
                    LOGGER.info(player.getUserId() + ": [" + player.encode() + "]");
                }
                LOGGER.info("FAILED SAVE DATA DUMP FINISHED");
            }
        }));

        new DiscordBot(Env.TOKEN, Env.GUILD, commands.COMMANDS, List.of(logHandler)).start();
    }

}