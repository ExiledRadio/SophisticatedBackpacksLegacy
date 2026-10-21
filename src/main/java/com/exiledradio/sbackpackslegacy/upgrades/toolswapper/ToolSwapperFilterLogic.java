/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.scorelegacy.upgrades.FilterLogicBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

/** One weapon filter and one filter per tool type, each a single ghost item. */
public class ToolSwapperFilterLogic extends FilterLogicBase {
    private static final String WEAPON_FILTER_TAG = "weaponFilter";
    private static final String TOOL_FILTERS_TAG = "toolFilters";
    private ItemStack weaponFilter;
    private final Map<String, ItemStack> toolFilters = new TreeMap<>();

    public ToolSwapperFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler) {
        super(upgrade, saveHandler, "");
        NBTTagCompound tag = upgrade.getTagCompound();
        weaponFilter = tag != null && tag.hasKey(WEAPON_FILTER_TAG) ? new ItemStack(tag.getCompoundTag(WEAPON_FILTER_TAG)) : ItemStack.EMPTY;
        for (String toolType : ToolTypes.getAll()) {
            toolFilters.put(toolType, ItemStack.EMPTY);
        }
        if (tag != null && tag.hasKey(TOOL_FILTERS_TAG)) {
            NBTTagCompound saved = tag.getCompoundTag(TOOL_FILTERS_TAG);
            for (String key : saved.getKeySet()) {
                if (toolFilters.containsKey(key)) {
                    toolFilters.put(key, new ItemStack(saved.getCompoundTag(key)));
                }
            }
        }
    }

    public ItemStack getWeaponFilter() {
        return weaponFilter;
    }

    public ItemStack getToolFilter(String toolType) {
        return toolFilters.getOrDefault(toolType, ItemStack.EMPTY);
    }

    public Set<String> getToolFilterTypes() {
        return toolFilters.keySet();
    }

    public boolean matchesWeaponFilter(ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxStackSize() == 1 && isAllowList() == stackMatchesFilter(stack, weaponFilter);
    }

    public boolean matchesToolFilter(ItemStack stack, String toolType) {
        return !stack.isEmpty() && stack.getMaxStackSize() == 1 && isAllowList() == stackMatchesFilter(stack, getToolFilter(toolType));
    }

    public void setWeaponFilter(ItemStack stack) {
        weaponFilter = stack;
        upgrade.setTagInfo(WEAPON_FILTER_TAG, stack.writeToNBT(new NBTTagCompound()));
        save();
    }

    public void setToolFilter(String toolType, ItemStack stack) {
        toolFilters.put(toolType, stack);
        NBTTagCompound saved = new NBTTagCompound();
        for (Map.Entry<String, ItemStack> entry : toolFilters.entrySet()) {
            saved.setTag(entry.getKey(), entry.getValue().writeToNBT(new NBTTagCompound()));
        }
        upgrade.setTagInfo(TOOL_FILTERS_TAG, saved);
        save();
    }
}
