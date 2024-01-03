package dev.glaeos.demonaday.demons;

import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerManager {

    private final List<Player> players;

    private final Lock lock;

    public PlayerManager() {
        players = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public Lock getLock() {
        return lock;
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

    public void save(@NotNull String filename) throws IOException {
        checkNotNull(filename);
        List<Byte> data = new ArrayList<>();

        lock.lock();
        for (Player player : players) {
            player.getLock().lock();
            data.addAll(player.serialize());
            player.getLock().unlock();
        }

        File file = new File(filename);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);

        char[] finalData = new char[data.size()];
        for (int i = 0; i < data.size(); i++) {
            finalData[i] = (char) data.get(i).byteValue();
        }

        writer.write(finalData);
        writer.close();
        lock.unlock();
    }

}
