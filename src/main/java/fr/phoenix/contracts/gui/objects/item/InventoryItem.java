package fr.phoenix.contracts.gui.objects.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.utils.ContractsUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class InventoryItem<T extends GeneratedInventory> {
    private final String id, function;
    private final List<Integer> slots = new ArrayList<>();

    private final Material material;
    private final String name,texture;
    private final List<String> lore;
    private final int modelData;
    private final boolean hideFlags;
    protected final InventoryItem parent;


    public InventoryItem(InventoryItem parent, ConfigurationSection config,Material material) {
        this.id = config.getName();
        this.function = config.getString("function", "").toLowerCase();
        this.material = material!=null?material:config.getString("item") != null ? Material.valueOf(ContractsUtils.enumName(config.getString("item"))) : Material.AIR;
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
        this.hideFlags = config.getBoolean("hide-flags");
        this.modelData = config.getInt("model-data");
        this.parent=parent;
        this.texture = config.getString("texture");
        config.getStringList("slots").forEach(str -> slots.add(Integer.parseInt(str)));

    }

    public InventoryItem(InventoryItem<? extends GeneratedInventory> parent,ConfigurationSection config) {
        this(parent,config,null);
    }

    public InventoryItem(ConfigurationSection config) {
        this(null,config);
        }


    public String getId() {
        return id;
    }

    public boolean hasFunction() {
        return !function.isEmpty();
    }

    public String getFunction() {
        return function;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean hideFlags() {
        return hideFlags;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getName() {
        return name;
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    public List<String> getLore() {
        return lore;
    }

    public int getModelData() {
        return modelData;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public boolean hasDifferentDisplay() {
        return false;
    }

    public void display(Inventory inv, T generated) {
        generated.addLoaded(this);

        if (!hasDifferentDisplay()) {
            ItemStack display = getDisplayedItem(generated, 0);
            for (int slot : getSlots())
                inv.setItem(slot, display);

        } else
            for (int j = 0; j < slots.size(); j++)
                inv.setItem(slots.get(j), getDisplayedItem(generated, j));
    }

    /**
     * @param inv Generated inventory
     * @return If the item can be displayed in this inventory
     */
    public boolean isDisplayed(T inv) {
        return true;
    }

    /**
     * @param inv Generated inventory being opened by a fr.phoenix.contracts.player
     * @param n   Some items are grouped, like the item 'stock' in the stock list
     *            as they are multiple stocks employer display yet only ONE inventory item
     *            gives the template. This is the index of the item being displayed.
     * @return Item that will be displayed in the generated inventory
     */

    public ItemStack getDisplayedItem(T inv, int n) {
        return getDisplayedItem(inv, n, null);
    }


    public ItemStack getDisplayedItem(T inv, int n, Material specificMaterial) {
        if (specificMaterial == null)
            specificMaterial = material;
        // Support for AIR
        if (specificMaterial == Material.AIR)
            return new ItemStack(Material.AIR);

        Placeholders placeholders = getPlaceholders(inv, n);
        ItemStack item = new ItemStack(specificMaterial);
        ItemMeta meta = item.getItemMeta();

        if (hasName())
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',placeholders.apply(inv.getPlayer(), getName())));

        if (hideFlags())
            meta.addItemFlags(ItemFlag.values());

        if (texture != null && meta instanceof SkullMeta) applyTexture(texture, (SkullMeta) meta);

        if (hasLore()) {
            List<String> lore = new ArrayList<>();
            for(String line:getLore()) {
                //Enables to have placeholders for a list of item. Color codes for the placeholders also (e.g player can introduce color codes in their input).
                String[] parsed= ChatColor.translateAlternateColorCodes('&',placeholders.apply(inv.getPlayer(), line)).split("\n");
                for(String str: parsed) {
                    lore.add(ChatColor.GRAY +str);
                }
            }
            meta.setLore(lore);
        }

        meta.setCustomModelData(getModelData());

        item.setItemMeta(meta);
        return item;
    }


    private void applyTexture(String value, SkullMeta meta) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            Contracts.log(Level.WARNING, "Could not apply item texture value of " + getId());
        }
    }

    public abstract Placeholders getPlaceholders(T inv, int n);
}
