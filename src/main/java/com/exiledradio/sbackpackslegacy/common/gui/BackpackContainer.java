/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.common.gui;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The backpack GUI: Core's storage container, opened from a {@link BackpackContext} (hand,
 * inventory slot, Baubles, worn, or placed block).
 */
public class BackpackContainer extends StorageContainerBase {
    private final BackpackContext context;
    // The exact stack instance that was opened. If the context ever points at a different
    // instance (moved, dropped, replaced, broken), the GUI closes.
    private final ItemStack openedStack;

    public BackpackContainer(EntityPlayer player, BackpackContext context, ItemStack openedStack, BackpackWrapper backpackWrapper) {
        super(player, backpackWrapper, context.getLockedMainInventorySlot());
        this.context = context;
        this.openedStack = openedStack;
        if (!player.world.isRemote) {
            updateRenderInfo();
        }
    }

    /** Builds the container for a context, or null when no backpack is there any more. */
    @Nullable
    public static BackpackContainer create(EntityPlayer player, BackpackContext context) {
        ItemStack stack = context.getBackpackStack(player);
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        return wrapper == null ? null : new BackpackContainer(player, context, stack, wrapper);
    }

    public BackpackContext getContext() {
        return context;
    }

    public ItemStack getOpenedStack() {
        return openedStack;
    }

    public BackpackWrapper getBackpackWrapper() {
        return (BackpackWrapper) storageWrapper;
    }

    /**
     * Checked by the server every tick; returning false closes the GUI. The context must still
     * point at the very same stack instance that was opened, or the backpack has gone somewhere.
     */
    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return context.isWithinReach(playerIn) && context.getBackpackStack(playerIn) == openedStack;
    }

    @Nullable
    @Override
    public BlockPos getStorageBlockPos() {
        return context instanceof BackpackContext.Block ? new BlockPos(context.getX(), context.getY(), context.getZ()) : null;
    }

    @Override
    public String getStorageTitle() {
        return openedStack.getDisplayName();
    }

    @Override
    protected void openSettingsGui(EntityPlayerMP playerMP) {
        GuiHandler.openBackpackSettings(playerMP, context);
    }

    @Override
    protected void openGuiAgain(EntityPlayerMP playerMP) {
        GuiHandler.openBackpack(playerMP, context);
    }

    /**
     * Tanks and the battery show on the backpack model. The look lives in the stack NBT so
     * every client can render it, so it is refreshed whenever the GUI (re)opens after an
     * upgrade change; a placed backpack also needs its block re-rendered.
     */
    private void updateRenderInfo() {
        ItemStack stack = context.getBackpackStack(player);
        if (stack.isEmpty() || !BackpackRenderInfo.update(stack, storageWrapper.getUpgradeHandler())) {
            return;
        }
        BlockPos pos = getStorageBlockPos();
        if (pos != null) {
            TileEntity tile = player.world.getTileEntity(pos);
            if (tile != null) {
                tile.markDirty();
            }
            IBlockState state = player.world.getBlockState(pos);
            player.world.notifyBlockUpdate(pos, state, state, 3);
        }
    }
}
