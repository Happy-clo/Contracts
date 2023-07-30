package fr.phoenix.contracts.compat.adventure.tag.implementation.decorations;

import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class StrikethroughTag extends AdventureTag {

    public StrikethroughTag() {
        super("strikethrough", (src, argumentQueue) -> "§m", true, false,"st");
    }
}
