package fr.phoenix.contracts.compat.adventure.tag.implementation;

import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class NewlineTag extends AdventureTag {

    public NewlineTag() {
        super("newline", (src, argumentQueue) -> "\n", true, false,"br");
    }
}
