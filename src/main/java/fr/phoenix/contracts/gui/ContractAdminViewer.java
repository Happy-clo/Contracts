package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ChatInput;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class ContractAdminViewer extends EditableInventory {

    public ContractAdminViewer() {
        super("contract-admin");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("contract"))
            return new ContractItem(config);

        return new SimpleItem(config);
    }

    public ContractAdminInventory generate(PlayerData playerData, GeneratedInventory prev) {
        return new ContractAdminInventory(playerData, this, prev);
    }


    public class ContractItem extends InventoryItem<ContractAdminInventory> {
        private double amount;

        public ContractItem(ConfigurationSection config) {
            super(config);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractAdminInventory inv, int n) {
            if (inv.getPage() + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.getPage() + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUUID().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractAdminInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.getPage() + n);
            return contract.getContractPlaceholder(inv);
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }


    public class ContractAdminInventory extends GeneratedInventory {
        private final List<Contract> displayedContracts = Contracts.plugin.dataProvider.getContractManager().getContractsOfState(ContractState.ADMIN_DISPUTED);

        public ContractAdminInventory(PlayerData playerData, EditableInventory editable, GeneratedInventory prev) {
            super(playerData, editable, prev);
            maxPage = Math.max(0, (displayedContracts.size() - 1) / editable.getSlots());
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return Contracts.plugin.placeholderParser.parse(player, str);
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item instanceof ContractItem && event.getClick() == ClickType.LEFT) {
                Contract contract = Contracts.plugin.dataProvider.getContractManager().get(UUID.fromString(
                        event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING)));
                //For MIDDLEMAN_DISPUTED contracts.
                if (contract.getState() == ContractState.ADMIN_DISPUTED) {
                    ContractItem contractItem = (ContractItem) item;
                    if (event.getClick() == ClickType.RIGHT) {
                        //If the player is already on chat input we block the access employer a new chat input.
                        if (playerData.isOnChatInput()) {
                            Message.ALREADY_ON_CHAT_INPUT.format().send(playerData.getPlayer());
                            return;
                        }
                        double max = contract.getAmount();
                        double min = -contract.getGuarantee();
                        Message.RESOLVE_DISPUTE_ASK.format("max", max, "min", min).send(playerData.getPlayer());
                        new ChatInput(playerData, this, (p, val) -> {
                            try {
                                double amount = Double.parseDouble(val);
                                if (amount < min || amount > max) {
                                    Message.NOT_IN_LIMIT.format("amount", amount, "max", max, "min", min);
                                    return false;
                                }
                                contractItem.setAmount(amount);

                            } catch (NumberFormatException e) {
                                Message.NOT_VALID_DOUBLE.format().send(player);
                                return false;
                            }
                            return true;
                        });
                    }
                    if (event.getClick() == ClickType.LEFT) {
                        InventoryManager.CONFIRMATION.generate(this, () -> contract.whenClosedByAdmin(contractItem.amount)).open();
                    }
                }
            }


        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
