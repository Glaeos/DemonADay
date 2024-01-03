package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.DemonCompletion;
import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.function.Function;

public class VerifyCommand implements Command {

    private static final long[] ADMINS = {621042431822921729L, 681328500807958629L};

    private final PlayerManager playerManager;

    public VerifyCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
            .name("verify")
            .description("ADMIN ONLY - Verifies a demon completion.")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("userId")
                    .description("The user ID of the player whose record is being verified.")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(ApplicationCommandOptionData.builder()
                    .name("levelId")
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
        interaction.deferReply().withEphemeral(true).subscribe();
        long userId = interaction.getInteraction().getUser().getId().asLong();
        boolean authenticated = false;
        for (long admin : ADMINS) {
            if (admin == userId) {
                authenticated = true;
                break;
            }
        }
        if (!authenticated) {
            interaction.reply("**You do not have permission to use this command.**").withEphemeral(true);
            return;
        }

        if (interaction.getOption("userId").isEmpty()) {
            interaction.reply("Missing user ID.").withEphemeral(true);
            return;
        }
        if (interaction.getOption("userId").get().getValue().isEmpty()) {
            interaction.reply("Missing user ID.").withEphemeral(true);
            return;
        }
        long commandUserId = interaction.getOption("userId").get().getValue().get().asLong();
        if (commandUserId < 1) {
            interaction.reply("Invalid user ID.").withEphemeral(true);
            return;
        }
        playerManager.getLock().lock();
        if (!playerManager.hasPlayer(userId)) {
            playerManager.getLock().unlock();
            interaction.reply("Could not find player with given user ID.").withEphemeral(true);
            return;
        }
        Player player = playerManager.getPlayer(userId);
        playerManager.getLock().unlock();

        if (interaction.getOption("levelId").isEmpty()) {
            interaction.reply("Missing level ID.").withEphemeral(true);
            return;
        }
        if (interaction.getOption("levelId").get().getValue().isEmpty()) {
            interaction.reply("Missing level ID.").withEphemeral(true);
            return;
        }
        int levelId = (int) interaction.getOption("levelId").get().getValue().get().asLong();
        if (levelId < 1 || levelId > 120000000) {
            interaction.reply("Invalid level ID. Should be between 1 and 120 million inclusive.").withEphemeral(true);
            return;
        }

        if (interaction.getOption("difficulty").isEmpty()) {
            interaction.reply("Missing difficulty.").withEphemeral(true);
            return;
        }
        if (interaction.getOption("difficulty").get().getValue().isEmpty()) {
            interaction.reply("Missing difficulty.").withEphemeral(true);
            return;
        }
        String difficultyString = interaction.getOption("difficulty").get().getValue().get().asString().toUpperCase();
        if (!difficultyString.equals("EASY") && !difficultyString.equals("MEDIUM") && !difficultyString.equals("HARD") && !difficultyString.equals("INSANE") && !difficultyString.equals("EXTREME")) {
            interaction.reply("Invalid difficulty. Should be one of: 'Easy', 'Medium', 'Hard', 'Insane' or 'Extreme'.").withEphemeral(true);
            return;
        }
        DemonDifficulty difficulty = DemonDifficulty.valueOf(difficultyString);

        player.getLock().lock();
        if (!player.hasCompleted(levelId)) {
            player.getLock().unlock();
            interaction.reply("Player has not logged a completion with the given level ID.").withEphemeral(true);
            return;
        }
        DemonCompletion completion = player.getCompletion(levelId);
        if (completion.isVerified()) {
            player.getLock().unlock();
            interaction.reply("Player's completion is already verified.").withEphemeral(true);
            return;
        }
        completion.setDifficulty(difficulty);
        completion.verify();
        player.getLock().unlock();
        interaction.reply("Player's record was successfully verified.");
    }

}
