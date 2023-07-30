package fr.phoenix.contracts.contract.list;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.parameter.Parameter;
import fr.phoenix.contracts.contract.parameter.ParameterType;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.UUID;

public class HitmanContract extends Contract implements Listener {
    private UUID playerToKill;

    public HitmanContract(ConfigurationSection section) {
        super(ContractType.HITMAN, section);
        loadParameters();
        playerToKill = UUID.fromString(section.getString("player-to-kill"));
        Bukkit.getPluginManager().registerEvents(this, Contracts.plugin);
    }

    public HitmanContract(JsonObject object) {
        super(ContractType.HITMAN, object);
        loadParameters();
        playerToKill = UUID.fromString(object.get("player-to-kill").getAsString());
        Bukkit.getPluginManager().registerEvents(this, Contracts.plugin);
    }

    public HitmanContract(UUID employer) {
        super(ContractType.HITMAN, employer);
        loadParameters();
        Bukkit.getPluginManager().registerEvents(this, Contracts.plugin);
    }


    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        LivingEntity killer = e.getEntity().getKiller();
        if (state == ContractState.OPEN && killer instanceof Player) {
            Player killingPlayer = (Player) killer;
            if (killingPlayer.equals(Bukkit.getPlayer(employee)) && e.getEntity().equals(Bukkit.getPlayer(playerToKill))) {
                whenContractFulfilled();
            }
        }
    }

    @Override
    public void loadParameters() {
        addParameter(new Parameter(ParameterType.PLAYER_TO_KILL,
                () -> Arrays.asList(Bukkit.getOfflinePlayer(playerToKill).getName()),
                (p, str) -> {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(str);
                    if (Contracts.plugin.dataProvider.getPlayerDataManager().hasAlreadyBeenConnected(player)) {
                        playerToKill = player.getUniqueId();
                    } else Message.NOT_VALID_PLAYER.format("input", str).send(p.getUniqueId());
                }, () -> playerToKill == null));
    }

    @Override
    public void save(FileConfiguration config) {
        super.save(config);
        String str = contractId.toString();
        config.set(str + ".player-to-kill", playerToKill.toString());
    }

    @Override
    public JsonObject getAsJsonObject() {
        JsonObject object = super.getAsJsonObject();
        object.addProperty("player-to-kill", playerToKill.toString());
        return object;
    }

    @Override
    public boolean canDoSpecialAction(PlayerData playerData) {
        return false;
    }

    @Override
    public void onSpecialAction(InventoryClickEvent e) {

    }


}
