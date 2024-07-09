package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.env.DiscordConstants;
import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import dev.glaeos.demonaday.messages.DemonLogResponse;
import dev.glaeos.demonaday.player.impl.DefaultPlayer;
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

public class AddCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final PlayerManager playerManager;

    public AddCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
            .name("add")
            .description("ADMIN ONLY - Manually adds a verified demon completion.")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("user")
                    .description("The user of the player whose record is being added.")
                    .type(ApplicationCommandOption.Type.USER.getValue())
                    .required(true)
                    .build())
            .addOption(ApplicationCommandOptionData.builder()
                    .name("day")
                    .description("The day of the year (1-366) when the player beat the level.")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
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
                    playerManager.addPlayer(new DefaultPlayer(commandUser.getId().asLong()));
                }
                player = playerManager.getPlayer(commandUser.getId().asLong());

                if (player == null) {
                    interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
                    return;
                }
            } finally {
                playerManager.release();
            }

            if (interaction.getOption("day").isEmpty()) {
                interaction.reply("Missing day.").withEphemeral(true).subscribe();
            }
            if (interaction.getOption("day").get().getValue().isEmpty()) {
                interaction.reply("Missing day.").withEphemeral(true).subscribe();
            }
            long day = interaction.getOption("day").get().getValue().get().asLong();
            if (day < 1 || day > 366) {
                interaction.reply("Invalid day. Should be between 1 and 366 inclusive.").withEphemeral(true).subscribe();
            }
            short dayOfYear = (short) day;

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

            if (interaction.getOption("difficulty").isEmpty()) {
                interaction.reply("Missing difficulty.").withEphemeral(true).subscribe();
                return;
            }
            if (interaction.getOption("difficulty").get().getValue().isEmpty()) {
                interaction.reply("Missing difficulty.").withEphemeral(true).subscribe();
                return;
            }
            String difficultyString = interaction.getOption("difficulty").get().getValue().get().asString().toUpperCase();
            if (!difficultyString.equals("EASY") && !difficultyString.equals("MEDIUM") && !difficultyString.equals("HARD") && !difficultyString.equals("INSANE") && !difficultyString.equals("EXTREME")) {
                interaction.reply("Invalid difficulty. Should be one of: 'Easy', 'Medium', 'Hard', 'Insane' or 'Extreme'.").withEphemeral(true).subscribe();
                return;
            }
            DemonDifficulty difficulty = DemonDifficulty.valueOf(difficultyString);
            String time = DemonLogResponse.formatDayOfYear(LocalDate.ofYearDay(2000, dayOfYear));

            player.acquire();
            if (player.hasCompleted(levelId)) {
                player.release();
                interaction.reply("Player already has a logged completion as the level with ID " + levelId).withEphemeral(true).subscribe();
                return;
            }
            if (player.hasCompletionOn(dayOfYear)) {
                player.release();
                interaction.reply("Player already has a logged completion for " + time).withEphemeral(true).subscribe();
                return;
            }

            DemonCompletion completion = new DemonCompletion(dayOfYear, levelId, difficulty, true);
            player.addCompletion(completion);
            player.release();

            interaction.reply("Successfully added record.").subscribe();
            Channel verifyChannel = interaction.getClient().getChannelById(Snowflake.of(DiscordConstants.VERIFY_CHANNEL)).block();
            if (verifyChannel == null) {
                LOGGER.error("Failed to fetch channel for verification logs.");
                return;
            }
            verifyChannel.getRestChannel().createMessage("<@" + player.getUserId() + "> Your record for **" + time + "** as the level with ID **" + levelId + "** has been added and verified with a demon difficulty of **" + difficulty.name().toLowerCase() + "**! <:verified:1192248314972872704>").subscribe();

        } catch (Exception err) {
            LOGGER.error("Add command encountered exception: " + err);
            interaction.reply("Something went wrong processing your request. Get in touch with Glaeos");
        }
    }

}
