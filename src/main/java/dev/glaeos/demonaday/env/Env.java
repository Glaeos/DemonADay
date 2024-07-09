package dev.glaeos.demonaday.env;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class Env {

    public static final @NotNull String TOKEN;

    public static final long GUILD;

    static {
        InputStream envStream = ClassLoader.getSystemResourceAsStream(".env");
        if (envStream == null) {
            throw new NullPointerException("Failed to find .env");
        }

        Scanner reader = new Scanner(envStream);
        String tokenLine = null;
        String guildLine = null;
        while ((tokenLine == null || guildLine == null) && reader.hasNextLine()) {
            String line = reader.nextLine();
            if (line.startsWith("TOKEN=")) {
                tokenLine = line;
                continue;
            }
            if (line.startsWith("GUILD=")) {
                guildLine = line;
            }
        }

        reader.close();
        if (tokenLine == null) {
            throw new IllegalArgumentException(".env does not contain token");
        }
        if (guildLine == null) {
            throw new IllegalArgumentException(".env does not contain guild");
        }
        TOKEN = tokenLine.substring(6);
        GUILD = Long.parseLong(guildLine.substring(6));
    }

}
