package dev.glaeos.demonaday;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Env {

    public static final String TOKEN;

    public static final long GUILD;

    static {
        File f = new File(".env");
        Scanner reader;
        try {
            reader = new Scanner(f);
        } catch (FileNotFoundException err) {
            throw new RuntimeException(err);
        }

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
            throw new RuntimeException(".env does not contain token");
        }
        if (guildLine == null) {
            throw new RuntimeException(".env does not contain guild");
        }
        TOKEN = tokenLine.substring(6);
        GUILD = Long.parseLong(guildLine.substring(6));
    }

}
