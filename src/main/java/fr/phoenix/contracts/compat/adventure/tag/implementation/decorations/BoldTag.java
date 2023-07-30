package fr.phoenix.contracts.compat.adventure.tag.implementation.decorations;


import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class BoldTag extends AdventureTag {

    public BoldTag() {
        super("bold", (src, argumentQueue) -> "§l", true, false,"b");
    }
}
