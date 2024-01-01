package dev.glaeos.demonaday;

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

import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class DiscordBot {

    private static final long DEMON_LOG_CHANNEL = 1191093950803624128L;

    protected DiscordBot(@NotNull String token, long guildId,
                         @NotNull Map<ApplicationCommandRequest, Function<ChatInputInteractionEvent, String>> commands,
                         @NotNull Function<Message, Publisher<?>> demonLogMessageHandler) {
        DiscordClient client = DiscordClient.create(token);
        client.gateway().setEnabledIntents(IntentSet.of(Intent.MESSAGE_CONTENT));
        GatewayDiscordClient gateway = client.login().block();
        assert gateway != null;

        gateway.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getChannelId().asLong() == DEMON_LOG_CHANNEL)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(demonLogMessageHandler)
                .subscribe();

        GuildCommandRegistrar.create(gateway.getRestClient(), commands.keySet().stream().toList())
                .registerCommands(Snowflake.of(guildId))
                .doOnError(err -> System.err.println("Failed to create guild command: " + err))
                .onErrorResume(err -> Mono.empty())
                .blockLast();

        gateway.on(new ReactiveEventAdapter() {
            @Override
            public @NotNull Publisher<?> onChatInputInteraction(@NotNull ChatInputInteractionEvent event) {
                for (ApplicationCommandRequest command : commands.keySet()) {
                    if (command.name().equals(event.getCommandName())) {
                        return event.reply(commands.get(command).apply(event));
                    }
                }
                return Mono.empty();
            }
        }).blockLast();
    }

    public static DiscordBot start(@NotNull String token, long guildId,
                                   @NotNull Map<ApplicationCommandRequest, Function<ChatInputInteractionEvent, String>> commands,
                                   @NotNull Function<Message, Publisher<?>> demonLogMessageHandler) {
        checkNotNull(token);
        checkNotNull(commands);
        return new DiscordBot(token, guildId, commands, demonLogMessageHandler);
    }

}
