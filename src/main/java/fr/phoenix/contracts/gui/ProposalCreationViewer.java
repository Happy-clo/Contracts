package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ChatInput;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ProposalCreationViewer extends EditableInventory {
    public ProposalCreationViewer() {
        super("proposal-creation");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("confirm"))
            return new SimpleItem(config);
        if (function.equals("amount"))
            return new AmountItem(config);
        if (function.equals("guarantee"))
            return new GuaranteeItem(config);

        return null;
    }

    public ProposalInventory generate(PlayerData playerData, Contract contract, GeneratedInventory invToOpen) {
        return new ProposalInventory(playerData, this, contract, invToOpen);
    }


    public class AmountItem extends InventoryItem<ProposalInventory> {

        public AmountItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ProposalInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("amount", inv.amount);
            holders.register("initial-amount", inv.contract.getAmount());
            return holders;
        }
    }


    public class GuaranteeItem extends InventoryItem<ProposalInventory> {

        public GuaranteeItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ProposalInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("guarantee", inv.guarantee);
            holders.register("initial-guarantee", inv.contract.getGuarantee());
            return holders;
        }

    }

    public class ProposalInventory extends GeneratedInventory {
        private final Contract contract;
        private double amount, guarantee;

        public ProposalInventory(PlayerData playerData, ProposalCreationViewer editable, Contract contract, GeneratedInventory prev) {
            super(playerData, editable,prev);
            this.contract = contract;
            this.amount = contract.getAmount();
            this.guarantee = contract.getGuarantee();
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return Contracts.plugin.placeholderParser.parse(player, str.replace("{contract-name}", contract.getName()));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("confirm") && event.getClick() == ClickType.LEFT) {
                if (Contracts.plugin.economy.getBalance(player) < guarantee) {
                    Message.NOT_ENOUGH_MONEY_PROPOSAL.format("guarantee", guarantee).send(player);
                    return;
                }

                InventoryManager.CONFIRMATION.generate(this, () ->
                {//We must run sync
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Contracts.plugin, () -> {
                                contract.makeProposal(getPlayerData(), amount, guarantee);
                            }
                    );
                }).open();
            }

            if (item instanceof AmountItem && event.getClick() == ClickType.LEFT) {
                new ChatInput(playerData, this, (p, val) -> {
                    try {
                        double amount = Double.parseDouble(val);
                        if (amount <= 0) {
                            Message.NOT_STRICTLY_POSITIVE_NUMBER.format().send(player);
                            return false;
                        }
                        this.amount = amount;
                    } catch (NumberFormatException e) {
                        Message.NOT_VALID_DOUBLE.format().send(player);
                        return false;
                    }
                    return true;
                });
            }

            if (item instanceof GuaranteeItem && event.getClick() == ClickType.LEFT) {
                new ChatInput(playerData, this, (p, val) -> {
                    try {
                        double amount = Double.parseDouble(val);
                        if (amount < 0) {
                            Message.NOT_POSITIVE_NUMBER.format().send(player);
                            return false;
                        }
                        this.guarantee = amount;
                    } catch (NumberFormatException e) {
                        Message.NOT_VALID_DOUBLE.format().send(player);
                        return false;
                    }
                    return true;
                });
            }


        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
