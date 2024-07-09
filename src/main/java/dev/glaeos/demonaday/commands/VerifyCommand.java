package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.DemonCalculator;
import dev.glaeos.demonaday.demons.Streak;
import dev.glaeos.demonaday.env.DiscordConstant;
import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogResponse;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class VerifyCommand implements Command {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final @NotNull PlayerManager playerManager;

    public VerifyCommand(@NotNull PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final @NotNull ApplicationCommandRequest APP_COMMAND = ApplicationCommandRequest.builder()
            .name("verify")
            .description("ADMIN ONLY - Verifies a demon completion.")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("user")
                    .description("The user of the player whose record is being verified.")
                    .type(ApplicationCommandOption.Type.USER.getValue())
                    .required(true)
                    .build())
            .addOption(ApplicationCommandOptionData.builder()
                    .name("level")
                    .description("The level ID of the level the player beat.")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(ApplicationCommandOptionData.builder()
                    .name("difficulty")
                    .description("The demon difficulty of the level the player beat.")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    @Override
    public @NotNull ApplicationCommandRequest getAppCommand() {
        return APP_COMMAND;
    }

    @Override
    public void handle(@NotNull ChatInputInteractionEvent interaction) {
        try {
            if (!CommandHelper.authenticate(interaction)) {
                return;
            }

            User user = CommandHelper.getUserOption(interaction);
            if (user == null) {
                return;
            }

            Integer levelId = CommandHelper.getLevelOption(interaction);
            if (levelId == null) {
                return;
            }

            DemonDifficulty difficulty = CommandHelper.getDifficultyOption(interaction);
            if (difficulty == null) {
                return;
            }

            Player player = CommandHelper.getPlayer(interaction, playerManager, user);
            if (player == null) {
                return;
            }

            player.acquire();
            try {
                if (!player.hasCompleted(levelId)) {
                    interaction.reply("Player has not logged a completion with the given level ID.").withEphemeral(true).subscribe();
                    return;
                }

                DemonCompletion completion = player.getCompletion(levelId);
                if (completion == null) {
                    interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
                    return;
                }
                if (completion.isEnabled()) {
                    player.release();
                    interaction.reply("Player's completion is already verified.").withEphemeral(true).subscribe();
                    return;
                }

                int prevPoints = DemonCalculator.calculatePoints(player.getCompletions());
                completion.setDifficulty(difficulty);
                completion.enable();
                int gainedPoints = DemonCalculator.calculatePoints(player.getCompletions()) - prevPoints;
                interaction.reply("Player's record was successfully verified.").subscribe();

                Channel verifyChannel = interaction.getClient().getChannelById(Snowflake.of(DiscordConstant.VERIFY_CHANNEL)).block();
                if (verifyChannel == null) {
                    LOGGER.error("Failed to fetch channel for verification logs.");
                    return;
                }

                String time = DemonLogResponse.formatDayOfYear(LocalDate.ofYearDay(2024, completion.getDayOfYear()));
                Streak streak = DemonCalculator.findStreakIncluding(player.getCompletions(), (short) LocalDate.now(DiscordConstant.TIMEZONE).getDayOfYear());
                int streakSize = streak == null ? 0 : streak.getSize();
                verifyChannel.getRestChannel().createMessage("<@" + player.getUserId() + "> Your record for **" + time + "** as the level with ID **" + levelId + "** has been verified with a demon difficulty of **" + difficulty.name().toLowerCase() + "**! You have gained **" + gainedPoints + "** point" + (gainedPoints == 1 ? "" : "s") + ", and are currently on a streak of **" + (streakSize + 1) + "** day" + (streakSize == 0 ? "" : "s") + "! <:verified:1192248314972872704>").subscribe();
            } finally {
                player.release();
            }
        } catch (Exception err) {
            LOGGER.error("Verify command encountered exception", err);
            interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
        }
    }

}
