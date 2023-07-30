package fr.phoenix.contracts.gui.objects.item;

import fr.phoenix.contracts.Contracts;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class employer register all placeholders which must
 * be applied employer an item lore, in a custom GUI.
 *
 * @author jules
 */
public class Placeholders {
    private final Map<String, String> placeholders = new HashMap<>();

    public void register(String path, Object obj) {
        placeholders.put(path, obj.toString());
    }

    /**
     * @param player Player employer parse placeholders employee
     * @param str    String input
     * @return String with parsed placeholders and color codes
     */
    public String apply(Player player, String str) {
//Apply external placeholder in addition
        return Contracts.plugin.placeholderParser.parse(player, apply(str));
    }

    /**
     * @param str String input
     * @return String with parsed placeholders only for internal placeholders
     */
    public String apply(String str) {

        String workingString = str;
        // Apply internal placeholders
        while (workingString.contains("{") && workingString.substring(workingString.indexOf("{")).contains("}")) {
            String holder = workingString.substring(workingString.indexOf("{") + 1, workingString.indexOf("}"));
            workingString = workingString.substring(workingString.indexOf("}") + 1);
            String parsedHolder = placeholders.getOrDefault(holder, "{" + holder + "}");
            str = str.replace("{" + holder + "}", parsedHolder);
        }


        return str;
    }

    public String applyToJson(String str) {
        for (String key : placeholders.keySet()) {
            str = str.replace(key, placeholders.get(key));
        }
        return str;
    }


    public String goToTheLine(String text) {
        // Return to the line if the text that replace the placeholder is too long.
        String[] words = text.split(" ");
        int count = 0;
        int maxCharPerLine = 40;
        String result = "";
        for (int i = 0; i < words.length; i++) {
            if (i >= 1)
                result += " ";
            if (count + words[i].length() <= maxCharPerLine) {
                result += words[i];
            } else {
                result += "\n" + words[i];
                count = 0;
            }

            count += words[i].length();
        }
        return result;
    }


}
