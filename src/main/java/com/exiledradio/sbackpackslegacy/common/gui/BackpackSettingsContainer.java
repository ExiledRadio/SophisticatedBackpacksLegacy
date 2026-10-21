/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.common.gui;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** The settings screen of a backpack, opened from the same context as its GUI. */
public class BackpackSettingsContainer extends SettingsContainerBase {
    private final BackpackContext context;
    private final ItemStack openedStack;

    public BackpackSettingsContainer(EntityPlayer player, BackpackContext context, ItemStack openedStack, BackpackWrapper wrapper) {
        super(player, wrapper);
        this.context = context;
        this.openedStack = openedStack;
    }

    @Nullable
    public static BackpackSettingsContainer create(EntityPlayer player, BackpackContext context) {
        ItemStack stack = context.getBackpackStack(player);
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        return wrapper == null ? null : new BackpackSettingsContainer(player, context, stack, wrapper);
    }

    /** Same rule as the backpack GUI: the very stack that was opened, still within reach. */
    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return context.isWithinReach(playerIn) && context.getBackpackStack(playerIn) == openedStack;
    }

    @Override
    public String getStorageTitle() {
        return openedStack.getDisplayName();
    }

    @Override
    protected void openStorageGuiAgain(EntityPlayerMP playerMP) {
        GuiHandler.openBackpack(playerMP, context);
    }
}
