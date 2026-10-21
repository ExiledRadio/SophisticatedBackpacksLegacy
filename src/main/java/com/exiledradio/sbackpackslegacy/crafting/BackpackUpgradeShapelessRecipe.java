/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.crafting;

import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

/**
 * A diamond backpack and a netherite ingot make the netherite backpack, keeping the backpack's
 * NBT like {@link BackpackUpgradeRecipe}. The original does this on a smithing table, which 1.12
 * doesn't have.
 */
public class BackpackUpgradeShapelessRecipe extends ShapelessOreRecipe {

    public BackpackUpgradeShapelessRecipe(ResourceLocation group, ItemStack result, NonNullList<Ingredient> input) {
        super(group, input, result);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack upgradedBackpack = super.getCraftingResult(inv);
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack oldBackpack = inv.getStackInSlot(slot);
            if (oldBackpack.getItem() instanceof BackpackItem) {
                NBTTagCompound tag = oldBackpack.getTagCompound();
                if (tag != null) {
                    upgradedBackpack.setTagCompound(tag.copy());
                }
                break;
            }
        }
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(upgradedBackpack);
        if (wrapper != null) {
            BackpackItem backpackItem = (BackpackItem) upgradedBackpack.getItem();
            wrapper.setSlotNumbers(backpackItem.getTier().getInventorySlots(), backpackItem.getTier().getUpgradeSlots());
        }
        return upgradedBackpack;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    /** Registered in recipes/_factories.json as "sbackpackslegacy:backpack_upgrade_shapeless". */
    public static class Factory implements IRecipeFactory {
        @Override
        public BackpackUpgradeShapelessRecipe parse(JsonContext context, JsonObject json) {
            ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
            String group = recipe.getGroup();
            return new BackpackUpgradeShapelessRecipe(group.isEmpty() ? null : new ResourceLocation(group), recipe.getRecipeOutput(), recipe.getIngredients());
        }
    }
}
