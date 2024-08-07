package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.DemonCalculator;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointsCommand implements Command {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);

    private final @NotNull PlayerManager playerManager;

    public PointsCommand(@NotNull PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private static final @NotNull ApplicationCommandRequest APP_COMMAND = ApplicationCommandRequest.builder()
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
    public @NotNull ApplicationCommandRequest getAppCommand() {
        return APP_COMMAND;
    }

    @Override
    public void handle(@NotNull ChatInputInteractionEvent interaction) {
        try {
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
                if (isSelf) {
                    message = "You currently have **" + DemonCalculator.calculatePoints(player.getCompletions()) + "** points. Keep it up!";
                } else {
                    message = "This player currently has **" + DemonCalculator.calculatePoints(player.getCompletions()) + "** points.";
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
