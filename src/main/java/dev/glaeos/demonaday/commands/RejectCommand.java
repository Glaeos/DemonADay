package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.env.DiscordConstant;
import dev.glaeos.demonaday.demons.DemonCompletion;
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

public class RejectCommand implements Command {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final @NotNull PlayerManager playerManager;

    public RejectCommand(@NotNull PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final @NotNull ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
            .name("reject")
            .description("ADMIN ONLY - Rejects a demon completion.")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("user")
                    .description("The user of the player whose record is being rejected.")
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
                    .name("reason")
                    .description("The reason the record is being rejected.")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(false)
                    .build())
            .build();

    @Override
    public ApplicationCommandRequest getAppCommand() {
        return appCommand;
    }

    @Override
    public void handle(ChatInputInteractionEvent interaction) {
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

            String reason = CommandHelper.getReasonOption(interaction);
            if (reason == null) {
                return;
            }

            Player player = CommandHelper.getPlayer(interaction, playerManager, user);
            if (player == null) {
                return;
            }

            player.acquire();
            try {
                if (!player.hasCompleted(levelId)) {
                    interaction.reply("Player has not submitted a completion with the given level ID.").withEphemeral(true).subscribe();
                    return;
                }
                DemonCompletion completion = player.getCompletion(levelId);
                if (completion == null) {
                    interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
                    return;
                }
                if (completion.isEnabled()) {
                    interaction.reply("Player's completion is already verified. Use /remove to remove a verified completion.").withEphemeral(true).subscribe();
                    return;
                }

                player.removeCompletion(completion);
                interaction.reply("Player's record was successfully rejected.").subscribe();

                Channel verifyChannel = interaction.getClient().getChannelById(Snowflake.of(DiscordConstant.VERIFY_CHANNEL)).block();
                if (verifyChannel == null) {
                    LOGGER.error("Failed to fetch channel for verification logs.");
                    return;
                }
                String time = DemonLogResponse.formatDayOfYear(LocalDate.ofYearDay(2000, completion.getDayOfYear()));
                verifyChannel.getRestChannel().createMessage("<@" + player.getUserId() + "> Your record for **" + time + "** as the level with ID **" + completion.getLevelId() + "** has been rejected. Reason: \"***" + reason + "***\". Get in touch with Glaeos or Trubactor. <:rejected:1192248311831343244>").subscribe();
            } finally {
                player.release();
            }
        } catch (Exception err) {
            LOGGER.error("Reject command encountered exception", err);
            CommandHelper.replyWithError(interaction);
        }
    }

}
