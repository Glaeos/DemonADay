package dev.glaeos.demonaday.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.function.Function;

public class TestCommand implements Command {

    private static final ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
            .name("test")
            .description("idfk")
            .build();

    private static final Function<ChatInputInteractionEvent, String> handler = applicationCommandInteraction -> "kill yourself, NOW";

    @Override
    public ApplicationCommandRequest getAppCommand() {
        return appCommand;
    }

    @Override
    public Function<ChatInputInteractionEvent, String> getCommandHandler() {
        return handler;
    }

}
