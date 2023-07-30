package fr.phoenix.contracts.compat.adventure.resolver;

import fr.phoenix.contracts.compat.adventure.argument.AdventureArgumentQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * This interface is responsible for resolving a tag
 * from a tag name and it's arguments.
 */
@FunctionalInterface
public interface AdventureTagResolver {

    /**
     * Resolve a tag.
     *
     * @param src           the source of the tag.
     * @param argumentQueue the argument queue.
     * @return the resolved tag
     */
    @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argumentQueue);

}
