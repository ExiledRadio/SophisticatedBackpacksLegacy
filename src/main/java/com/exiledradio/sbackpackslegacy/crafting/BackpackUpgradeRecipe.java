/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.crafting;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

/**
 * A shaped recipe that turns one backpack into the next tier. The result keeps the old stack's
 * NBT, so it is the same backpack (same UUID, same contents) with more slots.
 */
public class BackpackUpgradeRecipe extends ShapedOreRecipe {

    public BackpackUpgradeRecipe(ResourceLocation group, ItemStack result, CraftingHelper.ShapedPrimer primer) {
        super(group, result, primer);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack upgradedBackpack = super.getCraftingResult(inv);
        ItemStack oldBackpack = findBackpack(inv);
        NBTTagCompound tag = oldBackpack.getTagCompound();
        if (tag != null) {
            upgradedBackpack.setTagCompound(tag.copy());
        }
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(upgradedBackpack);
        if (wrapper != null) {
            BackpackItem backpackItem = (BackpackItem) upgradedBackpack.getItem();
            wrapper.setSlotNumbers(backpackItem.getTier().getInventorySlots(), backpackItem.getTier().getUpgradeSlots());
        }
        return upgradedBackpack;
    }

    private static ItemStack findBackpack(InventoryCrafting inv) {
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack slotStack = inv.getStackInSlot(slot);
            if (slotStack.getItem() instanceof BackpackItem) {
                return slotStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    /** Registered in recipes/_factories.json as "sbackpackslegacy:backpack_upgrade". */
    public static class Factory implements IRecipeFactory {
        @Override
        public BackpackUpgradeRecipe parse(JsonContext context, JsonObject json) {
            ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);
            CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
            primer.width = recipe.getRecipeWidth();
            primer.height = recipe.getRecipeHeight();
            primer.mirrored = true;
            primer.input = recipe.getIngredients();
            String group = recipe.getGroup();
            return new BackpackUpgradeRecipe(group.isEmpty() ? null : new ResourceLocation(group), recipe.getRecipeOutput(), primer);
        }
    }
}
