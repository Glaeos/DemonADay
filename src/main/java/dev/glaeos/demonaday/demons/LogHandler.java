package dev.glaeos.demonaday.demons;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkNotNull;

public class LogHandler {

    private final RecordManager manager;

    public static final String[] daySuffixes = {"0th", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th",
            "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd",
            "23rd", "24th", "25th", "26th", "27th", "28th", "29th", "30th", "31st"};

    public LogHandler(@NotNull RecordManager recordManager) {
        checkNotNull(recordManager);
        manager = recordManager;
    }

    public Publisher<?> handleMessage(Message message) {
        MessageChannel channel = message.getChannel().block();
        if (channel == null) {
            return Mono.empty();
        }
        if (message.getAuthor().isEmpty()) {
            return Mono.empty();
        }

        LocalDate time = LocalDate.now(ZoneId.of("America/Chicago"));
        String day = time.format(DateTimeFormatter.ofPattern("MMMM")) + " " + daySuffixes[time.getDayOfMonth()];
        String ping = "<@" + message.getAuthor().get().getId().asLong() + "> ";

        String content = message.getContent().stripLeading().toLowerCase();
        if (content.startsWith("id:")) {
            content = content.substring(3).stripLeading();
        }

        int i;
        for (i = 0; i < content.length(); i++) {
            if (!Character.isDigit(content.charAt(i))) {
                break;
            }
        }
        content = content.substring(0, i);

        long levelId;
        try {
            levelId = Long.parseLong(content);
        } catch (NumberFormatException err) {
            return channel.createMessage(ping + "Message appears to be missing level ID. Get in touch with Glaeos or Trubactor.");
        }

        if (levelId < 1 || levelId > 120000000) {
            return channel.createMessage(ping + "Not a valid level ID (should be between 1 and 120 million). Get in touch with Glaeos or Trubactor.");
        }
        if (message.getAttachments().size() < 2) {
            return channel.createMessage(ping + "Message missing screenshots. Get in touch with Glaeos or Trubactor.");
        }

        return channel.createMessage(ping + "**Your demon record for " + day + " has been logged as the level with ID " + levelId + ". Verification pending.**");
    }

}
