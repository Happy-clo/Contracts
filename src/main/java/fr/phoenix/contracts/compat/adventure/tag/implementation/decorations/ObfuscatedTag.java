package fr.phoenix.contracts.compat.adventure.tag.implementation.decorations;

import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class ObfuscatedTag extends AdventureTag {

    public ObfuscatedTag() {
        super("obfuscated", (src, argumentQueue) -> "§k", true, false,"obf");
    }
}
