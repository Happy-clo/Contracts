package fr.phoenix.contracts.contract.list;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * A contract that isn't for a specific task and thus without any algorithmic protection.
 */
public class GenericContract extends Contract {
    public GenericContract(ConfigurationSection section) {
        super(ContractType.GENERIC, section);
    }

    public GenericContract(JsonObject object) {
        super(ContractType.GENERIC, object);
    }

    public GenericContract(UUID employer) {
        super(ContractType.GENERIC, employer);
    }

    @Override
    public void loadParameters() {
    }

    @Override
    public boolean canDoSpecialAction(PlayerData playerData) {
        return false;
    }

    @Override
    public void onSpecialAction(InventoryClickEvent e) {

    }
}
