/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A placed backpack. Holds the backpack stack itself; its contents stay in BackpackStorage. */
public class BackpackTileEntity extends TileEntity implements ITickable {
    private static final String BACKPACK_DATA_TAG = "backpackData";

    private ItemStack backpack = ItemStack.EMPTY;
    @Nullable
    private BackpackWrapper backpackWrapper = null;

    @Nullable
    public static BackpackTileEntity at(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof BackpackTileEntity ? (BackpackTileEntity) te : null;
    }

    public void setBackpack(ItemStack backpack) {
        this.backpack = backpack;
        backpackWrapper = CapabilityBackpackWrapper.get(backpack);
        if (backpackWrapper != null) {
            // Pin the UUID and slot counts now (no-ops on the client), so the first sync to
            // clients already carries them and their containers match the server's.
            backpackWrapper.getOrCreateContentsUuid();
            backpackWrapper.getNumberOfInventorySlots();
            backpackWrapper.getNumberOfUpgradeSlots();
            backpackWrapper.setBackpackSaveHandler(this::onBackpackChanged);
        }
        onBackpackChanged();
    }

    /**
     * Takes the backpack out, leaving the tile empty. Every path that turns the block back into
     * an item goes through here first, so breakBlock can never drop a second copy.
     */
    public ItemStack removeBackpack() {
        ItemStack removed = backpack;
        backpack = ItemStack.EMPTY;
        backpackWrapper = null;
        markDirty();
        return removed;
    }

    public ItemStack getBackpack() {
        return backpack;
    }

    @Nullable
    public BackpackWrapper getBackpackWrapper() {
        return backpackWrapper;
    }

    private void onBackpackChanged() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public void update() {
        if (world.isRemote || backpackWrapper == null || backpackWrapper.getContentsUuid() == null) {
            return;
        }
        for (ITickableUpgrade upgrade : backpackWrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class)) {
            upgrade.tick(null, world, pos);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        setBackpack(new ItemStack(compound.getCompoundTag(BACKPACK_DATA_TAG)));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound ret = super.writeToNBT(compound);
        ret.setTag(BACKPACK_DATA_TAG, backpack.writeToNBT(new NBTTagCompound()));
        return ret;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        setBackpack(new ItemStack(pkt.getNbtCompound().getCompoundTag(BACKPACK_DATA_TAG)));
        // Colours come from the tile, so the chunk has to be re-rendered to pick them up.
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        // Rotating the backpack changes the state but must not throw the tile (and backpack) away.
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (backpackWrapper != null) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return true;
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return backpackWrapper.getFluidHandler() != null;
            }
            if (capability == CapabilityEnergy.ENERGY) {
                return backpackWrapper.getEnergyStorage() != null;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && backpackWrapper != null) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(backpackWrapper.getInventoryForInputOutput());
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && backpackWrapper != null) {
            StorageFluidHandler fluidHandler = backpackWrapper.getFluidHandler();
            if (fluidHandler != null) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
        }
        if (capability == CapabilityEnergy.ENERGY && backpackWrapper != null) {
            IEnergyStorage energyStorage = backpackWrapper.getEnergyStorage();
            if (energyStorage != null) {
                return CapabilityEnergy.ENERGY.cast(energyStorage);
            }
        }
        return super.getCapability(capability, facing);
    }
}
