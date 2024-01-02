package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Commands;
import dev.glaeos.demonaday.demons.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogHandler;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        PlayerManager playerManager = new PlayerManager();
        DemonLogHandler logHandler = new DemonLogHandler(playerManager);
        DiscordBot.start(Env.TOKEN, Env.GUILD, Commands.COMMANDS, List.of(logHandler));
    }

}