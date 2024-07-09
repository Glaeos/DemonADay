package dev.glaeos.demonaday.player;

import dev.glaeos.demonaday.util.Acquirable;
import dev.glaeos.demonaday.util.Encodable;
import dev.glaeos.demonaday.util.Toggleable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface PlayerManager extends Acquirable, Toggleable, Encodable {

    @NotNull Collection<Player> getPlayers();

    default boolean hasPlayer(long userId) {
        return getPlayers().stream().anyMatch(player -> player.getUserId() == userId);
    }

    default @Nullable Player getPlayer(long userId) {
        return getPlayers().stream().filter(player -> player.getUserId() == userId).findFirst().orElse(null);
    }

    void addPlayer(@NotNull Player player);

    void removePlayer(@NotNull Player player);

}
