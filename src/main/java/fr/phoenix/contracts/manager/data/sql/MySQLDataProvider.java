package fr.phoenix.contracts.manager.data.sql;

import fr.phoenix.contracts.manager.data.ContractManager;
import fr.phoenix.contracts.manager.data.DataProvider;
import fr.phoenix.contracts.manager.data.PlayerDataManager;
import fr.phoenix.contracts.manager.data.sql.api.MMODataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;


public class MySQLDataProvider extends MMODataSource implements DataProvider {
    private final MySQLPlayerDataManager playerDataManager = new MySQLPlayerDataManager(this);
    private final MySQLContractManager contractManager = new MySQLContractManager(this);

    private static final String[] NEW_COLUMNS = new String[]{
            "last_time_logged_out", "BIGINT"};

    public MySQLDataProvider(FileConfiguration config) {
        this.setup(config);

    }


    //TODO
    @Override
    public void load() {
        // Fully create contracts table
        executeUpdateAsync("CREATE TABLE IF NOT EXISTS contracts_contracts(uuid VARCHAR(36)," +
                "contract LONGTEXT,"+
                "PRIMARY KEY (uuid));");
        // Fully create playerdata table.
        executeUpdateAsync("CREATE TABLE IF NOT EXISTS contracts_playerdata(uuid VARCHAR(36)," +
                "player_name LONGTEXT,contracts LONGTEXT," +
                "middleman_contracts LONGTEXT," +
                "reviews LONGTEXT," +
                "last_time_logged_out BIGINT," +
                "PRIMARY KEY (uuid));");

        // Add columns that might not be here by default
        for (int i = 0; i < NEW_COLUMNS.length; i += 2) {
            final String columnName = NEW_COLUMNS[i];
            final String dataType = NEW_COLUMNS[i + 1];
            getResultAsync("SELECT * FROM information_schema.COLUMNS WHERE TABLE_NAME = 'contracts_playerdata' AND COLUMN_NAME = '" + columnName + "'", result -> {
                try {
                    if (!result.next())
                        executeUpdate("ALTER TABLE mmocore_playerdata ADD COLUMN " + columnName + " " + dataType);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    @Override
    public ContractManager getContractManager() {
        return contractManager;
    }

    @Override
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
