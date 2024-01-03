package dev.glaeos.demonaday.demons;

import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerManager {

    private final List<Player> players;

    private final Semaphore lock;

    public PlayerManager() {
        players = new ArrayList<>();
        lock = new Semaphore(1);
    }

    public void acquire() throws InterruptedException {
        lock.acquire();
    }

    public void release() {
        lock.release();
    }

    public List<Player> getPlayers() {
        return players;
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

    public void save(@NotNull String filename) throws IOException, InterruptedException {
        checkNotNull(filename);
        List<Byte> data = new ArrayList<>();

        acquire();
        for (Player player : players) {
            System.out.println(player.getUserId());
            player.acquire();
            data.addAll(player.serialize());
            player.release();
        }
        System.out.println(data);

        File file = new File(filename);
        file.createNewFile();
        FileOutputStream writer = new FileOutputStream(file);

        byte[] finalData = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            finalData[i] = data.get(i);
        }

        writer.write(finalData);
        writer.close();
        release();
    }

}
