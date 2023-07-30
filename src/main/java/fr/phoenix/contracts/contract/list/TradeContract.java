package fr.phoenix.contracts.contract.list;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractParties;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.parameter.Parameter;
import fr.phoenix.contracts.contract.parameter.ParameterType;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class TradeContract extends Contract implements Listener {
    private Material item;
    private int itemAmount, itemTaken, itemGiven;

    public TradeContract(UUID employer) {
        super(ContractType.TRADE, employer);
        loadParameters();
    }

    public TradeContract(ConfigurationSection section) {
        super(ContractType.TRADE, section);
        loadParameters();
        item = Material.valueOf(section.getString("traded-item"));
        itemAmount = section.getInt("traded-item-amount");
        itemGiven = section.getInt("traded-item-given");
        itemTaken = section.getInt("traded-item-taken");
    }

    public TradeContract(JsonObject object) {
        super(ContractType.TRADE, object);
        loadParameters();
        item = Material.valueOf(object.get("traded-item").getAsString());
        itemAmount = object.get("traded-item-amount").getAsInt();
        itemAmount = object.get("traded-item-given").getAsInt();
        itemAmount = object.get("traded-item-taken").getAsInt();
    }

    @Override
    public boolean canDoSpecialAction(PlayerData playerData) {
        if (getContractParty(playerData.getUuid()) == ContractParties.EMPLOYEE)
            return getState() == ContractState.OPEN;
        if (getContractParty(playerData.getUuid()) == ContractParties.EMPLOYER)
            return itemTaken < itemAmount && getState() == ContractState.RESOLVED;
        return false;
    }

    public void employeeSpecialAction(PlayerData playerData) {
        //We parse through the inventory and remove every corresponding item in its inventory.
        Inventory inventory = playerData.getPlayer().getInventory();
        int oldItemGiven = itemGiven;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == this.item) {
                if (itemGiven + item.getAmount() > itemAmount) {
                    item.setAmount(item.getAmount() - (itemAmount - itemGiven));
                    itemGiven = itemAmount;
                    whenContractFulfilled();
                    Message.TRADE_ITEM_DEPOSIT.format("amount", itemGiven - oldItemGiven, "item", item.toString()
                            , "item-remaining", itemAmount - itemGiven, "contract-name", name).send(playerData.getPlayer());
                    return;
                } else {
                    itemGiven += item.getAmount();
                    item.setAmount(0);
                }
            }
        }
        int amount = itemGiven - oldItemGiven;
        if (amount == 0)
            Message.NOT_ANY_TRADE_ITEM.format("item", item.toString()).send(playerData.getPlayer());
        else
            Message.TRADE_ITEM_DEPOSIT.format("amount", itemGiven - oldItemGiven, "item", item.toString()
                    , "item-remaining", itemAmount - itemGiven, "contract-name", name).send(playerData.getPlayer());

    }

    public void employerSpecialAction(PlayerData playerData) {
        int maxStackSize = item.getMaxStackSize();
        Inventory inventory = playerData.getPlayer().getInventory();
        ItemStack[] contents = inventory.getContents();
        int oldItemTaken = itemTaken;
        for (int i = 0; i < contents.length; i++) {
            ItemStack inventoryItem = contents[i];
            if (inventoryItem == null || inventoryItem.getType() == Material.AIR) {
                int amountTaken = Math.min(maxStackSize, itemAmount - itemTaken);
                inventory.setItem(i, new ItemStack(item, amountTaken));
                this.itemTaken += amountTaken;
                //If all the necessary items have been given.
                if (itemTaken >= itemAmount) {
                    Message.TRADE_ITEM_RECEIPT.format("amount", itemTaken - oldItemTaken, "item", item.toString()
                            , "item-remaining", itemAmount - itemTaken, "contract-name", name).send(playerData.getPlayer());
                    return;
                }
            }
        }
        if (amount == 0)
            Message.NOT_ANY_SPACE_INVENTORY.format().send(playerData.getPlayer());
        else
            Message.TRADE_ITEM_RECEIPT.format("amount", itemTaken - oldItemTaken, "item", item.toString()
                    , "item-remaining", itemAmount - itemTaken, "contract-name", name).send(playerData.getPlayer());


    }

    @Override
    public void onSpecialAction(InventoryClickEvent e) {
        if (e.getClick() != ClickType.LEFT)
            return;
        PlayerData playerData = PlayerData.get((Player) e.getWhoClicked());
        ContractParties contractParty = getContractParty(playerData.getUuid());
        if (contractParty == ContractParties.EMPLOYEE)
            employeeSpecialAction(playerData);
        if (contractParty == ContractParties.EMPLOYER)
            employerSpecialAction(playerData);
    }


    @Override
    public void loadParameters() {
        addParameter(new Parameter(ParameterType.TRADED_ITEM
                , () -> Arrays.asList(item == null ? "" : item.toString()), (player, str) -> {
            try {
                this.item = Material.valueOf(ContractsUtils.enumName(str));
            } catch (IllegalArgumentException exception) {
                Message.NOT_VALID_MATERIAL.format("input", ContractsUtils.enumName(str)).send(player);
            }
        }, () -> item == null));

        addParameter(new Parameter(ParameterType.TRADED_ITEM_AMOUNT,
                () -> Arrays.asList("" + itemAmount), (player, str) -> {
            try {
                itemAmount = Integer.parseInt(str);
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", str).send(player);
            }
        }, () -> itemAmount == 0));
    }

    @Override
    public void save(FileConfiguration config) {
        super.save(config);
        String str = contractId.toString();
        //Very important employer set the type in the yml
        config.set(str + ".traded-item", item.toString());
        config.set(str + ".traded-item-amount", itemAmount);
        config.set(str + ".traded-item-given", itemGiven);
        config.set(str + ".traded-item-taken", itemTaken);
    }

    public JsonObject getAsJsonObject() {
        JsonObject object = super.getAsJsonObject();
        object.addProperty("traded-item", item.toString());
        object.addProperty("traded-item-amount", itemAmount);
        object.addProperty("traded-item-given", itemGiven);
        object.addProperty("traded-item-taken", itemTaken);
        return object;
    }

    @Override
    public Placeholders getContractPlaceholder(GeneratedInventory inv) {
        Placeholders holders = super.getContractPlaceholder(inv);
        PlayerData playerData = inv.getPlayerData();
        holders.register("item-given", itemGiven);
        holders.register("item-taken", itemTaken);

        //Loads the type specific description of the contract.
        //This must absolutely be at the end as it will use all the other placeholders!
        holders.register("type-specific-description", holders.apply(playerData.getPlayer(), ContractsUtils.formatList(type.getSpecificDescription())));

        return holders;
    }
}

