package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Commands;
import dev.glaeos.demonaday.demons.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogHandler;
import dev.glaeos.demonaday.serialization.Loader;

import java.io.FileNotFoundException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        PlayerManager playerManager = Loader.load("players.dat");
        DemonLogHandler logHandler = new DemonLogHandler(playerManager);
        Commands commands = new Commands(playerManager);
        DiscordBot.start(Env.TOKEN, Env.GUILD, commands.COMMANDS, List.of(logHandler));
    }

}