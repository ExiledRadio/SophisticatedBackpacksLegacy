/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.scorelegacy.api.IPickupResponseUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.compat.baubles.BaublesCompat;
import com.exiledradio.sbackpackslegacy.compat.wearables.WearablesCompat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/** Walks every backpack a player carries: worn (Baubles, Thut's Wearables, chest), main inventory, offhand. */
public final class PlayerBackpacks {
    private PlayerBackpacks() {}

    /** Calls the visitor for each backpack until it returns true. */
    public static void forEach(EntityPlayer player, Predicate<BackpackWrapper> visitor) {
        if (BaublesCompat.isLoaded() && visit(BaublesCompat.getStack(player, BaublesCompat.findBackpackSlot(player)), visitor)) {
            return;
        }
        if (WearablesCompat.isLoaded() && visit(WearablesCompat.getBackStack(player), visitor)) {
            return;
        }
        for (ItemStack stack : player.inventory.armorInventory) {
            if (visit(stack, visitor)) {
                return;
            }
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (visit(stack, visitor)) {
                return;
            }
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            if (visit(stack, visitor)) {
                return;
            }
        }
    }

    private static boolean visit(ItemStack stack, Predicate<BackpackWrapper> visitor) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return false;
        }
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        // Backpacks never opened have no contents and therefore no upgrades to run.
        return wrapper != null && wrapper.getContentsUuid() != null && visitor.test(wrapper);
    }

    /** Offers a picked-up stack to the backpack's pickup upgrades; returns what is left. */
    public static ItemStack runPickupOnBackpack(World world, @Nullable EntityPlayer player, ItemStack remainingStack, BackpackWrapper wrapper, boolean simulate) {
        ItemStack remaining = remainingStack;
        for (IPickupResponseUpgrade pickupUpgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IPickupResponseUpgrade.class)) {
            int countBefore = remaining.getCount();
            remaining = pickupUpgrade.pickup(world, remaining, simulate);
            if (!simulate && player != null && remaining.getCount() != countBefore) {
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                        (world.rand.nextFloat() - world.rand.nextFloat()) * 1.4F + 2.0F);
            }
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return remaining;
    }
}
