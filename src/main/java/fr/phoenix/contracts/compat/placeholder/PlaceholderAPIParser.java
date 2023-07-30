package fr.phoenix.contracts.compat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import static fr.phoenix.contracts.compat.placeholder.DefaultPlaceholderParser.decode;

public class PlaceholderAPIParser implements PlaceholderParser {

    @Override
    public String parse(Player player, String input) {

        // Parse chat colors
        input = ChatColor.translateAlternateColorCodes('&', input);

        //Parse Unicode Characters
        input=decode(input);
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}
