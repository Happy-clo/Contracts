package fr.phoenix.contracts.command;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.command.objects.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadTreeNode extends CommandTreeNode {
    public ReloadTreeNode(CommandTreeNode parent) {
        super(parent, "reload");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading " + Contracts.plugin.getName() + " " + Contracts.plugin.getDescription().getVersion() + "...");
        long ms = System.currentTimeMillis();

        Contracts.plugin.configManager.load();

        ms = System.currentTimeMillis() - ms;
        sender.sendMessage(ChatColor.YELLOW + Contracts.plugin.getName() + " " + Contracts.plugin.getDescription().getVersion() + " successfully reloaded.");
        sender.sendMessage(ChatColor.YELLOW + "Time Taken: " + ChatColor.GOLD + ms + ChatColor.YELLOW + "ms (" + ChatColor.GOLD + (double) ms / 50 + ChatColor.YELLOW + " ticks)");
        return CommandResult.SUCCESS;
    }
}
