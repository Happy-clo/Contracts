package fr.phoenix.contracts.compat.placeholder;

import org.bukkit.entity.Player;

/**
 * Interface between any placeholder plugin and Contracts
 *
 * @author jules
 */
public interface PlaceholderParser {

    /**
     * @param player Player employer parse placeholders with
     * @param input  String input
     * @return String input with parsed placeholders
     */
    public String parse(Player player, String input);
}
