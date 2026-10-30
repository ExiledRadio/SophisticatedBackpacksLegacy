package com.exiledradio.sbackpackslegacy.compat.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Only loaded when Baubles is present. Lets backpacks go in the Baubles body slot. Also covers
 * BaublesEX (same mod id), which adapts this capability through its own wrapper.
 */
public class BaublesIntegration {
    private static final ResourceLocation BAUBLE_CAP_KEY = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "bauble");
    private static final IBauble BACKPACK_BAUBLE = new BackpackBauble();

    static int findBackpackSlot(EntityPlayer player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (handler.getStackInSlot(slot).getItem() instanceof BackpackItem) {
                    return slot;
                }
            }
        }
        return -1;
    }

    static ItemStack getStack(EntityPlayer player, int slot) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        return handler == null || slot < 0 || slot >= handler.getSlots() ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (!(event.getObject().getItem() instanceof BackpackItem)) {
            return;
        }
        event.addCapability(BAUBLE_CAP_KEY, new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(BACKPACK_BAUBLE) : null;
            }
        });
    }

    private static class BackpackBauble implements IBauble {
        @Override
        public BaubleType getBaubleType(ItemStack itemstack) {
            return BaubleType.BODY;
        }

        /**
         * Baubles syncs its slots on its own schedule, which can be after an open-GUI packet. Pinning
         * the UUID and slot counts at equip time means the client's copy already has them.
         */
        @Override
        public void onEquipped(ItemStack stack, EntityLivingBase wearer) {
            BackpackWrapper wrapper = wearer.world.isRemote ? null : CapabilityBackpackWrapper.get(stack);
            if (wrapper != null) {
                wrapper.getOrCreateContentsUuid();
                wrapper.getNumberOfInventorySlots();
                wrapper.getNumberOfUpgradeSlots();
            }
        }

        /** Worn backpacks keep their upgrades running, same as one carried in the inventory. */
        @Override
        public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
            if (wearer.world.isRemote) {
                return;
            }
            BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
            if (wrapper == null || wrapper.getContentsUuid() == null) {
                return;
            }
            for (ITickableUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class)) {
                upgrade.tick(wearer, wearer.world, wearer.getPosition());
            }
        }

        // Baubles only re-sends a slot whose stack was replaced, unless the item asks for auto-sync,
        // which compares the stack with the copy it last sent and re-sends only when they differ.
        // The worn render needs that: the tanks' fluid, the battery charge and the item display
        // live in the stack's NBT, which changes in place.
        @Override
        public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
            return true;
        }
    }
}
