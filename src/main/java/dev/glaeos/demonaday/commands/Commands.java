package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.player.PlayerManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class Commands {

    private Commands() {

    }

    public static @NotNull List<Command> getCommands(@NotNull PlayerManager playerManager) {
        List<Command> commands = new ArrayList<>();
        commands.add(new VerifyCommand(playerManager));
        commands.add(new RejectCommand(playerManager));
        commands.add(new AddCommand(playerManager));
        commands.add(new RemoveCommand(playerManager));
        commands.add(new PointsCommand(playerManager));
        commands.add(new CurrentStreakCommand(playerManager));
        commands.add(new BestStreakCommand(playerManager));
        return commands;
    }

}
