package dev.glaeos.demonaday.messages;

import dev.glaeos.demonaday.demons.RecordManager;
import dev.glaeos.demonaday.responses.DemonLogFailureReason;
import dev.glaeos.demonaday.responses.DemonLogResponse;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.google.common.base.Preconditions.checkNotNull;

public class DemonLogHandler implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemonLogHandler.class);

    private static final long CHANNEL = 1191093950803624128L;

    private final RecordManager manager;

    public DemonLogHandler(@NotNull RecordManager recordManager) {
        checkNotNull(recordManager);
        manager = recordManager;
    }

    public long getChannelId() {
        return CHANNEL;
    }

    public Publisher<?> handle(@Nullable MessageChannel channel, @NotNull Message message) {
        if (channel == null) {
            return Mono.empty();
        }
        if (message.getAuthor().isEmpty()) {
            return Mono.empty();
        }

        LocalDate time = LocalDate.now(ZoneId.of("America/Chicago"));
        long userId = message.getAuthor().get().getId().asLong();

        String content = message.getContent().stripLeading().toLowerCase();
        if (content.startsWith("id:")) {
            content = content.substring(3).stripLeading();
        }

        int i;
        for (i = 0; i < content.length(); i++) {
            if (!Character.isDigit(content.charAt(i))) {
                break;
            }
        }
        content = content.substring(0, i);

        long levelId;
        try {
            levelId = Long.parseLong(content);
        } catch (NumberFormatException err) {
            return channel.createMessage(DemonLogResponse.failure(userId, time, DemonLogFailureReason.MISSING_LEVEL_ID));
        }

        if (levelId < 1 || levelId > 120000000) {
            return channel.createMessage(DemonLogResponse.failure(userId, time, DemonLogFailureReason.INVALID_LEVEL_ID));
        }
        if (message.getAttachments().size() < 2) {
            return channel.createMessage(DemonLogResponse.failure(userId, time, DemonLogFailureReason.MISSING_ATTACHMENTS));
        }

        return channel.createMessage(DemonLogResponse.success(userId, time, levelId));
    }

}
