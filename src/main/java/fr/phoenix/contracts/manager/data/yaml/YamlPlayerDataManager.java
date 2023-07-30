package fr.phoenix.contracts.manager.data.yaml;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.manager.data.DataProvider;
import fr.phoenix.contracts.manager.data.PlayerDataManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class YamlPlayerDataManager extends PlayerDataManager {
    private final DataProvider provider;

    public YamlPlayerDataManager(DataProvider provider) {
        this.provider = provider;
    }

    public boolean hasAlreadyBeenConnected(OfflinePlayer player) {
        if (players.containsKey(player.getUniqueId()))
            return true;
        File file = new File(Contracts.plugin.getDataFolder(), "userdata/" + player.getUniqueId() + ".yml");
        if (file.exists())
            return true;
        return false;
    }


    @Override
    public void loadAndRun(UUID uuid, Consumer<PlayerData> consumer) {
        ConfigFile config = new ConfigFile("/userdata", uuid.toString());
        PlayerData playerData = new PlayerData(uuid, config.getConfig());
        players.put(playerData.getUuid(), playerData);
        lastTimeUsed.put(playerData.getUuid(), System.currentTimeMillis());
        long lastTimeLoggedOut = config.getConfig().getLong("last-time-logged-out");
        if (Contracts.plugin.configManager.sendMessageWhenLogin && playerData.getPlayer() != null && playerData.getPlayer().isOnline())
            Message.LOGIN_MESSAGE
                    .format("contracts", Contracts.plugin.dataProvider.getContractManager().getContracts()
                            .stream()
                            .filter(contract -> contract.getEnteringTime(ContractState.AWAITING_EMPLOYEE) > lastTimeLoggedOut)
                            .collect(Collectors.toList()).size()).send(playerData.getPlayer());
        consumer.accept(playerData);
    }

    @Override
    public void save(PlayerData playerData) {
        lastTimeUsed.put(playerData.getUuid(), System.currentTimeMillis());
        ConfigFile config = new ConfigFile("/userdata", playerData.getUuid().toString());
        for (String key : config.getConfig().getKeys(true))
            config.getConfig().set(key, null);
        config.getConfig().set("player-name", playerData.getPlayerName());
        ConfigurationSection section = config.getConfig().createSection("reviews");
        //Set the reviews
        for (ContractReview review : playerData.getReviews())
            review.save(section);

        //Set the contracts
        List<String> list = playerData.getAllContracts().stream().map(contract -> contract.getUUID().toString()).collect(Collectors.toList());
        config.getConfig().set("contracts", list);

        //Set the middleman contracts
        List<String> middlemanList = playerData.getMiddlemanContracts().stream().map(contract -> contract.getUUID().toString()).collect(Collectors.toList());
        config.getConfig().set("middleman-contracts", middlemanList);

        config.getConfig().set("last-time-logged-out", System.currentTimeMillis());

        config.save();
    }

}
