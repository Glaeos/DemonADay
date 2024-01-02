package dev.glaeos.demonaday.commands;

import java.util.ArrayList;
import java.util.List;

public class Commands {

    public static final List<Command> COMMANDS = new ArrayList<>();

    static {
        COMMANDS.add(new TestCommand());
    }

}
