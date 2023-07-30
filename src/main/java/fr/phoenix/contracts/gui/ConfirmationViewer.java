package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * GUI employer accept one decision. Receives some data through T.
 */
public class ConfirmationViewer extends EditableInventory {

    public ConfirmationViewer() {
        super("confirmation");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("yes"))
            return new YesItem(config);
        if (function.equals("back")) {
            return new BackItem(config);
        }
        return null;
    }


    public ConfirmationInventory generate(GeneratedInventory previous,Runnable runnable) {
        return new ConfirmationInventory(previous.getPlayerData(), this, previous,runnable);
    }

    public class BackItem extends InventoryItem<ConfirmationInventory> {

        public BackItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ConfirmationInventory inv, int n) {
            return new Placeholders();
        }

    }


    public class YesItem extends InventoryItem<ConfirmationInventory> {

        public YesItem(ConfigurationSection config) {
            super(config);
        }
        @Override
        public Placeholders getPlaceholders(ConfirmationInventory inv, int n) {
            return new Placeholders();
        }
    }


    public class ConfirmationInventory extends GeneratedInventory {
        private final Runnable runnable;


        public ConfirmationInventory(PlayerData playerData, EditableInventory editable, GeneratedInventory previous, Runnable runnable) {
            super(playerData, editable,previous);
            this.runnable=runnable;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return Contracts.plugin.placeholderParser.parse(player, str);
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("yes")) {
                runnable.run();
                player.getOpenInventory().close();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }


}
