package dev.glaeos.demonaday.responses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkNotNull;

public class DemonLogResponse {

    private static final String[] DAY_SUFFIXES = {"0th", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th",
            "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd",
            "23rd", "24th", "25th", "26th", "27th", "28th", "29th", "30th", "31st"};

    protected static @NotNull String formatDayOfYear(@NotNull LocalDate time) {
        return time.format(DateTimeFormatter.ofPattern("MMMM")) + " " + DAY_SUFFIXES[time.getDayOfMonth()];
    }

    protected static @NotNull String formatPing(long userId) {
        return "<@" + userId + ">";
    }

    public static @NotNull String failure(long userId, @NotNull LocalDate time, @Nullable Integer levelId, @NotNull DemonLogFailureReason reason) {
        checkNotNull(reason);
        checkNotNull(time);

        String response;
        switch (reason) {
            case MISSING_LEVEL_ID -> response = "Message appears to be missing a valid level ID. Get in touch with Glaeos or Trubactor.";
            case INVALID_LEVEL_ID -> response = "Not a valid level ID (should be between 1 and 120 million). Get in touch with Glaeos or Trubactor.";
            case MISSING_ATTACHMENTS -> response = "Message missing screenshots. Get in touch with Glaeos or Trubactor.";
            case PLAYER_DISABLED -> response = "Your player account is currently disabled either due to an error with the bot or intentionally, therefore you cannot log any new demon completions until it is enabled. Get in touch with Glaeos or Trubactor.";
            case ALREADY_COMPLETED -> response = "You have already logged a completion as the level with ID " + levelId + ". Get in touch with Glaeos or Trubactor.";
            case ALREADY_SUBMITTED_TODAY -> response = "You have already logged a completion for " + formatDayOfYear(time) + ". Try again later.";
            default -> response = "Something went wrong processing your request. Get in touch with Glaeos.";
        }
        return formatPing(userId) + " " + response;
    }

    public static @NotNull String success(long userId, @NotNull LocalDate time, long levelId) {
        checkNotNull(time);
        return formatPing(userId) + " **Your demon completion for " + formatDayOfYear(time) + " has been logged as the level with ID " + levelId + ". Verification pending.**";
    }

    public static @NotNull String error(long userId) {
        return formatPing(userId) + " Something went wrong processing your request. Get in touch with Glaeos.";
    }

}
