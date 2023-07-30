package fr.phoenix.contracts.compat.adventure.tag.implementation.decorations;


import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class ResetTag extends AdventureTag {

    public ResetTag() {
        super("reset", (src, argumentQueue) -> "§r", true, true,"r");
    }
}
