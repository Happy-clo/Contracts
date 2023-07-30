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
import fr.phoenix.contracts.utils.ContractsUtils;
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

import java.util.*;
import java.util.logging.Level;


public class ContractMiddlemanViewer extends EditableInventory {
    public ContractMiddlemanViewer() {
        super("contract-middleman");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("change-state"))
            return new ChangeStateItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);

        return null;
    }

    public ContractMiddlemanInventory generate(PlayerData playerData, GeneratedInventory prev) {
        return new ContractMiddlemanInventory(playerData, this, prev);
    }

    public class ChangeStateItem extends InventoryItem<ContractMiddlemanInventory> {
        private final ConfigurationSection config;

        public ChangeStateItem(ConfigurationSection config) {
            super(config);
            this.config = config;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            ContractState contractState = inv.contractStates.get(n);
            Material displayMaterial = Material.AIR;
            try {
                displayMaterial = Objects.requireNonNull(Material.valueOf(ContractsUtils.enumName(config.getString("material" + (n + 1)))));
            } catch (Exception e) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Couldn't load material" + (n + 1) + ":" + config.getString("material" + (n + 1)) + " for the change state item of the contracts gui");
            }

            ItemStack item = super.getDisplayedItem(inv, n, displayMaterial);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract-state"), PersistentDataType.STRING, contractState.toString());
            item.setItemMeta(meta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("contract-state", ContractsUtils.chatName(inv.contractStates.get(n).toString()));
            return holders;
        }
    }

    public class ContractItem extends InventoryItem<ContractMiddlemanInventory> {
        private final Map<ContractState, InventoryItem> inventoryItems = new HashMap<>();

        public ContractItem(ConfigurationSection config) {
            super(config);
            Material material = Material.valueOf(Objects.requireNonNull(ContractsUtils.enumName(config.getString("item"))));

            for (ContractState contractState : Arrays.asList(ContractState.MIDDLEMAN_DISPUTED, ContractState.MIDDLEMAN_RESOLVED, ContractState.ADMIN_DISPUTED, ContractState.RESOLVED)) {
                ConfigurationSection section = Objects.requireNonNull(config.getConfigurationSection(ContractsUtils.ymlName(contractState.toString()))
                        , "Could not load " + ContractsUtils.ymlName(contractState.toString()) + " config");
                inventoryItems.put(contractState, getItem(section, contractState, material));
            }
        }

        public InventoryItem getItem(ConfigurationSection section, ContractState state, Material material) {
            switch (state) {
                case MIDDLEMAN_DISPUTED:
                    return new MiddlemanDisputedContractItem(this, section, material);
                case MIDDLEMAN_RESOLVED:
                    return new MiddlemanResolvedContractItem(this, section, material);
                case ADMIN_DISPUTED:
                    return new AdminDisputedContractItem(this, section, material);
                case RESOLVED:
                    return new ResolvedContractItem(this, section, material);

            }
            return null;
        }

        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            return null;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            Contract contract = inv.displayedContracts.get(inv.page + n);

            ItemStack item = inventoryItems.get(contract.getState()).getDisplayedItem(inv, n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUUID().toString());
            item.setItemMeta(itemMeta);
            return item;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }


    }


    public class MiddlemanDisputedContractItem extends InventoryItem<ContractMiddlemanInventory> {
        private double amount;


        public MiddlemanDisputedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUUID().toString());
            item.setItemMeta(itemMeta);
            return item;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = contract.getContractPlaceholder(inv);
            holders.register("amount", amount);
            return holders;
        }
    }

    public class MiddlemanResolvedContractItem extends InventoryItem<ContractMiddlemanInventory> {


        public MiddlemanResolvedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUUID().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = contract.getContractPlaceholder(inv);
            return holders;
        }
    }

    public class AdminDisputedContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public AdminDisputedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUUID().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = contract.getContractPlaceholder(inv);
            return holders;
        }
    }

    public class ResolvedContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public ResolvedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUUID().toString());
            item.setItemMeta(itemMeta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = contract.getContractPlaceholder(inv);
            return holders;
        }
    }

    public class ContractMiddlemanInventory extends GeneratedInventory {
        private int page = 0;
        private final int contractsPerPage;
        private List<Contract> displayedContracts;
        private int maxPage;
        private ContractState contractState = ContractState.MIDDLEMAN_DISPUTED;
        private List<ContractState> contractStates = Arrays.asList(ContractState.MIDDLEMAN_DISPUTED, ContractState.MIDDLEMAN_RESOLVED, ContractState.ADMIN_DISPUTED, ContractState.RESOLVED);

        public ContractMiddlemanInventory(PlayerData playerData, EditableInventory editable, GeneratedInventory prev) {
            super(playerData, editable, prev);
            displayedContracts = playerData.getMiddlemanContracts(contractState);
            contractsPerPage = getEditable().getByFunction("contract").getSlots().size();

            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;

        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{contract-state}", ContractsUtils.chatName(contractState.toString()));
        }

        public void changeState(ContractState contractState) {
            this.contractState = contractState;
            displayedContracts = playerData.getMiddlemanContracts(contractState);
            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item instanceof ChangeStateItem) {
                ContractState newView = ContractState.valueOf(event.getCurrentItem().getItemMeta().getPersistentDataContainer().
                        get(new NamespacedKey(Contracts.plugin, "contract-state"), PersistentDataType.STRING));
                changeState(newView);
                open();
            }

            if (item instanceof ContractItem) {
                ContractItem contractItem = (ContractItem) item;
                Contract contract = Contracts.plugin.dataProvider.getContractManager().get(UUID.fromString(Objects.requireNonNull(event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING))));

                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    InventoryManager.REPUTATION.openInventory(playerData, contract.getEmployer(), this);
                    return;
                }

                if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    InventoryManager.REPUTATION.openInventory(playerData, contract.getEmployee(), this);

                    return;
                }

                //For MIDDLEMAN_DISPUTED contracts.
                if (contractState == ContractState.MIDDLEMAN_DISPUTED) {
                    MiddlemanDisputedContractItem middlemanDisputedContractItem = (MiddlemanDisputedContractItem) contractItem.inventoryItems.get(contractState);

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
                                middlemanDisputedContractItem.setAmount(amount);

                            } catch (NumberFormatException e) {
                                Message.NOT_VALID_DOUBLE.format().send(player);
                                return false;
                            }
                            return true;
                        });
                    }
                    if (event.getClick() == ClickType.LEFT) {
                        InventoryManager.CONFIRMATION.generate(this, () -> contract.whenDecidedByMiddleman(middlemanDisputedContractItem.amount)).open();
                    }
                }


            }

        }


        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }

}
