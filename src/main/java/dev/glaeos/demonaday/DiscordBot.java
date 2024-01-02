package dev.glaeos.demonaday;

import dev.glaeos.demonaday.commands.Command;
import dev.glaeos.demonaday.messages.MessageHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.interaction.GuildCommandRegistrar;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class DiscordBot {

    private static final long DEMON_LOG_CHANNEL = 1191093950803624128L;

    protected DiscordBot(@NotNull String token, long guildId,
                         @NotNull List<Command> commands, @NotNull List<MessageHandler> messageHandlers) {


        DiscordClient client = DiscordClient.create(token);
        client.gateway().setEnabledIntents(IntentSet.of(Intent.MESSAGE_CONTENT));
        GatewayDiscordClient gateway = client.login().block();
        assert gateway != null;

        Map<Long, MessageHandler> handlers = new HashMap<>();
        for (MessageHandler handler : messageHandlers) {
            handlers.put(handler.getChannelId(), handler);
        }

        gateway.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> handlers.containsKey(message.getChannelId().asLong()))
                .flatMap(message -> message.getChannel().map(channel -> handlers.get(message.getChannelId().asLong()).handle(channel, message)))
                .subscribe();

        GuildCommandRegistrar.create(gateway.getRestClient(), commands.stream().map(Command::getAppCommand).toList())
                .registerCommands(Snowflake.of(guildId))
                .doOnError(err -> System.err.println("Failed to create guild command: " + err))
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
