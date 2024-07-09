package dev.glaeos.demonaday.messages;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.env.DiscordConstant;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import dev.glaeos.demonaday.player.impl.DefaultPlayer;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

public class DemonLogHandler implements MessageHandler {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DemonLogHandler.class);
    private static final long CHANNEL = 1191085366011244644L;

    private final @NotNull PlayerManager playerManager;

    public DemonLogHandler(@NotNull PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public long getChannelId() {
        return CHANNEL;
    }

    public @NotNull Publisher<?> handle(@Nullable MessageChannel channel, @NotNull Message message) {
        if (channel == null) {
            return Mono.empty();
        }

        Optional<User> author = message.getAuthor();
        if (author.isEmpty()) {
            return Mono.empty();
        }

        LocalDate time = LocalDate.now(DiscordConstant.TIMEZONE);
        long userId = author.get().getId().asLong();
        int levelId;

        try {
            String content = message.getContent().stripLeading().toLowerCase();
            if (content.startsWith("id:")) {
                content = content.substring(3).stripLeading();
            } else if (content.startsWith("id")) {
                content = content.substring(2).stripLeading();
            }

            int i;
            for (i = 0; i < content.length(); i++) {
                if (!Character.isDigit(content.charAt(i))) {
                    break;
                }
            }
            content = content.substring(0, i);

            try {
                levelId = Integer.parseInt(content);
            } catch (NumberFormatException err) {
                return channel.createMessage(DemonLogResponse.failure(userId, DemonLogResponse.FailReason.MISSING_LEVEL_ID, time, null));
            }

            if (levelId < 1 || levelId > 120000000) {
                return channel.createMessage(DemonLogResponse.failure(userId, DemonLogResponse.FailReason.INVALID_LEVEL_ID, time, levelId));
            }
            if (message.getAttachments().size() < 2) {
                return channel.createMessage(DemonLogResponse.failure(userId, DemonLogResponse.FailReason.MISSING_ATTACHMENTS, time, levelId));
            }
        } catch (Exception err) {
            LOGGER.error("Demon log handler encountered exception during pre-player processing", err);
            return channel.createMessage(DemonLogResponse.error(userId));
        }

        try {
            Player player;
            playerManager.acquire();
            try {
                if (!playerManager.hasPlayer(userId)) {
                    playerManager.addPlayer(new DefaultPlayer(userId));
                }
                player = playerManager.getPlayer(userId);
                if (player == null) {
                    return channel.createMessage(DemonLogResponse.error(userId));
                }
            } finally {
                playerManager.release();
            }

            player.acquire();
            try {
                if (!player.isEnabled()) {
                    return channel.createMessage(DemonLogResponse.failure(userId, DemonLogResponse.FailReason.PLAYER_DISABLED, time, levelId));
                }
                if (player.hasCompleted(levelId)) {
                    return channel.createMessage(DemonLogResponse.failure(userId, DemonLogResponse.FailReason.ALREADY_COMPLETED, time, levelId));
                }
                if (player.hasCompletionOn((short) time.getDayOfYear())) {
                    return channel.createMessage(DemonLogResponse.failure(userId, DemonLogResponse.FailReason.ALREADY_SUBMITTED_TODAY, time, levelId));
                }
                player.addCompletion(new DemonCompletion((short) time.getDayOfYear(), levelId, null, false));
            } finally {
                player.release();
            }
            return channel.createMessage(DemonLogResponse.success(userId, time, levelId));
        } catch (Exception err) {
            LOGGER.error("Demon log handler encountered exception during player processing", err);
            return channel.createMessage(DemonLogResponse.error(userId));
        }
    }

}
