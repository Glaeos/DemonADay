package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.DemonDifficulty;
import dev.glaeos.demonaday.env.DiscordConstant;
import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class CommandHelper {

    private CommandHelper() {

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean authenticate(@NotNull ChatInputInteractionEvent interaction) {
        long userId = interaction.getInteraction().getUser().getId().asLong();
        for (long admin : DiscordConstant.ADMINS) {
            if (admin == userId) {
                return true;
            }
        }
        interaction.reply("**You do not have permission to use this command.**").withEphemeral(true).subscribe();
        return false;
    }

    public static void replyWithError(@NotNull ChatInputInteractionEvent interaction) {
        interaction.reply("Something went wrong processing your request. Get in touch with Glaeos.")
            .withEphemeral(true)
            .subscribe();
    }

    public static @Nullable ApplicationCommandInteractionOptionValue getInteractionOption(@NotNull ChatInputInteractionEvent interaction,
                                                                                          @NotNull String name) {
        Optional<ApplicationCommandInteractionOption> option = interaction.getOption(name);
        if (option.isEmpty()) {
            interaction.reply("Missing " + name + ".").withEphemeral(true).subscribe();
            return null;
        }

        Optional<ApplicationCommandInteractionOptionValue> value = option.get().getValue();
        if (value.isEmpty()) {
            interaction.reply("Missing " + name + ".").withEphemeral(true).subscribe();
            return null;
        }
        return value.get();
    }

    public static @Nullable User getUserOption(@NotNull ChatInputInteractionEvent interaction) {
        ApplicationCommandInteractionOptionValue option = CommandHelper.getInteractionOption(interaction, "user");
        if (option == null) {
            return null;
        }

        User user = option.asUser().block();
        if (user == null) {
            CommandHelper.replyWithError(interaction);
            return null;
        }
        return user;
    }

    public static @Nullable Short getDayOption(@NotNull ChatInputInteractionEvent interaction) {
        ApplicationCommandInteractionOptionValue option = CommandHelper.getInteractionOption(interaction, "day");
        if (option == null) {
            return null;
        }

        long day = option.asLong();
        if (day < 1 || day > 366) {
            interaction.reply("Invalid day. Should be between 1 and 366 inclusive.").withEphemeral(true).subscribe();
        }
        return (short) day;
    }

    public static @Nullable Integer getLevelOption(@NotNull ChatInputInteractionEvent interaction) {
        ApplicationCommandInteractionOptionValue option = CommandHelper.getInteractionOption(interaction, "level");
        if (option == null) {
            return null;
        }

        int levelId = (int) option.asLong();
        if (levelId < 1 || levelId > 120000000) {
            interaction.reply("Invalid level ID. Should be between 1 and 120 million inclusive.").withEphemeral(true).subscribe();
            return null;
        }
        return levelId;
    }

    public static @Nullable DemonDifficulty getDifficultyOption(@NotNull ChatInputInteractionEvent interaction) {
        ApplicationCommandInteractionOptionValue option = CommandHelper.getInteractionOption(interaction, "difficulty");
        if (option == null) {
            return null;
        }

        try {
            return DemonDifficulty.valueOf(option.asString().toUpperCase());
        } catch (IllegalArgumentException err) {
            interaction.reply("Invalid difficulty. Should be one of: 'Easy', 'Medium', 'Hard', 'Insane' or 'Extreme'.").withEphemeral(true).subscribe();
            return null;
        }
    }

    public static @Nullable String getReasonOption(@NotNull ChatInputInteractionEvent interaction) {
        ApplicationCommandInteractionOptionValue option = CommandHelper.getInteractionOption(interaction, "reason");
        if (option == null) {
            return null;
        }
        return option.asString();
    }

    public static @Nullable Player getPlayer(@NotNull ChatInputInteractionEvent interaction, @NotNull PlayerManager manager, @NotNull User user) {
        manager.acquire();
        try {
            if (!manager.hasPlayer(user.getId().asLong())) {
                interaction.reply("Could not find player for given user.").withEphemeral(true).subscribe();
                return null;
            }

            Player player = manager.getPlayer(user.getId().asLong());
            if (player == null) {
                CommandHelper.replyWithError(interaction);
                return null;
            }
            return player;
        } finally {
            manager.release();
        }
    }

}
