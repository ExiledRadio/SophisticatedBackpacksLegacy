/*
 * Written for Sophisticated Backpacks Legacy by ExiledRadio, GPLv3.
 */
package com.exiledradio.sbackpackslegacy.compat;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.scorelegacy.compat.MaterialCompat;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

/**
 * The diamond backpack becomes the netherite one on a smithing table, as in the original, where a
 * mod adds one through Backport Core's smithing upgrades (read by reflection, so nothing here
 * needs it). The crafting-grid recipe stays. The result carries {@link BackpackItem#SMITHED_TAG}
 * so taking it gives the backpack the netherite tier's slot counts.
 */
public final class SmithingCompat {
    private SmithingCompat() {}

    /** postInit, after Core has decided whether netherite exists. */
    public static void postInit() {
        if (!Loader.isModLoaded("backportcore") || !MaterialCompat.Material.NETHERITE.isAvailable()) {
            return;
        }
        Item from = Item.REGISTRY.getObject(new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "diamond_backpack"));
        Item to = Item.REGISTRY.getObject(new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "netherite_backpack"));
        if (from == null || to == null) {
            return;
        }
        try {
            ItemStack result = new ItemStack(to);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(BackpackItem.SMITHED_TAG, true);
            result.setTagCompound(tag);
            Class.forName("com.exiledradio.backportcore.smithing.SmithingUpgrades")
                    .getMethod("add", Item.class, Ingredient.class, ItemStack.class, String.class)
                    .invoke(null, from, null, result, SophisticatedBackpacksLegacy.MODID);
            SophisticatedBackpacksLegacy.LOGGER.info("Netherite backpack smithing upgrade added");
        } catch (ReflectiveOperationException | RuntimeException e) {
            SophisticatedBackpacksLegacy.LOGGER.warn("Couldn't add the netherite backpack smithing upgrade", e);
        }
    }
}
