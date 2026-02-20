package com.hibiscusmc.hmcclaims.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtil {

    public static ItemStack build(Material material, String name, List<String> lore) {
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        meta.customName(Text.parseItem(name));
        meta.lore(lore.stream().map(Text::parseItem).toList());

        item.setItemMeta(meta);
        return item;
    }
}