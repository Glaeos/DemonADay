package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Command;
import dev.glaeos.demonaday.messages.MessageHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.interaction.GuildCommandRegistrar;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

    protected DiscordBot(@NotNull String token, long guildId,
                         @NotNull List<Command> commands, @NotNull List<MessageHandler> messageHandlers) {


        DiscordClient client = DiscordClient.create(token);
        client.gateway().setEnabledIntents(IntentSet.of(Intent.MESSAGE_CONTENT));
        GatewayDiscordClient gateway = client.gateway().withEventDispatcher(d -> d.on(ReadyEvent.class)
                .doOnNext(event -> LOGGER.info("Logged in as " + event.getSelf().getUsername())))
                .login().block();
        assert gateway != null;

        Map<Long, MessageHandler> handlers = new HashMap<>();
        for (MessageHandler handler : messageHandlers) {
            handlers.put(handler.getChannelId(), handler);
        }

        gateway.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(message -> handlers.containsKey(message.getChannelId().asLong()))
                .flatMap(message -> handlers.get(message.getChannelId().asLong()).handle(message.getChannel().block(), message))
                .subscribe();

        GuildCommandRegistrar.create(gateway.getRestClient(), commands.stream().map(Command::getAppCommand).toList())
                .registerCommands(Snowflake.of(guildId))
                .doOnError(err -> LOGGER.error("Failed to create guild command: " + err))
                .onErrorResume(err -> Mono.empty())
                .blockLast();

        gateway.on(new ReactiveEventAdapter() {
            @Override
            public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
                for (Command command : commands) {
                    if (command.getAppCommand().name().equals(event.getCommandName())) {
                        return event.reply(command.getCommandHandler().apply(event));
                    }
                }
                return Mono.empty();
            }
        }).blockLast();
    }

    public static DiscordBot start(@NotNull String token, long guildId,
                                   @NotNull List<Command> commands, @NotNull List<MessageHandler> messageHandlers) {
        checkNotNull(token);
        checkNotNull(commands);
        return new DiscordBot(token, guildId, commands, messageHandlers);
    }

}
