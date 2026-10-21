package com.exiledradio.sbackpackslegacy.api;

import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

/**
 * Backpack item stacks expose their {@link BackpackWrapper} through this capability. It has no
 * storage of its own: everything persistent lives in the stack NBT or in BackpackStorage.
 */
public final class CapabilityBackpackWrapper {
    @CapabilityInject(BackpackWrapper.class)
    public static Capability<BackpackWrapper> BACKPACK_WRAPPER = null;

    private CapabilityBackpackWrapper() {}

    public static void register() {
        CapabilityManager.INSTANCE.register(BackpackWrapper.class, new Capability.IStorage<BackpackWrapper>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<BackpackWrapper> capability, BackpackWrapper instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<BackpackWrapper> capability, BackpackWrapper instance, EnumFacing side, NBTBase nbt) {
                //noop
            }
        }, () -> {
            throw new UnsupportedOperationException("Backpack wrappers only exist on backpack stacks");
        });
    }

    @Nullable
    public static BackpackWrapper get(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(BACKPACK_WRAPPER, null);
    }
}
