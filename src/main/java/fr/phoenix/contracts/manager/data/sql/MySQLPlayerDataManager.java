package fr.phoenix.contracts.manager.data.sql;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.manager.data.PlayerDataManager;
import fr.phoenix.contracts.manager.data.sql.api.MySQLRequest;
import fr.phoenix.contracts.manager.data.sql.api.MySQLTableEditor;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MySQLPlayerDataManager extends PlayerDataManager {
    private final MySQLDataProvider provider;

    public MySQLPlayerDataManager(MySQLDataProvider provider) {
        this.provider = provider;
    }

    //TODO
    @Override
    public boolean hasAlreadyBeenConnected(OfflinePlayer player) {
        return false;
    }

    @Override
    public void loadAndRun(UUID uuid, Consumer<PlayerData> consumer) {
        players.put(uuid, new PlayerData(uuid));
        //To prevent infinite loops.
        provider.getResult("SELECT * FROM " + MySQLTableEditor.Table.PLAYERDATA + " WHERE uuid = '" + uuid + "';", (result) -> {
            try {
                if (result.next()) {
                    Contracts.sqlDebug("Loading data for: '" + uuid + "'...");
                    List<Contract> contracts = new ArrayList<>();
                    JsonArray contractsArray = new JsonParser().parse(result.getString("contracts")).getAsJsonArray();
                    for (JsonElement element : contractsArray) {
                        Contract contract = provider.getContractManager().get(UUID.fromString(element.getAsString()));
                        contracts.add(contract);
                    }
                    JsonArray middlemanContractsArray = new JsonParser().parse(result.getString("middleman_contracts")).getAsJsonArray();
                    List<Contract> middlemanContracts = new ArrayList<>();
                    for (JsonElement element : middlemanContractsArray) {
                        Contract contract = provider.getContractManager().get(UUID.fromString(element.getAsString()));
                        middlemanContracts.add(contract);
                    }

                    List<ContractReview> contractReviews = new ArrayList<>();
                    JsonArray reviewsArray = new JsonParser().parse(result.getString("reviews")).getAsJsonArray();

                    for (JsonElement element : reviewsArray) {
                        ContractReview review = new ContractReview(element.getAsJsonObject());
                        contractReviews.add(review);
                    }

                    PlayerData playerData = new PlayerData(uuid, result.getString("player_name"),
                            contracts, middlemanContracts, contractReviews);
                    //The data is cached in memory and is removed once it has not been used for 1 day.
                    players.put(uuid, playerData);
                    lastTimeUsed.put(uuid, System.currentTimeMillis());

                    //The consumer is here to tell what needs to be made after the data is loaded
                    consumer.accept(playerData);

                    long lastTimeLoggedOut = result.getLong("last_time_logged_out");
                    //If the player has been disconnected for more than 30 minutes, we send him a message to tell him how many contracts are waiting for him.
                    if (Contracts.plugin.configManager.sendMessageWhenLogin && System.currentTimeMillis() - lastTimeLoggedOut > 1000 * 60 * 30) {
                        Message.LOGIN_MESSAGE
                                .format("contracts", Contracts.plugin.dataProvider.getContractManager().getContracts()
                                        .stream()
                                        .filter(contract -> contract.getEnteringTime(ContractState.AWAITING_EMPLOYEE) > lastTimeLoggedOut)
                                        .collect(Collectors.toList()).size()).send(playerData.getPlayer());
                    }


                    Contracts.sqlDebug("Loaded saved data for: '" + uuid + "'!");
                    return;

                } else {
                    PlayerData playerData = new PlayerData(uuid);
                    consumer.accept(playerData);
                    Contracts.sqlDebug("Created player data for: '" + uuid + "' as no saved data was found.");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void save(PlayerData playerData) {
        lastTimeUsed.put(playerData.getUuid(), System.currentTimeMillis());
        MySQLTableEditor sql = new MySQLTableEditor(MySQLTableEditor.Table.PLAYERDATA, playerData.getUuid(), provider);
        Contracts.sqlDebug("Saving data for: '" + playerData.getUuid() + "'...");
        MySQLRequest request = new MySQLRequest(playerData.getUuid());
        request.addData("player_name", playerData.getPlayerName());
        request.addJSONArray("middleman_contracts", playerData.getMiddlemanContracts().stream().map(contract -> contract.getUUID().toString()).collect(Collectors.toList()));
        request.addJSONArray("contracts", playerData.getAllContracts().stream().map(contract -> contract.getUUID().toString()).collect(Collectors.toList()));
        JsonArray jsonArray = new JsonArray();
        playerData.getReviews().stream().forEach(review -> jsonArray.add(review.getAsJsonObject()));
        request.addData("reviews", jsonArray);
        request.addData("last_time_logged_out", System.currentTimeMillis());
        sql.updateData(request);
        Contracts.sqlDebug("Saved data for: " + playerData.getUuid());
    }
}



