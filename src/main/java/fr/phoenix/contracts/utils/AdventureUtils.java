package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.Contracts;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
@UtilityClass
public class AdventureUtils {

    /**
     * Find a {@link ChatColor} from it's name.
     * Note: This method is case insensitive.
     *
     * @param name The name of the color.
     * @return An optional containing the color if found.
     */
    public static @NotNull Optional<ChatColor> getByName(@NotNull String name) {
        return Arrays.stream(ChatColor.values())
                .filter(chatColor -> chatColor.name().equalsIgnoreCase(name))
                .filter(ChatColor::isColor)
                .findFirst();
    }

    /**
     * Find a {@link ChatColor} from it's hexidecimal value.
     *
     * @param hex The hexidecimal value of the color as a string.
     * @return An optional containing the color if found.
     */
    public static @NotNull Optional<net.md_5.bungee.api.ChatColor> getByHex(@NotNull String hex) {
        if (hex.length() == 7 && hex.startsWith("#"))
            hex = hex.substring(1);
        // TODO: uncomment the following line
        if (hex.length() != 6 /* || MythicLib.plugin.getVersion().isBelowOrEqual(1, 15) */)
            return Optional.empty();
        try {
            return Optional.of(net.md_5.bungee.api.ChatColor.of('#' + hex));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Run a task asynchronously.
     *
     * @param runnable The task to run.
     */
    public static void runAsync(Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskAsynchronously(Contracts.plugin);
    }

    /**
     * Run a task asynchronously and supply a result.
     *
     * @param supplier The task to run.
     * @param <U>      The type of the result.
     * @return A completable future containing the result.
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        if (!Bukkit.isPrimaryThread())
            return CompletableFuture.completedFuture(supplier.get());
        CompletableFuture<U> future = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    future.complete(supplier.get());
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        }.runTaskAsynchronously(Contracts.plugin);
        return future;
    }

    /**
     * Convert a string (hex color or color name) to a {@link Color} using {@link net.md_5.bungee.api.ChatColor}.
     *
     * @param raw The color to convert.
     * @return The converted color.
     */
    public static Color color(String raw) {
        try {
            net.md_5.bungee.api.ChatColor chatColor = net.md_5.bungee.api.ChatColor.of(raw);
            return chatColor.getColor();
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    /**
     * Parse and color a string then set it as the display name of an item.
     *
     * @param meta The item meta to set the display name of.
     * @param name The name to set.
     * @return The item meta.
     */
    public static ItemMeta setDisplayName(ItemMeta meta, String name) {
        meta.setDisplayName(Contracts.plugin.parseColors(name));
        return meta;
    }

    /**
     * Parse and color a list of strings then set it as the lore of an item.
     *
     * @param meta The item meta to set the lore of.
     * @param lore The lore to set.
     * @return The item meta.
     */
    public static ItemMeta setLore(ItemMeta meta, List<String> lore) {
        meta.setLore(Contracts.plugin.parseColors(lore));
        return meta;
    }

}
