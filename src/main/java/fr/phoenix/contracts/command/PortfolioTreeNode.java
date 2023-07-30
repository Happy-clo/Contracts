package fr.phoenix.contracts.command;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortfolioTreeNode extends CommandTreeNode {

    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public PortfolioTreeNode(CommandTreeNode parent) {
        super(parent,"portfolio");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return CommandResult.FAILURE;
        }
        if (args.length != 1)
            return CommandResult.FAILURE;

        Player player = (Player) sender;
        InventoryManager.CONTRACT_PORTFOLIO.newInventory(PlayerData.get(player),null).open();
        return CommandResult.SUCCESS;
    }
}
