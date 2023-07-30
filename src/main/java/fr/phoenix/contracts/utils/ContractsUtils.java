package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ContractsUtils {
    public static String applyColorCode(String str) {
        return ChatColor.translateAlternateColorCodes(Contracts.plugin.configManager.colorCodeChar, str);
    }

    /**
     * Takes a time in millis and returns the hours that passed since that event
     */
    public static int timeSinceInHours(long time) {
        return (int) (System.currentTimeMillis() - time) / (1000 * 3600);
    }

    public static String formatTime(long time) {
        if ((System.currentTimeMillis() - time) / (1000 * 3600 * 24) > 1) {
            //{day} day, {hour} hours format.
            int day = (int) ((System.currentTimeMillis() - time) / (1000 * 3600 * 24));
            int hour = (int) ((System.currentTimeMillis() - time) / (1000 * 3600) - 24 * day);
            return day + "d, " + hour + "h";
        } else {
            //{hours} hours, {minutes} minutes format
            //{day} day, {hour} hours format.
            int hour = (int) ((System.currentTimeMillis() - time) / (1000 * 3600));
            int minute = (int) ((System.currentTimeMillis() - time) / (1000 * 60) - 60 * hour);
            return hour + "h, " + minute + "min";
        }
    }


    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    public static String enumName(String str) {
        return str.toUpperCase().replace("-", "_");
    }

    public static String formatBoolean(boolean b) {
        return  (b ? ChatColor.GREEN : ChatColor.RED) + "" + b;
    }

    public static String formatList(List<String> list) {
        String result = "";
        for (String str : list)
            result += "\n" + str;

        if (result != "")
            result = result.substring(1);
        return result;
    }


    public static String formatNotation(double notation) {
        String halfStar = "\u2BEA";
        String fullStar = "\u2605";
        String emptyStar = "\u2606";

        String result="";
        int value = (int) (2 * notation);
        for(int i=0;i<5;i++) {
            if(value>=2) {
                result+=fullStar;
            }
            else if(value==1) {
                result+=halfStar;
            }
            else {
                result+=emptyStar;
            }
            value-=2;
        }
        return result;
    }


    public static String chatName(String str) {
        StringBuilder result = new StringBuilder();
        str=str.replace("_"," _").replace("-", " -");
        Arrays.stream(str.split("_")).forEach(intermediary -> Arrays.stream(intermediary.split("-")).forEach(word -> result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase())));
        return result.toString();
    }
}
