package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointsCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final PlayerManager playerManager;

    public PointsCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final ApplicationCommandRequest appCommand = ApplicationCommandRequest.builder()
            .name("points")
            .description("Calculates your current points for all your completions so far.")
            .build();

    @Override
    public ApplicationCommandRequest getAppCommand() {
        return appCommand;
    }

    @Override
    public void handle(ChatInputInteractionEvent interaction) {
        try {
            long userId = interaction.getInteraction().getUser().getId().asLong();
            playerManager.acquire();
            if (!playerManager.hasPlayer(userId)) {
                playerManager.release();
                interaction.reply("Could not find your player. Have you submitted any demon completions yet?").subscribe();
                return;
            }
            Player player = playerManager.getPlayer(userId);
            playerManager.release();

            player.acquire();
            interaction.reply("You currently have **" + player.calculatePoints() + "** points. Keep it up!").subscribe();
            player.release();
        } catch (Exception err) {
            LOGGER.error("Points command encountered exception: " + err);
            interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
        }
    }

}
