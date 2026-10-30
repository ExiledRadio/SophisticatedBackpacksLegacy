package com.exiledradio.sbackpackslegacy.compat.wearables;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Only loaded when Thut's Wearables is present. Lets backpacks go in its back slot, the way its
 * own config-listed wearables do (a capability on the stack). Our layer draws the worn backpack.
 */
public class WearablesIntegration {
    private static final ResourceLocation WEARABLE_CAP_KEY = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "wearable");
    private static final IActiveWearable BACKPACK_WEARABLE = new BackpackWearable();

    static ItemStack getBackStack(EntityPlayer player) {
        PlayerWearables wearables = ThutWearables.getWearables(player);
        ItemStack stack = wearables == null ? null : wearables.getWearable(EnumWearable.BACK);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (!(event.getObject().getItem() instanceof BackpackItem)) {
            return;
        }
        event.addCapability(WEARABLE_CAP_KEY, new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == IActiveWearable.WEARABLE_CAP;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                return capability == IActiveWearable.WEARABLE_CAP ? IActiveWearable.WEARABLE_CAP.cast(BACKPACK_WEARABLE) : null;
            }
        });
    }

    private static class BackpackWearable implements IActiveWearable {
        @Override
        public EnumWearable getSlot(ItemStack stack) {
            return EnumWearable.BACK;
        }

        /** Nothing here: {@code BackpackLayer} draws worn backpacks, wherever they are worn. */
        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks) {
        }

        /** As with Baubles: pin the UUID and slot counts at put-on time, before any open-GUI packet. */
        @Override
        public void onPutOn(EntityLivingBase wearer, ItemStack stack, EnumWearable slot, int subIndex) {
            BackpackWrapper wrapper = wearer.world.isRemote ? null : CapabilityBackpackWrapper.get(stack);
            if (wrapper != null) {
                wrapper.getOrCreateContentsUuid();
                wrapper.getNumberOfInventorySlots();
                wrapper.getNumberOfUpgradeSlots();
            }
        }

        /** Worn backpacks keep their upgrades running, same as one carried in the inventory. */
        @Override
        public void onUpdate(EntityLivingBase wearer, ItemStack stack, EnumWearable slot, int subIndex) {
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
    }
}
