package fr.phoenix.contracts.listener;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.gui.objects.PluginInventory;
import fr.phoenix.contracts.manager.data.sql.MySQLDataProvider;
import fr.phoenix.contracts.manager.data.yaml.YamlDataProvider;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

public class PlayerListener implements Listener {

    /**
     * Load player data of players when logging in.
     * If there was no player on the server before loads entirely the contractManager.
     */
    @EventHandler
    public void a(PlayerJoinEvent event) {
        if (Bukkit.getOnlinePlayers().size() == 1 && Contracts.plugin.dataProvider instanceof MySQLDataProvider)
            Contracts.plugin.dataProvider.getContractManager().load();
        Contracts.plugin.dataProvider.getPlayerDataManager().load(event.getPlayer().getUniqueId());
    }

    /**
     * Unload player data of players that quit.
     * If there is no player on the server after the player quits clears contractManager.
     */
    @EventHandler
    public void a(PlayerQuitEvent event) {
        if (Contracts.plugin.dataProvider instanceof YamlDataProvider)
            Contracts.plugin.dataProvider.getPlayerDataManager().save(PlayerData.get(event.getPlayer()));

        else if (Bukkit.getOnlinePlayers().size() == 1 && Contracts.plugin.dataProvider instanceof MySQLDataProvider) {
            Contracts.plugin.dataProvider.getContractManager().clear();
            Contracts.plugin.dataProvider.getPlayerDataManager().clear();
        }
    }

    /**
     * Registers clicks in custom GUIs
     */
    @EventHandler
    public void b(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder != null && holder instanceof PluginInventory)
            ((PluginInventory) holder).whenClicked(event);
    }
}
