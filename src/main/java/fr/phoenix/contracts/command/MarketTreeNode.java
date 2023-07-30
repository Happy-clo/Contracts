package fr.phoenix.contracts.command;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.gui.ContractTypeViewer;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketTreeNode extends CommandTreeNode {

    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public MarketTreeNode(CommandTreeNode parent) {
        super(parent, "market");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return CommandResult.FAILURE;

        if (args.length != 1)
            return CommandResult.FAILURE;

        Player player = (Player) sender;
        InventoryManager.CONTRACT_TYPE.newInventory(PlayerData.get(player), ContractTypeViewer.InventoryToOpenType.MARKET_VIEWER, true, null).open();
        return CommandResult.SUCCESS;
    }
}
