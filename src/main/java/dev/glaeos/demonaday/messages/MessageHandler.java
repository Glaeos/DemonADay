package dev.glaeos.demonaday.messages;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

public interface MessageHandler {

    long getChannelId();

    Publisher<?> handle(@NotNull MessageChannel channel, @NotNull Message message);

}
