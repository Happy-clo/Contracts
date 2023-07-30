package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractParties;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.review.ContractReview;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ActionViewer extends EditableInventory {
    public ActionViewer() {
        super("action");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("cancel"))
            return new CancelItem(config);
        if (function.equals("call-middleman"))
            return new CallMiddlemanItem(config);
        if (function.equals("call-admin"))
            return new CallAdminItem(config);
        if (function.equals("accept-offer"))
            return new AcceptOfferItem(config);
        if (function.equals("end"))
            return new EndItem(config);
        if (function.equals("offer"))
            return new OfferItem(config);
        if (function.equals("review"))
            return new ReviewItem(config);
        if (function.equals("special-action"))
            return new SpecialActionItem(config);

        return null;
    }

    public ActionInventory generate(PlayerData playerData, Contract contract, ContractPortfolioViewer.ContractPortfolioInventory prev) {
        return new ActionInventory(playerData, this, contract, prev);
    }


    public class ReviewItem extends InventoryItem<ActionInventory> {

        public ReviewItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.contract.canLeaveReview(inv.getPlayerData());
        }


        @Override
        public Placeholders getPlaceholders(ActionInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("notation", ContractsUtils.formatNotation(inv.review.getNotation()));
            holders.register("comment", ContractsUtils.formatList(inv.review.getComment()));
            return holders;
        }
    }

    public class OfferItem extends InventoryItem<ActionInventory> {
        private double offer;


        public OfferItem(ConfigurationSection config) {
            super(config);
        }


        @Override
        public Placeholders getPlaceholders(ActionInventory inv, int n) {
            Placeholders holders = inv.contract.getContractPlaceholder(inv);
            holders.register("offer", Contracts.plugin.configManager.decimalFormat.format(offer));
            return holders;
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.contract.getState() == ContractState.OPEN;
        }

        public void setOffer(double offer) {
            this.offer = offer;
        }
    }

    public class EndItem extends SimpleItem<ActionInventory> {

        public EndItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.getPlayerData().getUuid().equals(inv.contract.getEmployer()) && inv.contract.getState() == ContractState.OPEN;
        }
    }

    public class AcceptOfferItem extends InventoryItem<ActionInventory> {

        public AcceptOfferItem(ConfigurationSection config) {
            super(config);
        }

        /**
         * @return true if the contract is open and the player has received an offer.
         */
        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.contract.getState() == ContractState.OPEN && inv.contract.getLastOfferProvider() != null && inv.contract.getLastOfferProvider() != inv.contractParties;
        }

        @Override
        public Placeholders getPlaceholders(ActionInventory inv, int n) {
            Placeholders holders = inv.contract.getContractPlaceholder(inv);
            return holders;
        }
    }

    public class CallAdminItem extends SimpleItem<ActionInventory> {

        public CallAdminItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.contract.getState() == ContractState.MIDDLEMAN_RESOLVED;
        }

    }

    public class CallMiddlemanItem extends InventoryItem<ActionInventory> {

        public CallMiddlemanItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.contract.getState() == ContractState.OPEN;
        }

        @Override
        public Placeholders getPlaceholders(ActionInventory inv, int n) {
            Placeholders holders = new Placeholders();
            double commissionRate = Contracts.plugin.configManager.middlemanCommission;
            holders.register("commission", Contracts.plugin.configManager.decimalFormat.format(inv.contract.getAmount() * commissionRate / 100));
            holders.register("commission-rate", commissionRate);
            return holders;
        }

    }

    public class CancelItem extends SimpleItem<ActionInventory> {

        public CancelItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return (inv.contract.getState() == ContractState.OPEN) && inv.contract.isEmployee(inv.getPlayerData().getUuid());
        }
    }

    public class SpecialActionItem extends InventoryItem<ActionInventory> {

        public SpecialActionItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ActionInventory inv) {
            return inv.contract.canDoSpecialAction(inv.getPlayerData());
        }

        @Override
        public ItemStack getDisplayedItem(ActionInventory inv, int n) {
            ItemStack itemStack = super.getDisplayedItem(inv, n);
            ItemMeta meta = itemStack.getItemMeta();
            Placeholders holders = inv.contract.getContractPlaceholder(inv);
            ContractType contractType = inv.contract.getType();
            meta.setDisplayName(holders.apply(contractType.getSpecialActionName(inv.contractParties)));

            List<String> lore = contractType.getSpecialActionLore(inv.contractParties);
            lore = lore.stream().map((str) -> holders.apply(str)).collect(Collectors.toList());
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            return itemStack;
        }

        @Override
        public Placeholders getPlaceholders(ActionInventory inv, int n) {
            return new Placeholders();
        }
    }


    public class ActionInventory extends GeneratedInventory {
        private final Contract contract;
        private final ContractParties contractParties;
        private final ContractReview review;

        public ActionInventory(PlayerData playerData, EditableInventory editable, Contract contract, GeneratedInventory prev) {
            super(playerData, editable, prev);
            this.contract = contract;
            contractParties = contract.getContractParty(playerData.getUuid());
            UUID reviewer = playerData.getUuid();
            UUID reviewed = contract.getOther(playerData);
            int notation = Contracts.plugin.configManager.defaultNotation;
            review = new ContractReview(reviewed, reviewer, contract, notation, new ArrayList<>());
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{contract-name}", contract.getName());
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            event.setCancelled(true);
            if (item instanceof CancelItem && event.getClick() == ClickType.LEFT && contract.isEmployee(playerData.getUuid())) {
                InventoryManager.CONFIRMATION.generate(this, () -> {
                    contract.whenCancelledByEmployee();
                }).open();
                return;

            }
            if (item instanceof SpecialActionItem) {
                contract.onSpecialAction(event);
                open();
                return;
            }

            if (item instanceof CallMiddlemanItem && event.getClick() == ClickType.LEFT) {
                double commission = Contracts.plugin.configManager.middlemanCommission * contract.getAmount() / 100;
                if (Contracts.plugin.economy.getBalance(player) < commission) {
                    Message.NOT_ENOUGH_MONEY_FOR_COMMISSION.format().send(player);
                    return;
                }

                InventoryManager.CONFIRMATION.generate(this, () -> {
                    contract.callDispute(playerData);
                    Contracts.plugin.economy.withdrawPlayer(player, commission);
                }).open();
                return;
            }

            if (item instanceof CallAdminItem && event.getClick() == ClickType.LEFT) {
                InventoryManager.CONFIRMATION.generate(this, () -> contract.callAdminDispute(playerData)).open();
                return;
            }

            if (item instanceof EndItem && event.getClick() == ClickType.LEFT && contract.isEmployer(playerData.getUuid())) {
                InventoryManager.CONFIRMATION.generate(this, () -> contract.whenContractEnded()).open();
                return;
            }

            if (item instanceof AcceptOfferItem && event.getClick() == ClickType.LEFT) {
                InventoryManager.CONFIRMATION.generate(this, () -> contract.whenOfferAccepted()).open();
                return;
            }

            if (item instanceof OfferItem) {
                OfferItem offerItem = (OfferItem) item;
                if (event.getClick() == ClickType.LEFT) {
                    InventoryManager.CONFIRMATION.generate(this, () -> contract.whenOfferCreated(playerData, offerItem.offer)).open();
                }

                if (event.getClick() == ClickType.RIGHT) {
                    //If the player is already on chat input we block the access employer a new chat input.
                    if (playerData.isOnChatInput()) {
                        Message.ALREADY_ON_CHAT_INPUT.format().send(playerData.getPlayer());
                        return;
                    }
                    double max = contract.getAmount();
                    double min = -contract.getGuarantee();
                    PlayerData.loadAndRun(contract.getEmployer(), (employerData, isAsync) -> Message.SET_OFFER_ASK.format("max", max, "min", min
                            , "employer", playerData.getUuid().equals(contract.getEmployer()) ? "you" : employerData.getPlayerName()).send(playerData.getPlayer()));
                    new ChatInput(playerData, this, (p, val) -> {
                        try {
                            double amount = Double.parseDouble(val);
                            if (amount < min || amount > max) {
                                Message.NOT_IN_LIMIT.format("amount", amount, "max", max, "min", min).send(player);
                                return false;
                            }
                            offerItem.setOffer(amount);

                        } catch (NumberFormatException e) {
                            Message.NOT_VALID_DOUBLE.format().send(player);
                            return false;
                        }
                        return true;
                    });
                }
            }

            if (item instanceof ReviewItem) {
                if (event.getClick() == ClickType.LEFT) {
                    if (review.getComment().size() == 0) {
                        Message.COMMENT_REQUIRED.format().send(player);
                        return;
                    }
                    InventoryManager.CONFIRMATION.generate(this, () -> {
                        contract.hasSentReview(playerData);
                        PlayerData.loadAndRun(review.getReviewed(), (reviewedData, isAsync) -> reviewedData.addReview(review));
                        Message.SEND_REVIEW.format("contract-name", contract.getName()).send(player);
                        Player reviewed = Bukkit.getPlayer(review.getReviewed());
                        if (reviewed != null)
                            Message.RECEIVED_REVIEW.format("contract-name", contract.getName(), "notation", review.getNotation()).send(reviewed);
                    }).open();
                    return;
                }
                if (event.getClick() == ClickType.RIGHT) {
                    Message.SET_NOTATION_ASK.format().send(player);
                    new ChatInput(playerData, this, (playerData, str) -> {
                        try {
                            int notation = Integer.parseInt(str);
                            if (notation < 0 || notation > 5) {
                                Message.NOT_VALID_NOTATION.format("input", str).send(player);
                                return false;
                            }
                            review.setNotation(notation);
                            return true;
                        } catch (NumberFormatException e) {
                            Message.NOT_VALID_INTEGER.format("input", str).send(player);
                            return false;
                        }
                    });
                    return;
                }
                if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    event.setCancelled(true);
                    Message.SET_COMMENT_ASK.format().send(player);
                    new ChatInput(playerData, this, (playerData, str) -> {
                        try {
                            review.addComment(ChatColor.translateAlternateColorCodes('&', str));
                            return true;
                        } catch (NumberFormatException e) {
                            Message.NOT_VALID_INTEGER.format("input", str).send(player);
                            return false;
                        }
                    });
                    return;
                }

            }


        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }

}


