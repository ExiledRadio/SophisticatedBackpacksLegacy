/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.compat.jei;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTier;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Backpacks' own JEI additions, as in the original: dyed backpacks are separate entries (one per
 * colour pair), example dye recipes in the crafting category, and the Crafting Upgrade listed as a
 * crafting catalyst. Screen handling (exclusion areas, filters, recipe transfer) is in Core.
 */
@JEIPlugin
public class BackpacksJeiPlugin implements IModPlugin {
    private static final String CLOTH_COLOR_TAG = "clothColor";
    private static final String BORDER_COLOR_TAG = "borderColor";
    // By dye metadata; 1.12's names differ ("silver"), so they can't be built from the enum.
    private static final String[] DYE_ORE_NAMES = {
            "dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue", "dyeYellow", "dyeLime", "dyePink", "dyeGray",
            "dyeLightGray", "dyeCyan", "dyePurple", "dyeBlue", "dyeBrown", "dyeGreen", "dyeRed", "dyeBlack"
    };

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
        // Only the colours make a different entry; contents UUIDs, upgrades and render info don't.
        ISubtypeRegistry.ISubtypeInterpreter colors = stack -> {
            NBTTagCompound tag = stack.getTagCompound();
            int cloth = tag != null && tag.hasKey(CLOTH_COLOR_TAG) ? tag.getInteger(CLOTH_COLOR_TAG) : BackpackWrapper.DEFAULT_CLOTH_COLOR;
            int border = tag != null && tag.hasKey(BORDER_COLOR_TAG) ? tag.getInteger(BORDER_COLOR_TAG) : BackpackWrapper.DEFAULT_BORDER_COLOR;
            return "{clothColor:" + cloth + ",borderColor:" + border + "}";
        };
        for (BackpackTier tier : BackpackTier.values()) {
            BackpackItem item = BackpackItem.forTier(tier);
            if (item != null) {
                subtypeRegistry.registerSubtypeInterpreter(item, colors);
            }
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        BackpackItem backpack = BackpackItem.forTier(BackpackTier.LEATHER);
        if (backpack != null) {
            registry.addRecipes(getDyeRecipes(backpack), VanillaRecipeCategoryUid.CRAFTING);
        }
        Item craftingUpgrade = Item.REGISTRY.getObject(new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "crafting_upgrade"));
        if (craftingUpgrade != null) {
            registry.addRecipeCatalyst(new ItemStack(craftingUpgrade), VanillaRecipeCategoryUid.CRAFTING);
        }
    }

    /**
     * Display-only copies of the dye recipe (the real one is dynamic, so JEI can't show it): the
     * backpack over one dye for each colour, and the original's two-colour example.
     */
    private static List<IRecipe> getDyeRecipes(Item backpack) {
        List<IRecipe> recipes = new ArrayList<>();
        Ingredient undyed = Ingredient.fromStacks(new ItemStack(backpack));
        for (EnumDyeColor color : EnumDyeColor.values()) {
            recipes.add(new ShapedRecipes("", 1, 2, NonNullList.from(Ingredient.EMPTY, undyed, dye(color)),
                    BackpackItem.dyedSingleColor(backpack, color)));
        }
        recipes.add(new ShapedRecipes("", 3, 2, NonNullList.from(Ingredient.EMPTY,
                dye(EnumDyeColor.YELLOW), undyed, Ingredient.EMPTY,
                dye(EnumDyeColor.LIME), dye(EnumDyeColor.BLUE), dye(EnumDyeColor.BLACK)),
                BackpackItem.dyedMultipleColors(backpack)));
        return recipes;
    }

    /** Any dye of that colour, by the same ore dictionary names the real recipe accepts. */
    static Ingredient dye(EnumDyeColor color) {
        return new OreIngredient(DYE_ORE_NAMES[color.getMetadata()]);
    }
}
