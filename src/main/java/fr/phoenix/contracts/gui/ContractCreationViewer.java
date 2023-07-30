package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractParties;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.parameter.Parameter;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class ContractCreationViewer extends EditableInventory {
    public ContractCreationViewer() {
        super("contract-creation");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("create"))
            return new CreateItem(config);
        if (function.equals("parameter"))
            return new ParameterItem(config);
        return new SimpleItem(config);
    }

    public ContractCreationInventory newInventory(PlayerData playerData, ContractType contractType,GeneratedInventory prev) {
        return new ContractCreationInventory(playerData, this, contractType,prev);
    }

    public class CreateItem extends InventoryItem<ContractCreationInventory> {

        public CreateItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ContractCreationInventory inv, int n) {
            return new Placeholders();
        }
    }


    public class ParameterItem extends InventoryItem<ContractCreationInventory> {
        private final FilledParameter filledParameter;
        private final ParameterToFill parameterToFill;


        public ParameterItem(ConfigurationSection config) {
            super(config);
            ConfigurationSection filledParameterSection = config.getConfigurationSection("filled");
            ConfigurationSection parameterToFillSection = config.getConfigurationSection("to-fill");
            Validate.notNull(filledParameterSection, "Couldn't load filled parameters in contract-creation.yml");
            Validate.notNull(parameterToFillSection, "Couldn't load to-fill parameters in contract-creation.yml");
            filledParameter = new FilledParameter(this, filledParameterSection);
            parameterToFill = new ParameterToFill(this, parameterToFillSection);
        }

        @Override
        public ItemStack getDisplayedItem(ContractCreationInventory inv, int n) {
            if (inv.parametersList.size() <= n) {
                return new ItemStack(Material.AIR);
            }
            Parameter parameter = inv.parametersList.get(n);

            ItemStack item;
            if (!parameter.needsToBeFilled())
                item = filledParameter.getDisplayedItem(inv, n);
            else
                item = parameterToFill.getDisplayedItem(inv, n);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "parameter"), PersistentDataType.STRING, parameter.getId());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(ContractCreationInventory inv, int n) {
            Placeholders holders = new Placeholders();
            String id=inv.parametersList.get(n).getId();
            holders.register("id", ContractsUtils.chatName(id));
            holders.register("description", ContractsUtils.formatList(inv.contract.getParameter(id).getParameterType()));
            return holders;
        }
    }

    public class FilledParameter extends InventoryItem<ContractCreationInventory> {

        public FilledParameter(InventoryItem parent, ConfigurationSection config) {
            super(parent, config);
        }


        @Override
        public Placeholders getPlaceholders(ContractCreationInventory inv, int n) {
            Placeholders placeholders = parent.getPlaceholders(inv, n);
            List<String> valueString = inv.parametersList.get(n).get().stream().filter(str -> str != null).collect(Collectors.toList());

            if (valueString.size() == 1) {
                placeholders.register("value", valueString.get(0));
            }
            //If there is more than 1 line we go to the line
            else {
                String result = "";
                for (String str : valueString) {
                    result += "\n" + str;
                }
                placeholders.register("value", result);
            }

            return placeholders;
        }
    }

    public class ParameterToFill extends InventoryItem<ContractCreationInventory> {

        public ParameterToFill(InventoryItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public Placeholders getPlaceholders(ContractCreationInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }


    public class ContractCreationInventory extends GeneratedInventory {
        private ContractType contractType;
        private Contract contract;
        private List<Parameter> parametersList;


        public ContractCreationInventory(PlayerData playerData, EditableInventory editable, ContractType contractType,GeneratedInventory prev) {
            super(playerData, editable,prev);
            this.contractType = contractType;
            //We load the contract and enable the modification of its parameters
            contract = contractType.instanciate(playerData.getUuid());
            parametersList = contract.getParametersList();
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(str.replace("{type}", ContractsUtils.chatName(contractType.toString())));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item instanceof CreateItem) {
                if (!contract.allParameterFilled()) {
                    Message.MISSING_CONTRACT_PARAMETER.format().send(player.getUniqueId());
                    return;
                }
                if (playerData.getUncompletedContracts(playerData, ContractParties.EMPLOYER).size() > Contracts.plugin.configManager.maxEmployerContract) {
                    Message.ALREADY_HAS_TOO_MUCH_CONTRACT.format("amount", Contracts.plugin.configManager.maxEmployerContract).send(player);
                    return;
                } else {
                    //Create the contract and close the inventory
                    player.getOpenInventory().close();
                    contract.createContract();
                }
            }
            if (item instanceof ParameterItem) {
                String parameter = event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(Contracts.plugin, "parameter"), PersistentDataType.STRING);
                contract.openChatInput(parameter, playerData, this);
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
