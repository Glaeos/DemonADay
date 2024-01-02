package dev.glaeos.demonaday.demons;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerManager {

    private final List<Player> players;

    public PlayerManager() {
        players = new ArrayList<>();
    }

    public boolean hasPlayer(long userId) {
        return players.stream().anyMatch(player -> player.getUserId() == userId);
    }

    public @NotNull Player getPlayer(long userId) throws IllegalArgumentException {
        for (Player player : players) {
            if (player.getUserId() == userId) {
                return player;
            }
        }
        throw new IllegalArgumentException("player not found");
    }

    public void addPlayer(@NotNull Player player) throws IllegalArgumentException {
        checkNotNull(player);
        if (hasPlayer(player.getUserId())) {
            throw new IllegalArgumentException("cannot add player who is already managed by this manager");
        }
        players.add(player);
    }

}
