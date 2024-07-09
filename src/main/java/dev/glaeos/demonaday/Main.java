package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Command;
import dev.glaeos.demonaday.commands.Commands;
import dev.glaeos.demonaday.env.Env;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogHandler;
import dev.glaeos.demonaday.player.impl.DefaultPlayerManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
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

    private static byte[] getArray(@NotNull ByteBuf buffer) {
        byte[] finalData;
        if (!buffer.hasArray()) {
            finalData = new byte[buffer.readableBytes()];
            for (int i = 0; i < buffer.readableBytes(); i++) {
                finalData[i] = buffer.readByte();
            }
            return finalData;
        }
        return buffer.array();
    }

    public static void main(String[] args) throws IOException {
        File infile = new File("players.dat");
        FileInputStream inputStream = new FileInputStream(infile);
        ByteBuf inBuffer = Unpooled.wrappedBuffer(inputStream.readAllBytes());
        inputStream.close();

        PlayerManager playerManager = DefaultPlayerManager.load(inBuffer);
        inBuffer.release();
        DemonLogHandler logHandler = new DemonLogHandler(playerManager);
        List<Command> commands = Commands.getCommands(playerManager);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ByteBuf buffer = Unpooled.buffer(1024);
                playerManager.encode(buffer);
                byte[] finalData = getArray(buffer);

                File outfile = new File("players.dat");
                boolean ignored = outfile.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(outfile);
                outputStream.write(finalData);
                outputStream.close();
                buffer.release();
            } catch (Exception err) {
                LOGGER.error("Failed to save data", err);
                LOGGER.info("DUMPING FAILED SAVE DATA");
                for (Player player : playerManager.getPlayers()) {
                    ByteBuf buffer = Unpooled.buffer(256);
                    player.encode(buffer);
                    LOGGER.info("{}: [{}]", player.getUserId(), getArray(buffer));
                    buffer.release();
                }
                LOGGER.info("FAILED SAVE DATA DUMP FINISHED");
            }
        }));

        new DiscordBot(Env.TOKEN, Env.GUILD, commands, List.of(logHandler)).start();
    }

}