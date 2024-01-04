package dev.glaeos.demonaday.messages;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
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

    private static final long CHANNEL = 1191085366011244644L;

    private final PlayerManager playerManager;

    public DemonLogHandler(@NotNull PlayerManager playerManager) {
        checkNotNull(playerManager);
        this.playerManager = playerManager;
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
        long userId = message.getAuthor().get().getId().asLong();
        int levelId;
        LocalDate time = LocalDate.now(ZoneId.of("America/Chicago"));
        ;

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
                return channel.createMessage(DemonLogResponse.failure(userId, time, null, DemonLogFailureReason.MISSING_LEVEL_ID));
            }

            if (levelId < 1 || levelId > 120000000) {
                return channel.createMessage(DemonLogResponse.failure(userId, time, levelId, DemonLogFailureReason.INVALID_LEVEL_ID));
            }
            if (message.getAttachments().size() < 2) {
                return channel.createMessage(DemonLogResponse.failure(userId, time, levelId, DemonLogFailureReason.MISSING_ATTACHMENTS));
            }
        } catch (Exception err) {
            LOGGER.error("Demon log handler encountered exception during pre-player processing: " + err);
            return channel.createMessage(DemonLogResponse.error(userId));
        }

        try {
            Player player;
            playerManager.acquire();
            try {
                if (!playerManager.hasPlayer(userId)) {
                    playerManager.addPlayer(new Player(userId));
                }
                player = playerManager.getPlayer(userId);
            } finally {
                playerManager.release();
            }
            player.acquire();

            try {
                if (player.isDisabled()) {
                    player.release();
                    return channel.createMessage(DemonLogResponse.failure(userId, time, levelId, DemonLogFailureReason.PLAYER_DISABLED));
                }
                if (player.hasCompleted(levelId)) {
                    player.release();
                    return channel.createMessage(DemonLogResponse.failure(userId, time, levelId, DemonLogFailureReason.ALREADY_COMPLETED));
                }
                if (player.hasCompletionOn((short) time.getDayOfYear())) {
                    player.release();
                    return channel.createMessage(DemonLogResponse.failure(userId, time, levelId, DemonLogFailureReason.ALREADY_SUBMITTED_TODAY));
                }
                player.addCompletion(new DemonCompletion((short) time.getDayOfYear(), levelId, null, false));
            } finally {
                player.release();
            }
            return channel.createMessage(DemonLogResponse.success(userId, time, levelId));
        } catch (Exception err) {
            LOGGER.error("Demon log handler encountered exception during player processing: " + err);
            return channel.createMessage(DemonLogResponse.error(userId));
        }
    }

}
