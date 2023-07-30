package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.Proposal;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ProposalViewer extends EditableInventory {
    public ProposalViewer() {
        super("proposal");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("proposal"))
            return new ProposalItem(config);
        Contracts.log(Level.SEVERE, "No item with function: " + function + "in " + getId() + ".yml.");
        return null;
    }

    public ProposalInventory generate(PlayerData playerData, Contract contract, ContractPortfolioViewer.ContractPortfolioInventory prev) {
        return new ProposalInventory(playerData, this, contract, prev);
    }


    public class ProposalItem extends InventoryItem<ProposalInventory> {

        public ProposalItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ProposalInventory inv, int n) {
            if (inv.page + n >= inv.displayedProposals.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Proposal proposal = inv.displayedProposals.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "employee-uuid"), PersistentDataType.STRING, proposal.getEmployee().toString());
            item.setItemMeta(itemMeta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(ProposalInventory inv, int n) {
            Proposal proposal = inv.displayedProposals.get(n + inv.page * inv.proposalsPerPage);
            Placeholders holders = proposal.getContract().getContractPlaceholder(inv);
            UUID other = proposal.getEmployee();
            holders.register("initial-amount", Contracts.plugin.configManager.decimalFormat.format(inv.contract.getAmount()));
            holders.register("initial-guarantee", Contracts.plugin.configManager.decimalFormat.format(inv.contract.getGuarantee()));
            holders.register("proposal-amount", Contracts.plugin.configManager.decimalFormat.format(proposal.getAmount()));
            holders.register("proposal-amount-taxes", Contracts.plugin.configManager.decimalFormat.format(proposal.getAmount() * (1 + Contracts.plugin.configManager.contractTaxes / 100.)));
            holders.register("proposal-guarantee", Contracts.plugin.configManager.decimalFormat.format(proposal.getGuarantee()));
            holders.register("posted-since", ContractsUtils.formatTime(proposal.getCreationTime()));
            PlayerData.loadAndRun(other, (otherData, async) -> {
                holders.register("other", otherData.getPlayerName());
                holders.register("other-reputation", ContractsUtils.formatNotation(otherData.getMeanNotation()));
                holders.register("other-total-reviews", otherData.getNumberReviews());
                if (async)
                    inv.open();
            });
            return holders;
        }
    }

    public class ProposalInventory extends GeneratedInventory {
        private final Contract contract;
        private int page = 0;
        private final int proposalsPerPage;
        private final List<Proposal> displayedProposals;
        private int maxPage;

        public ProposalInventory(PlayerData playerData, EditableInventory editable, Contract contract, GeneratedInventory prev) {
            super(playerData, editable, prev);
            this.contract = contract;
            displayedProposals = contract.getProposals();
            proposalsPerPage = getEditable().getByFunction("proposal").getSlots().size();
            maxPage = Math.max(0, displayedProposals.size() - 1) / proposalsPerPage;

        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{contract-name}", contract.getName());
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item instanceof ProposalItem) {
                UUID employeeUUID = UUID.fromString(event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(Contracts.plugin, "employee-uuid"), PersistentDataType.STRING));
                Proposal proposal = displayedProposals.stream().filter(proposal1 -> proposal1.getEmployee().equals(employeeUUID)).findFirst().get();
                if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    InventoryManager.REPUTATION.openInventory(playerData, proposal.getEmployee(), this);
                    return;
                }
                if (event.getClick() == ClickType.LEFT) {
                    double paymentAmount = proposal.getAmount() * (1 + Contracts.plugin.configManager.contractTaxes / 100.);
                    if (Contracts.plugin.economy.getBalance(player) < paymentAmount) {
                        Message.NOT_ENOUGH_MONEY_ACCEPT_PROPOSAL.format("amount", paymentAmount).send(player);
                        return;
                    }

                    InventoryManager.CONFIRMATION.generate(this, () ->
                    {//We must run sync
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Contracts.plugin, () ->
                                contract.whenProposalAccepted(proposal));

                    }).open();
                }
            }

        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
