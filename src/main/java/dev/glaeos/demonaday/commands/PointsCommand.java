package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.Player;
import dev.glaeos.demonaday.demons.PlayerManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
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
            .addOption(ApplicationCommandOptionData.builder()
                    .name("user")
                    .description("The user whose points you want to view.")
                    .type(ApplicationCommandOption.Type.USER.getValue())
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

            boolean isSelf;
            long commandUserId;
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

            playerManager.acquire();
            if (!playerManager.hasPlayer(commandUserId)) {
                playerManager.release();
                String failedMessage;
                if (isSelf) {
                    failedMessage = "Could not find your player. Have you submitted any demon completions yet?";
                } else {
                    failedMessage = "Could not find the player for the given user. Have they submitted any demon completions yet?";
                }
                interaction.reply(failedMessage).subscribe();
                return;
            }
            Player player = playerManager.getPlayer(commandUserId);
            playerManager.release();

            player.acquire();
            String message;
            if (isSelf) {
                message = "You currently have **" + player.calculatePoints() + "** points. Keep it up!";
            } else {
                message = "This player currently has **" + player.calculatePoints() + "** points.";
            }
            player.release();
            interaction.reply(message).subscribe();
        } catch (Exception err) {
            LOGGER.error("Points command encountered exception: " + err);
            interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.").subscribe();
        }
    }

}
