package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.DemonCalculator;
import dev.glaeos.demonaday.demons.Streak;
import dev.glaeos.demonaday.env.DiscordConstant;
import dev.glaeos.demonaday.messages.DemonLogResponse;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class CurrentStreakCommand implements Command {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final @NotNull PlayerManager playerManager;

    public CurrentStreakCommand(@NotNull PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final @NotNull ApplicationCommandRequest APP_COMMAND = ApplicationCommandRequest.builder()
        .name("currentStreak")
        .description("Calculates a player's current streak.")
        .addOption(ApplicationCommandOptionData.builder()
            .name("user")
            .description("The user whose current streak you want to view.")
            .type(ApplicationCommandOption.Type.USER.getValue())
            .required(false)
            .build())
        .build();

    @Override
    public @NotNull ApplicationCommandRequest getAppCommand() {
        return APP_COMMAND;
    }

    @Override
    public void handle(@NotNull ChatInputInteractionEvent interaction) {
        try {
            LocalDate time = LocalDate.now(DiscordConstant.TIMEZONE);
            long userId = interaction.getInteraction().getUser().getId().asLong();

            boolean isSelf;
            long commandUserId;
            // TODO: kys
            if (interaction.getOption("user").isEmpty()) {
                isSelf = true;
                commandUserId = userId;
            } else {
                isSelf = false;
                if (interaction.getOption("user").get().getValue().isEmpty()) {
                    interaction.reply("User given but value missing.").withEphemeral(true).subscribe();
                    return;
                }
                commandUserId = interaction.getOption("user").get().getValue().get().asUser().block().getId().asLong();
            }

            Player player;
            playerManager.acquire();
            try {
                if (!playerManager.hasPlayer(commandUserId)) {
                    String failedMessage;
                    if (isSelf) {
                        failedMessage = "Could not find your player. Have you submitted any demon completions yet?";
                    } else {
                        failedMessage = "Could not find the player for the given user. Have they submitted any demon completions yet?";
                    }
                    interaction.reply(failedMessage).subscribe();
                    return;
                }

                player = playerManager.getPlayer(commandUserId);
                if (player == null) {
                    CommandHelper.replyWithError(interaction);
                    return;
                }
            } finally {
                playerManager.release();
            }

            String message;
            player.acquire();
            try {
                Streak streak = DemonCalculator.findLongestStreak(player.getCompletions());
                if (streak == null) {
                    message = "Could not find a streak. Has this player submitted any demon completions yet?";
                } else if (isSelf) {
                    String day = DemonLogResponse.formatDayOfYear(LocalDate.ofYearDay(2024, streak.startDay()));
                    message = "You are currently on a streak of **" + streak.getSize() + "** days since **" + day + "**. Keep it up, don't forget to log a completion tomorrow!";
                } else {
                    message = "This player is currently on a streak of **" + DemonCalculator.findStreakIncluding(player.getCompletions(), (short) time.getDayOfYear()) + "** days.";
                }
            } finally {
                player.release();
            }
            interaction.reply(message).subscribe();
        } catch (Exception err) {
            LOGGER.error("Points command encountered exception", err);
            CommandHelper.replyWithError(interaction);
        }
    }

}
