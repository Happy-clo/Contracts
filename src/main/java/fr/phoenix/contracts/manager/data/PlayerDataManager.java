package fr.phoenix.contracts.manager.data;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public abstract class PlayerDataManager {
    protected final Map<UUID, PlayerData> players = new HashMap<>();
    protected final Map<UUID, Long> lastTimeUsed = new HashMap<>();


    public PlayerData get(UUID uuid) {
        return players.get(uuid);
    }

    public List<PlayerData> getAll() {
        return new ArrayList<>(players.values());
    }

    public Collection<PlayerData> getAllPlayerData() {
        return players.values();
    }

    public Set<UUID> getAllUUID() {
        return new HashSet(players.keySet());
    }

    public long getLastTimeUsed(UUID uuid) {
        return lastTimeUsed.get(uuid);
    }

    public void refreshLastTimeUsed(PlayerData playerData) {
        lastTimeUsed.put(playerData.getUuid(), System.currentTimeMillis());
    }

    public void remove(UUID uuid) {
        lastTimeUsed.remove(uuid);
        players.remove(uuid);
    }

    public boolean has(UUID uuid) {
        return players.containsKey(uuid);
    }

    public abstract boolean hasAlreadyBeenConnected(OfflinePlayer player);

    //We load without doing anything.
    public void load(UUID uuid) {
        loadAndRun(uuid, (playerData) -> {
        });
    }

    /**
     * Abstraction that is useful for Async management for SQL request.
     * Loads the playerData and runs the consumer method after it.
     * Similar to .then() in javaScript for promises.
     */
    public abstract void loadAndRun(UUID uuid, Consumer<PlayerData> consumer);

    public abstract void save(PlayerData playerData);

    public void load() {
        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(player -> load(player.getUniqueId()));
    }

    public void clear() {
        players.clear();
        lastTimeUsed.clear();
    }
}
