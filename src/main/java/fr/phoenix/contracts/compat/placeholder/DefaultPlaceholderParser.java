package fr.phoenix.contracts.compat.placeholder;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Only parses & chat colors and hex colors
 */
public class DefaultPlaceholderParser implements PlaceholderParser {
    private final Pattern PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    @Override
    public String parse(Player player, String input) {

        // Parse chat colors
        input = ChatColor.translateAlternateColorCodes('&', input);

        //Parse Unicode Characters
        input=decode(input);

        // Parse hex colors
        Matcher match = PATTERN.matcher(input);

        while (match.find()) {
            String color = input.substring(match.start(), match.end());
            input = input.replace(color, "" + ChatColor.of('#' + match.group(2)));
            match = PATTERN.matcher(input);
        }

        return input;
    }

    static String decode( String in)
    {
        String working = in;
        int index;
        index = working.indexOf("\\u");
        while(index > -1)
        {
            int length = working.length();
            if(index > (length-6))break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring,16);
            String stringStart = working.substring(0, index);
            String stringEnd   = working.substring(numFinish);
            working = stringStart + ((char)number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }
}
