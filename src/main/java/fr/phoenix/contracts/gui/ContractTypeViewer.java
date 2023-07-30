package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ContractTypeViewer extends EditableInventory {


    public ContractTypeViewer() {
        super("contract-type");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (Arrays.stream(ContractType.values()).map(contractType -> ContractsUtils.ymlName(contractType.toString())).collect(Collectors.toList()).contains(function))
            return new SimpleItem<ContractTypeInventory>(config) {
            };
        if (function.equals("all"))
            return new SimpleItem<ContractTypeInventory>(config) {
                @Override
                public boolean isDisplayed(ContractTypeInventory inv) {
                    return inv.showAllType;
                }
            };
        return null;
    }

    public ContractTypeInventory newInventory(PlayerData playerData, InventoryToOpenType inventoryToOpen, boolean showAllType, GeneratedInventory prev) {
        return new ContractTypeInventory(playerData, this, inventoryToOpen, showAllType, prev);
    }

    public class ContractTypeInventory extends GeneratedInventory {
        private final InventoryToOpenType inventoryToOpen;
        private final boolean showAllType;

        public ContractTypeInventory(PlayerData playerData, EditableInventory editable, InventoryToOpenType inventoryToOpen, boolean showAllType, GeneratedInventory prev) {
            super(playerData, editable, prev);
            this.inventoryToOpen = inventoryToOpen;
            this.showAllType = showAllType;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(
                    str.replace("{type}", inventoryToOpen == InventoryToOpenType.CREATION_VIEWER ? "Contract Creation" : "Contract Market"));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            //Opens the right inventory for the players. (market or create)
            event.setCancelled(true);
            if (item.getFunction().equals("all"))
                inventoryToOpen.open(playerData, null,this);
            else {
                //We get the type corresponding to the click and open the inventory.
                ContractType type = ContractType.valueOf(ContractsUtils.enumName(item.getFunction()));
                inventoryToOpen.open(playerData, type,this);
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }

    public enum InventoryToOpenType {
        MARKET_VIEWER(((playerData, contractType,prev) -> InventoryManager.CONTRACT_MARKET.newInventory(playerData, contractType, prev).open())),
        CREATION_VIEWER(((playerData, contractType,prev) -> InventoryManager.CONTRACT_CREATION.newInventory(playerData, contractType, prev).open()));

        private final TriConsumer<PlayerData, ContractType,GeneratedInventory> openInv;

        InventoryToOpenType(TriConsumer<PlayerData, ContractType,GeneratedInventory> openInv) {
            this.openInv = openInv;
        }

        public void open(PlayerData playerData, ContractType contractType,GeneratedInventory prev) {
            openInv.accept(playerData, contractType,prev);
        }
    }
}
