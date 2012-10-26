/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow.handler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author ucchy
 *
 */
public class ItemHandler {

    protected boolean hasItem(Player player, int id, int amount) {
        Inventory inv = player.getInventory();
        return inv.contains(id, amount);
    }

    protected void chargeItem(Player player, int id, int amount) {
        Inventory inv = player.getInventory();
        ItemStack item = new ItemStack(id, amount);
        inv.removeItem(item);
    }

    protected void addItem(Player player, int id, int amount) {
        Inventory inv = player.getInventory();
        ItemStack item = new ItemStack(id, amount);
        inv.addItem(item);
    }

    protected String getDisplayName(int id) {
        return Material.getMaterial(id).name();
    }
}
