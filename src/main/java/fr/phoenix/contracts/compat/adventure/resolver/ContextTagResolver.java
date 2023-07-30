package fr.phoenix.contracts.compat.adventure.resolver;

import fr.phoenix.contracts.compat.adventure.argument.AdventureArgumentQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * This interface is responsible for resolving a context tag.
 * The "context" is everything that is between the open and close tags
 * or the end of the string if there is no close tag.
 */
@FunctionalInterface
public interface ContextTagResolver extends AdventureTagResolver {

    /**
     * Resolve a tag.
     *
     * @param src           the source of the tag.
     * @param argumentQueue the argument queue.
     * @param context       the context of the tag.
     * @param decorations   the decorations of the tag.
     * @return the resolved tag
     */
    @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argumentQueue, @NotNull String context, @NotNull List<String> decorations);

    @Override
    default @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argumentQueue) {
        return null;
    }
}
