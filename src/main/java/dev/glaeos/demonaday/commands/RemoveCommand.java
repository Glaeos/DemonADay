package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.env.DiscordConstants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class RemoveCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final PlayerManager playerManager;

    public RemoveCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
            .name("remove")
            .description("ADMIN ONLY - Manually removes a demon completion.")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("user")
                    .description("The user of the player whose record is being removed.")
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
                    .description("The reason the record is being removed.")
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
            long userId = interaction.getInteraction().getUser().getId().asLong();
            boolean authenticated = false;
            for (long admin : DiscordConstants.ADMINS) {
                if (admin == userId) {
                    authenticated = true;
                    break;
                }
            }
            if (!authenticated) {
                interaction.reply("**You do not have permission to use this command.**").withEphemeral(true).subscribe();
                return;
            }

            if (interaction.getOption("user").isEmpty()) {
                interaction.reply("Missing user.").withEphemeral(true).subscribe();
                return;
            }
            if (interaction.getOption("user").get().getValue().isEmpty()) {
                interaction.reply("Missing user.").withEphemeral(true).subscribe();
                return;
            }
            User commandUser = interaction.getOption("user").get().getValue().get().asUser().block();
            if (commandUser == null) {
                interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
                return;
            }

            Player player;
            playerManager.acquire();
            try {
                if (!playerManager.hasPlayer(commandUser.getId().asLong())) {
                    playerManager.release();
                    interaction.reply("Could not find player for given user.").withEphemeral(true).subscribe();
                    return;
                }

                player = playerManager.getPlayer(commandUser.getId().asLong());
                if (player == null) {
                    interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
                    return;
                }
            } finally {
                playerManager.release();
            }

            if (interaction.getOption("level").isEmpty()) {
                interaction.reply("Missing level ID.").withEphemeral(true).subscribe();
                return;
            }
            if (interaction.getOption("level").get().getValue().isEmpty()) {
                interaction.reply("Missing level ID.").withEphemeral(true).subscribe();
                return;
            }
            int levelId = (int) interaction.getOption("level").get().getValue().get().asLong();
            if (levelId < 1 || levelId > 120000000) {
                interaction.reply("Invalid level ID. Should be between 1 and 120 million inclusive.").withEphemeral(true).subscribe();
                return;
            }

            String reason;
            if (interaction.getOption("reason").isEmpty()) {
                reason = "No reason given.";
            } else if (interaction.getOption("reason").get().getValue().isEmpty()) {
                interaction.reply("Reason given but empty.").withEphemeral(true).subscribe();
                return;
            } else {
                reason = interaction.getOption("reason").get().getValue().get().asString();
            }

            player.acquire();
            try {
                if (!player.hasCompleted(levelId)) {
                    player.release();
                    interaction.reply("Player has not submitted a completion with the given level ID.").withEphemeral(true).subscribe();
                    return;
                }
                DemonCompletion completion = player.getCompletion(levelId);
                player.removeCompletion(completion);
                player.release();
                interaction.reply("Player's record was successfully removed.").subscribe();

                Channel verifyChannel = interaction.getClient().getChannelById(Snowflake.of(DiscordConstants.VERIFY_CHANNEL)).block();
                if (verifyChannel == null) {
                    LOGGER.error("Failed to fetch channel for verification logs.");
                    return;
                }
                String time = DemonLogResponse.formatDayOfYear(LocalDate.ofYearDay(2000, completion.getDayOfYear()));
                verifyChannel.getRestChannel().createMessage("<@" + player.getUserId() + "> Your record for **" + time + "** as the level with ID **" + completion.getLevelId() + "** has been removed. Reason: \"***" + reason + "***\". <:rejected:1192248311831343244>").subscribe();
            } finally {
                player.release();
            }
        } catch (Exception err) {
            LOGGER.error("Remove command encountered exception: " + err);
            interaction.reply("Something went wrong processing your request. Get in touch with Glaeos").withEphemeral(true).subscribe();
        }
    }

}
