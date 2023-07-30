package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.utils.ContractsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ReputationViewer extends EditableInventory {


    public ReputationViewer() {
        super("reputation");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("review"))
            return new ReviewItem(config);
        return new SimpleItem(config);
    }

    /**
     * Used when a player tries employer check his reputation
     */
    public ReputationInventory newInventory(PlayerData playerData) {
        return new ReputationInventory(playerData, playerData, this, null);
    }

    public ReputationInventory newInventory(PlayerData playerData, PlayerData reputationPlayer) {
        return new ReputationInventory(playerData, reputationPlayer, this, null);
    }

    public ReputationInventory newInventory(PlayerData playerData, PlayerData reputationPlayer, GeneratedInventory prev) {
        return new ReputationInventory(playerData, reputationPlayer, this, prev);
    }

    /**
     * Supports loading playerData of an offline player as it should be possible to see the reputation of an offline player.
     */
    public void openInventory(PlayerData playerData, UUID reputationPlayer, GeneratedInventory invToOpen) {
        PlayerData.loadAndRun(reputationPlayer, (reputationPlayerData, isAsync) -> newInventory(playerData, reputationPlayerData, invToOpen).open());
    }


    public class ReviewItem extends InventoryItem<ReputationInventory> {


        public ReviewItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ReputationInventory inv, int n) {
            if (inv.getReviews().size() <= inv.getPage() * inv.getReviewPerPage() + n)
                return new ItemStack(Material.AIR);

            return super.getDisplayedItem(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(ReputationInventory inv, int n) {
            ContractReview review = inv.getReviews().get(inv.getPage() * inv.getReviewPerPage() + n);
            Placeholders holders = new Placeholders();
            PlayerData.loadAndRun(review.getReviewer(), (reviewerData, isAsync) -> {
                holders.register("reviewer", reviewerData.getPlayerName());
                if (isAsync)
                    inv.open();
            });
            holders.register("contract-state", review.getContract().getState().toString().toLowerCase().
                    replace("_", " "));
            holders.register("contract-name", review.getContract().getName());
            holders.register("notation", ContractsUtils.formatNotation(review.getNotation()));

            holders.register("comment", ContractsUtils.formatList(review.getComment()));
            holders.register("posted-since", ContractsUtils.formatTime(review.getReviewDate()));
            return holders;
        }
    }

    public class ReputationInventory extends GeneratedInventory {
        private final PlayerData reputationPlayer;
        private final List<ContractReview> reviews;
        //The getByFunction method of generated inventory will return only if the item has been loaded
        // in it which is not the case here -> editable method
        private final int reviewPerPage;


        public ReputationInventory(PlayerData playerData, PlayerData reputationPlayer, EditableInventory editable, GeneratedInventory prev) {
            super(playerData, editable, prev);
            this.reputationPlayer = reputationPlayer;
            reviews = reputationPlayer.getReviews();
            reviewPerPage = getEditable().getByFunction("review").getSlots().size();
            maxPage = (Math.max(0, playerData.getReviews().size() - 1)) / reviewPerPage;
        }


        public List<ContractReview> getReviews() {
            return reviews;
        }

        public int getReviewPerPage() {
            return reviewPerPage;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(str.replace("{player}", reputationPlayer.getPlayerName()));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            //nothing
        }
    }
}
