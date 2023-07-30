package fr.phoenix.contracts.manager.data.sql;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.manager.data.ContractManager;
import fr.phoenix.contracts.manager.data.sql.api.MySQLRequest;
import fr.phoenix.contracts.manager.data.sql.api.MySQLTableEditor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.UUID;

public class MySQLContractManager extends ContractManager {
    private final MySQLDataProvider provider;

    public MySQLContractManager(MySQLDataProvider provider) {
        this.provider = provider;
    }

    /**
     * Loads all the contracts into the RAM.
     */
    @Override
    public void load() {
        provider.getResult("SELECT * FROM " + MySQLTableEditor.Table.CONTRACTS + ";", (result) -> {
            try {
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    //If the data couldn't be loaded for more than 2 seconds its probably due to a server crash and we load the old data
                    Contracts.sqlDebug("Loading contract data for: '" + uuid + "'...");
                    JsonObject object = new JsonParser().parse(result.getString("contract")).getAsJsonObject();
                    ContractType contractType = ContractType.valueOf(object.get("contract-type").getAsString());
                    contracts.put(uuid, contractType.loadFromJson(object));
                    Contracts.sqlDebug("Loaded saved contract data for: '" + uuid + "'!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void loadContract(UUID uuid) {
        provider.getResult("SELECT * FROM " + MySQLTableEditor.Table.CONTRACTS + " WHERE uuid = '" + uuid + "';", (result) -> {
            try {
                if (!result.next())
                    contracts.remove(uuid);
                else {
                    Contracts.sqlDebug("Loading contract data for: '" + uuid + "'...");
                    JsonObject object = new JsonParser().parse(result.getString("contract")).getAsJsonObject();
                    ContractType contractType = ContractType.valueOf(object.get("contract-type").getAsString());
                    contracts.put(uuid, contractType.loadFromJson(object));
                    Contracts.sqlDebug("Loaded saved contract data for: '" + uuid + "'!");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void saveContract(Contract contract) {
        MySQLTableEditor sql = new MySQLTableEditor(MySQLTableEditor.Table.CONTRACTS, contract.getUUID(), provider);
        Contracts.sqlDebug("Saving contract data for: '" + contract.getUUID() + "'...");
        MySQLRequest request = new MySQLRequest(contract.getUUID());
        request.addData("contract", contract.getAsJsonObject().toString());
        sql.updateData(request);
        Contracts.sqlDebug("Saved contract data for: " + contract.getUUID());
    }

    @Override
    public void unregisterContract(Contract contract) {
        contracts.remove(contract.getUUID());
        provider.executeUpdateAsync("DELETE FROM " + MySQLTableEditor.Table.CONTRACTS + " WHERE uuid = '" + contract.getUUID() + "';");
        new BukkitRunnable() {
            @Override
            public void run() {
                Contracts.plugin.pluginMessageManager.notifyContractUpdate(contract.getUUID());
            }
        }.runTaskLater(Contracts.plugin, 30L);
    }
}
