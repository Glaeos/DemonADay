package dev.glaeos.demonaday.messages;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

public interface MessageHandler {

    long getChannelId();

    Publisher<?> handle(@Nullable MessageChannel channel, @NotNull Message message);

}
