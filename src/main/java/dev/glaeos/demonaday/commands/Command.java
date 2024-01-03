package dev.glaeos.demonaday.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.function.Function;

public interface Command {

    ApplicationCommandRequest getAppCommand();

    void handle(ChatInputInteractionEvent interaction);

}
