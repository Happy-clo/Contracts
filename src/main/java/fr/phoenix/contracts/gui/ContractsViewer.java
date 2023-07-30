package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ContractsViewer extends EditableInventory {


    public ContractsViewer() {
        super("contracts");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if(function.equals("middleman"))
            return new SimpleItem<ContractsGenerated>(config) {
                @Override
                public boolean isDisplayed(ContractsGenerated inv) {
                    return inv.getPlayer().hasPermission("contracts.middleman");
                }
            };
        if(function.equals("admin"))
            return new SimpleItem<ContractsGenerated>(config) {
                @Override
                public boolean isDisplayed(ContractsGenerated inv) {
                    return inv.getPlayer().hasPermission("contracts.admin");
                }
            };
        return new SimpleItem(config);

    }


    public ContractsGenerated newInventory(PlayerData playerData,GeneratedInventory prev) {
        return new ContractsGenerated(playerData, this,prev);
    }



    public class ContractsGenerated<ContractsViewer> extends GeneratedInventory {

        public ContractsGenerated(PlayerData playerData, EditableInventory editable,GeneratedInventory prev){
            super(playerData, editable,prev);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return Contracts.plugin.placeholderParser.parse(player, str);
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("portfolio"))
                InventoryManager.CONTRACT_PORTFOLIO.newInventory(playerData,this).open();
            if (item.getFunction().equals("market"))
                InventoryManager.CONTRACT_TYPE.newInventory(playerData, ContractTypeViewer.InventoryToOpenType.MARKET_VIEWER,true,this).open();
            if (item.getFunction().equals("creation"))
                InventoryManager.CONTRACT_TYPE.newInventory(playerData, ContractTypeViewer.InventoryToOpenType.CREATION_VIEWER,false,this).open();
            if (item.getFunction().equals("middleman"))
                InventoryManager.CONTRACT_MIDDLEMAN.generate(playerData,this).open();
            if (item.getFunction().equals("admin"))
                InventoryManager.CONTRACT_ADMIN.generate(playerData,this).open();
            if (item.getFunction().equals("reputation"))
                InventoryManager.REPUTATION.newInventory(playerData,playerData,this).open();
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
        }
    }

}
