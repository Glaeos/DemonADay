package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.DiscordConstants;
import dev.glaeos.demonaday.Env;
import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class VerifyCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final PlayerManager playerManager;

    public VerifyCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
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
    public ApplicationCommandRequest getAppCommand() {
        return appCommand;
    }

    public void handle(ChatInputInteractionEvent interaction) {
        //interaction.deferReply().withEphemeral(true).subscribe();
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
            playerManager.acquire();
            if (!playerManager.hasPlayer(commandUser.getId().asLong())) {
                playerManager.release();
                interaction.reply("Could not find player with given username.").withEphemeral(true).subscribe();
                return;
            }
            Player player = playerManager.getPlayer(commandUser.getId().asLong());
            playerManager.release();

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

            player.acquire();
            if (!player.hasCompleted(levelId)) {
                player.release();
                interaction.reply("Player has not logged a completion with the given level ID.").withEphemeral(true).subscribe();
                return;
            }
            DemonCompletion completion = player.getCompletion(levelId);
            if (completion.isVerified()) {
                player.release();
                interaction.reply("Player's completion is already verified.").withEphemeral(true).subscribe();
                return;
            }
            completion.setDifficulty(difficulty);
            completion.verify();
            player.release();
            interaction.reply("Player's record was successfully verified.").subscribe();

        } catch (Exception err) {
            LOGGER.error("Verify command encountered exception: " + err);
            interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
        }
    }

}
