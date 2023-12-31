package dev.glaeos.demonaday.commands;

import dev.glaeos.demonaday.demons.PlayerManager;

import java.util.ArrayList;
import java.util.List;

public class Commands {

    public final List<Command> COMMANDS;

    public Commands(PlayerManager playerManager) {
        COMMANDS = new ArrayList<>();
        COMMANDS.add(new VerifyCommand(playerManager));
        COMMANDS.add(new RejectCommand(playerManager));
        COMMANDS.add(new AddCommand(playerManager));
        COMMANDS.add(new RemoveCommand(playerManager));
        COMMANDS.add(new PointsCommand(playerManager));
    }

}
