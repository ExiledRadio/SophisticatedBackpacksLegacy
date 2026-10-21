/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.upgrades.inception.InceptionUpgradeItem;
import com.exiledradio.scorelegacy.inventory.StorageContents;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import net.minecraft.item.ItemStack;

import java.util.UUID;

/** Backpack rules for what may go into a backpack's inventory. */
public final class BackpackContentsPolicy implements StorageContents.Policy {
    public static final BackpackContentsPolicy INSTANCE = new BackpackContentsPolicy();

    private BackpackContentsPolicy() {}

    /**
     * Backpacks need the Inception upgrade here, can't have one themselves (so nesting stops at
     * one level), and can never be this very backpack (a linked copy of it).
     */
    @Override
    public boolean isItemValid(StorageContents contents, int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return true;
        }
        UpgradeHandler upgrades = contents.getExistingUpgradeHandler();
        if (upgrades == null || !upgrades.hasUpgrade(InceptionUpgradeItem.TYPE)) {
            return false;
        }
        BackpackWrapper inserted = CapabilityBackpackWrapper.get(stack);
        if (inserted == null) {
            return false;
        }
        UUID insertedUuid = inserted.getContentsUuid();
        if (insertedUuid != null && insertedUuid.equals(contents.getUuid())) {
            return false;
        }
        return insertedUuid == null || !inserted.getUpgradeHandler().hasUpgrade(InceptionUpgradeItem.TYPE);
    }

    @Override
    public String[] getNonStackableItems() {
        return ModConfig.upgrades.nonStackableItems;
    }
}
