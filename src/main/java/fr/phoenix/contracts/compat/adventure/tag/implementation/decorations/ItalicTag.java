package fr.phoenix.contracts.compat.adventure.tag.implementation.decorations;


import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class ItalicTag extends AdventureTag {

    public ItalicTag() {
        super("italic", (src, argumentQueue) -> "§o", true, false,"i");
    }
}
