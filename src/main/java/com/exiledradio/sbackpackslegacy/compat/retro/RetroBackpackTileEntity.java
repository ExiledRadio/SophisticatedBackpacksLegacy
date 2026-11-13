package com.exiledradio.sbackpackslegacy.compat.retro;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A placed Retro backpack, loaded under Retro's tile id. Retro's tile held the backpack's data
 * but not which backpack it was, so the conversion waits for the first tick, when the block
 * (already remapped to ours) can be read without loading the chunk again. From then on it saves
 * as our own tile, and the next load creates a plain {@link BackpackTileEntity}.
 */
public class RetroBackpackTileEntity extends BackpackTileEntity {
    private static final String RETRO_DATA_TAG = "backpackInventory";
    private static final String OUR_TILE_ID = SophisticatedBackpacksLegacy.MODID + ":backpack";
    @Nullable
    private NBTTagCompound pendingRetroData = null;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        pendingRetroData = compound.hasKey(RETRO_DATA_TAG, 10) ? compound.getCompoundTag(RETRO_DATA_TAG) : null;
    }

    @Override
    public void update() {
        if (pendingRetroData != null && !world.isRemote) {
            convert();
        }
        super.update();
    }

    private void convert() {
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof BackpackBlock)) {
            return;
        }
        ItemStack backpack = new ItemStack(BackpackItem.forTier(((BackpackBlock) block).getTier()));
        RetroBackpackImporter.stashBackpackData(backpack, pendingRetroData);
        pendingRetroData = null;
        // Assigns the contents UUID, which runs the import.
        setBackpack(backpack);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound ret = super.writeToNBT(compound);
        if (pendingRetroData != null) {
            // Saved before its first tick: keep Retro's data and id, to convert on the next load.
            ret.setTag(RETRO_DATA_TAG, pendingRetroData.copy());
        } else {
            ret.setString("id", OUR_TILE_ID);
        }
        return ret;
    }
}
