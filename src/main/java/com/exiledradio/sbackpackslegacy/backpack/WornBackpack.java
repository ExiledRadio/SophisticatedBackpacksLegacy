package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.sbackpackslegacy.compat.baubles.BaublesCompat;
import com.exiledradio.sbackpackslegacy.compat.wearables.WearablesCompat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/** The backpack a player has on their back: Baubles first, then Thut's Wearables' back slot, then the chest slot. */
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
            int slot = BaublesCompat.findBackpackSlot(player);
            if (slot >= 0) {
                BackpackContext bauble = new BackpackContext.Item(BackpackContext.InventoryKind.BAUBLE, slot);
                ItemStack stack = bauble.getBackpackStack(player);
                if (!stack.isEmpty()) {
                    return new WornBackpack(stack, bauble, false);
                }
            }
        }
        if (WearablesCompat.isLoaded()) {
            BackpackContext wearable = new BackpackContext.Item(BackpackContext.InventoryKind.WEARABLE, 0);
            ItemStack stack = wearable.getBackpackStack(player);
            if (!stack.isEmpty()) {
                return new WornBackpack(stack, wearable, false);
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

    /** False for the Baubles and Wearables slots, where armour in the chest slot may sit under the backpack. */
    public boolean isInChestSlot() {
        return inChestSlot;
    }
}
