package fr.phoenix.contracts.compat.adventure.tag.implementation.decorations;


import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class UnderlineTag extends AdventureTag {

    public UnderlineTag() {
        super("underlined", (src, argumentQueue) -> "§n", true, false, "u", "underline");
    }

}
