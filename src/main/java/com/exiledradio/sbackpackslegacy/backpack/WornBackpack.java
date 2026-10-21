package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.sbackpackslegacy.compat.baubles.BaublesCompat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/** The backpack a player has on their back: the Baubles body slot first, then the chest slot. */
public final class WornBackpack {
    private final ItemStack backpack;
    private final BackpackContext context;
    private final boolean inChestSlot;

    private WornBackpack(ItemStack backpack, BackpackContext context, boolean inChestSlot) {
        this.backpack = backpack;
        this.context = context;
        this.inChestSlot = inChestSlot;
    }

    @Nullable
    public static WornBackpack find(EntityPlayer player) {
        if (BaublesCompat.isLoaded()) {
            BackpackContext bauble = new BackpackContext.Item(BackpackContext.InventoryKind.BAUBLE, BaublesCompat.getBodySlot());
            ItemStack stack = bauble.getBackpackStack(player);
            if (!stack.isEmpty()) {
                return new WornBackpack(stack, bauble, false);
            }
        }
        BackpackContext chest = new BackpackContext.Item(BackpackContext.InventoryKind.ARMOR, 0);
        ItemStack stack = chest.getBackpackStack(player);
        return stack.isEmpty() ? null : new WornBackpack(stack, chest, true);
    }

    public ItemStack getBackpack() {
        return backpack;
    }

    public BackpackContext getContext() {
        return context;
    }

    /** False for the Baubles slot, where armour in the chest slot may sit under the backpack. */
    public boolean isInChestSlot() {
        return inChestSlot;
    }
}
