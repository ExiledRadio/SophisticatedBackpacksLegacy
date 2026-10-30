/*
 * Written for Sophisticated Backpacks Legacy by ExiledRadio, GPLv3. Dev environment only.
 */
package com.exiledradio.sbackpackslegacy.compat.wearables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

/** For {@code /sbackpacksdev worn}: puts a stack in a player's Wearables back slot. */
public final class WearablesDevCheck {
    private WearablesDevCheck() {}

    /** What Thut's Wearables makes of the stack, then whether it took it into the back slot. */
    public static String putOnBack(EntityPlayer player, ItemStack stack) {
        PlayerWearables wearables = ThutWearables.getWearables(player);
        if (wearables == null) {
            return "no wearables inventory";
        }
        EnumWearable slot = EnumWearable.getSlot(stack);
        boolean set = wearables.setWearable(EnumWearable.BACK, stack);
        return "thut slot=" + slot + " set=" + set;
    }
}
