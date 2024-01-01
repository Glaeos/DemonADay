package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.TestCommand;
import dev.glaeos.demonaday.demons.LogHandler;
import dev.glaeos.demonaday.demons.RecordManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) {
        RecordManager recordManager = new RecordManager();
        LogHandler logHandler = new LogHandler(recordManager);

        TestCommand test = new TestCommand();

        Map<ApplicationCommandRequest, Function<ChatInputInteractionEvent, String>> commands = new HashMap<>();
        commands.put(test.getAppCommand(), test.getCommandHandler());

        DiscordBot.start(Env.TOKEN, Env.GUILD, commands, logHandler::handleMessage);
    }

}