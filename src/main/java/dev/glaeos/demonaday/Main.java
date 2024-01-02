package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Commands;
import dev.glaeos.demonaday.demons.RecordManager;
import dev.glaeos.demonaday.messages.DemonLogHandler;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        RecordManager recordManager = new RecordManager();
        DemonLogHandler logHandler = new DemonLogHandler(recordManager);
        DiscordBot.start(Env.TOKEN, Env.GUILD, Commands.COMMANDS, List.of(logHandler));
    }

}