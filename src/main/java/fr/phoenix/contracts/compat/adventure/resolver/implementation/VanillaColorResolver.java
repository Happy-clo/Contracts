package fr.phoenix.contracts.compat.adventure.resolver.implementation;

import fr.phoenix.contracts.compat.adventure.argument.AdventureArgumentQueue;
import fr.phoenix.contracts.compat.adventure.resolver.AdventureTagResolver;
import fr.phoenix.contracts.utils.AdventureUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class VanillaColorResolver implements AdventureTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String tag, @NotNull AdventureArgumentQueue argumentQueue) {
        return AdventureUtils.getByName(tag)
                .map(chatColor -> String.format("%c%c", ChatColor.COLOR_CHAR, chatColor.getChar()))
                .orElse(null);
    }
}
