/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * 1.12 has no tool type registry, only the tool class strings items report. The set of types
 * (for the filter slots) is collected from every registered item the first time it's needed,
 * which is after registration on both sides, so client and server build the same slots.
 */
public final class ToolTypes {
    public static final String PICKAXE = "pickaxe";
    public static final String AXE = "axe";
    public static final String SHOVEL = "shovel";
    public static final String SWORD = "sword";
    private static volatile Set<String> allToolTypes = null;

    private ToolTypes() {}

    public static Set<String> getAll() {
        if (allToolTypes == null) {
            Set<String> types = new TreeSet<>();
            types.add(PICKAXE);
            types.add(AXE);
            types.add(SHOVEL);
            for (Item item : Item.REGISTRY) {
                try {
                    types.addAll(item.getToolClasses(new ItemStack(item)));
                } catch (RuntimeException e) {
                    // Some modded items can't answer for a stack with no NBT; they just don't add a type.
                }
            }
            types.remove(SWORD);
            allToolTypes = Collections.unmodifiableSet(types);
        }
        return allToolTypes;
    }

    public static Set<String> of(ItemStack stack) {
        return stack.isEmpty() ? Collections.emptySet() : stack.getItem().getToolClasses(stack);
    }
}
