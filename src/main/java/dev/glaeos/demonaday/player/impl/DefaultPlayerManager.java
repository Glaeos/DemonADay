package dev.glaeos.demonaday.player.impl;

import dev.glaeos.demonaday.player.Player;
import dev.glaeos.demonaday.player.PlayerManager;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultPlayerManager implements PlayerManager {

    private final @NotNull Lock lock;
    private boolean enabled;
    private final @NotNull List<Player> players;

    public DefaultPlayerManager() {
        this.lock = new ReentrantLock();
        this.enabled = true;
        this.players = new ArrayList<>();
    }

    public static @NotNull DefaultPlayerManager load(@NotNull ByteBuf buffer) {
        DefaultPlayerManager manager = new DefaultPlayerManager();
        manager.acquire();
        while (buffer.readableBytes() > 0) {
            manager.addPlayer(DefaultPlayer.load(buffer));
        }
        manager.release();
        return manager;
    }

    @Override
    public void acquire() {
        lock.lock();
    }

    @Override
    public void release() {
        lock.unlock();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public @NotNull Collection<Player> getPlayers() {
        return players;
    }

    @Override
    public void addPlayer(@NotNull Player player) throws IllegalArgumentException {
        if (!enabled) {
            throw new IllegalStateException("Cannot add player to disabled player manager");
        }
        if (hasPlayer(player.getUserId())) {
            throw new IllegalArgumentException("Cannot add player who is already managed by this manager");
        }
        players.add(player);
    }

    @Override
    public void removePlayer(@NotNull Player player) {
        if (!enabled) {
            throw new IllegalStateException("Cannot remove player from disabled player manager");
        }
        players.remove(player);
    }

    @Override
    public void encode(@NotNull ByteBuf buffer) {
        lock.lock();
        try {
            for (Player player : players) {
                player.acquire();
                try {
                    player.encode(buffer);
                } finally {
                    player.release();
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
