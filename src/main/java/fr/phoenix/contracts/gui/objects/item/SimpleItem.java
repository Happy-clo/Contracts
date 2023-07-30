package fr.phoenix.contracts.gui.objects.item;

import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;

/**
 * An inventory item that has no particular placeholder
 * yet it DOES support PAPI placeholders.
 */
public class SimpleItem<T extends GeneratedInventory> extends InventoryItem<T> {
    public SimpleItem(ConfigurationSection config) {
        super(config);
    }

    @Override
    public Placeholders getPlaceholders(T inv, int n) {
        return new Placeholders();
    }
}
