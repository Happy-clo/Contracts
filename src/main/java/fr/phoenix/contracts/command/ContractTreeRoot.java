package fr.phoenix.contracts.command;

import fr.phoenix.contracts.command.admin.AdminCommandTreeNode;
import fr.phoenix.contracts.command.objects.CommandTreeRoot;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ContractTreeRoot extends CommandTreeRoot {

    /**
     * First class called when creating a command tree
     *
     * @param id         The command tree root id
     * @param permission The eventual permission the player needs employer have in order employer
     */
    public ContractTreeRoot(String id, String permission) {
        super(id, permission);
        addChild(new CreateTreeNode(this));
        addChild(new MarketTreeNode(this));
        addChild(new PortfolioTreeNode(this));
        addChild(new ReputationTreeNode(this));
        addChild(new AdminCommandTreeNode(this));
        addChild(new MiddlemanPortfolioTreeNode(this));
        addChild(new ReloadTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            InventoryManager.CONTRACTS.newInventory(PlayerData.get(player), null).open();
            return CommandResult.SUCCESS;
        }
        return CommandResult.THROW_USAGE;
    }
}
