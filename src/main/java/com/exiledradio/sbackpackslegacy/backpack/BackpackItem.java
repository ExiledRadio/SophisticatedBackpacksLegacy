/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.sbackpackslegacy.common.gui.GuiHandler;
import com.exiledradio.sbackpackslegacy.crafting.BackpackDyeRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import com.exiledradio.sbackpackslegacy.upgrades.everlasting.EverlastingBackpackItemEntity;
import com.exiledradio.sbackpackslegacy.upgrades.everlasting.EverlastingUpgradeItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import com.exiledradio.sbackpackslegacy.ModConfig;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackpackItem extends Item {
    private static final Map<BackpackTier, BackpackItem> BY_TIER = new EnumMap<>(BackpackTier.class);
    private final BackpackTier tier;

    public BackpackItem(BackpackTier tier, CreativeTabs tab) {
        this.tier = tier;
        setRegistryName(SophisticatedBackpacksLegacy.MODID, tier.getRegistryName());
        setTranslationKey(SophisticatedBackpacksLegacy.MODID + "." + tier.getRegistryName());
        setMaxStackSize(1);
        setCreativeTab(tab);
        BY_TIER.put(tier, this);
    }

    public static BackpackItem forTier(BackpackTier tier) {
        return BY_TIER.get(tier);
    }

    public BackpackTier getTier() {
        return tier;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab) || !tier.isAvailable()) {
            return;
        }
        items.add(new ItemStack(this));
        if (tier != BackpackTier.LEATHER) {
            return;
        }
        // The original shows every dye colour of the leather backpack in the creative tab, plus
        // the two-colour example from its JEI dye recipe.
        for (EnumDyeColor color : EnumDyeColor.values()) {
            items.add(dyedSingleColor(this, color));
        }
        items.add(dyedMultipleColors(this));
    }

    /** What dyeing an undyed backpack with one dye gives (the colours the dye recipe computes). */
    public static ItemStack dyedSingleColor(Item backpackItem, EnumDyeColor color) {
        List<EnumDyeColor> dyes = Collections.singletonList(color);
        return withColors(backpackItem,
                BackpackDyeRecipe.calculateColor(BackpackWrapper.DEFAULT_CLOTH_COLOR, BackpackWrapper.DEFAULT_CLOTH_COLOR, dyes),
                BackpackDyeRecipe.calculateColor(BackpackWrapper.DEFAULT_BORDER_COLOR, BackpackWrapper.DEFAULT_BORDER_COLOR, dyes));
    }

    /** The original's example: yellow, lime and blue on the cloth, blue and black on the trim. */
    public static ItemStack dyedMultipleColors(Item backpackItem) {
        return withColors(backpackItem,
                BackpackDyeRecipe.calculateColor(BackpackWrapper.DEFAULT_CLOTH_COLOR, BackpackWrapper.DEFAULT_CLOTH_COLOR,
                        Arrays.asList(EnumDyeColor.BLUE, EnumDyeColor.YELLOW, EnumDyeColor.LIME)),
                BackpackDyeRecipe.calculateColor(BackpackWrapper.DEFAULT_BORDER_COLOR, BackpackWrapper.DEFAULT_BORDER_COLOR,
                        Arrays.asList(EnumDyeColor.BLUE, EnumDyeColor.BLACK)));
    }

    private static ItemStack withColors(Item backpackItem, int clothColor, int borderColor) {
        ItemStack stack = new ItemStack(backpackItem);
        new BackpackWrapper(stack).setColors(clothColor, borderColor);
        return stack;
    }

    // Item.addInformation is itself client-only, so FML strips this override on a dedicated
    // server along with the client-only ITooltipFlag in its signature.
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (!net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
            tooltip.add(com.exiledradio.sbackpackslegacy.client.BackpackTooltipRenderer.getShiftHint());
        }
        if (flagIn.isAdvanced()) {
            BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
            UUID uuid = wrapper == null ? null : wrapper.getContentsUuid();
            if (uuid != null) {
                tooltip.add(TextFormatting.DARK_GRAY + "UUID: " + uuid);
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            BackpackContext context = hand == EnumHand.MAIN_HAND
                    ? new BackpackContext.Item(BackpackContext.InventoryKind.MAIN, player.inventory.currentItem)
                    : new BackpackContext.Item(BackpackContext.InventoryKind.OFFHAND, 0);
            GuiHandler.openBackpack(player, context);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /** Sneak + right-click on a block places the backpack. */
    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        ItemStack backpack = player.getHeldItem(hand);
        if (BackpackEvents.tryInventoryInteraction(world, pos, facing, backpack, player)) {
            return EnumActionResult.SUCCESS;
        }
        Block backpackBlock = BackpackBlock.forTier(tier);
        if (backpackBlock == null) {
            return EnumActionResult.FAIL;
        }

        IBlockState clickedState = world.getBlockState(pos);
        BlockPos placePos = clickedState.getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
        if (!player.canPlayerEdit(placePos, facing, backpack) || !world.mayPlace(backpackBlock, placePos, false, facing, player)) {
            return EnumActionResult.FAIL;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        IBlockState placementState = backpackBlock.getDefaultState().withProperty(BackpackBlock.FACING, player.getHorizontalFacing().getOpposite());
        if (!world.setBlockState(placePos, placementState, 3)) {
            return EnumActionResult.FAIL;
        }
        BackpackTileEntity te = BackpackTileEntity.at(world, placePos);
        if (te != null) {
            te.setBackpack(getBackpackToPlace(player, backpack));
        }
        SoundType soundtype = placementState.getBlock().getSoundType(placementState, world, placePos, player);
        world.playSound(null, placePos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        if (!player.capabilities.isCreativeMode) {
            backpack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    private static ItemStack getBackpackToPlace(EntityPlayer player, ItemStack backpack) {
        if (!player.capabilities.isCreativeMode) {
            // Same UUID: the placed block and the (now consumed) stack are the same backpack.
            return backpack.copy();
        }
        // Creative keeps the held stack, so the placed one must be an independent backpack.
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(backpack);
        return wrapper == null ? backpack.copy() : wrapper.cloneBackpack();
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {
            @Nullable
            private BackpackWrapper wrapper = null;

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                if (capability == CapabilityBackpackWrapper.BACKPACK_WRAPPER || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return true;
                }
                // Only backpacks that have contents: a capability check must never create storage.
                if (capability == CapabilityEnergy.ENERGY) {
                    return getWrapper().getContentsUuid() != null && getWrapper().getEnergyStorage() != null;
                }
                if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                    return ModConfig.upgrades.itemFluidHandlerEnabled && getWrapper().getContentsUuid() != null && getWrapper().getFluidHandler() != null;
                }
                return false;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == CapabilityBackpackWrapper.BACKPACK_WRAPPER) {
                    return CapabilityBackpackWrapper.BACKPACK_WRAPPER.cast(getWrapper());
                }
                if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getWrapper().getInventoryForInputOutput());
                }
                if (capability == CapabilityEnergy.ENERGY && getWrapper().getContentsUuid() != null) {
                    IEnergyStorage energyStorage = getWrapper().getEnergyStorage();
                    return energyStorage == null ? null : CapabilityEnergy.ENERGY.cast(energyStorage);
                }
                if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY && ModConfig.upgrades.itemFluidHandlerEnabled && getWrapper().getContentsUuid() != null) {
                    IFluidHandlerItem fluidHandler = getWrapper().getFluidHandler();
                    return fluidHandler == null ? null : CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(fluidHandler);
                }
                return null;
            }

            private BackpackWrapper getWrapper() {
                if (wrapper == null) {
                    wrapper = new BackpackWrapper(stack);
                }
                return wrapper;
            }
        };
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        // Only backpacks that have been opened have contents (and so possibly upgrades) to tick.
        if (wrapper == null || wrapper.getContentsUuid() == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        for (ITickableUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class)) {
            upgrade.tick(player, world, player.getPosition());
        }
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        return wrapper != null && wrapper.getContentsUuid() != null && wrapper.getUpgradeHandler().hasUpgrade(EverlastingUpgradeItem.TYPE);
    }

    /** With the Everlasting upgrade a dropped backpack becomes an indestructible item entity. */
    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        if (!(location instanceof EntityItem)) {
            return null;
        }
        EntityItem original = (EntityItem) location;
        EverlastingBackpackItemEntity entity = new EverlastingBackpackItemEntity(world, original.posX, original.posY, original.posZ, itemstack);
        entity.motionX = original.motionX;
        entity.motionY = original.motionY;
        entity.motionZ = original.motionZ;
        entity.setThrower(original.getThrower());
        entity.setDefaultPickupDelay();
        return entity;
    }

    /** Lets the backpack sit in the chest armour slot (worn on the back). */
    @Nullable
    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    /** Q while the backpack's own GUI is open would otherwise drop the open backpack. */
    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return !(player.openContainer instanceof BackpackContainer) || ((BackpackContainer) player.openContainer).getOpenedStack() != item;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    /**
     * Leather is the backpack's "repair item". Backpacks never take damage, so this does nothing
     * at an anvil; it is what Quality Tools' reforging station checks (with its default "use
     * repair item" setting) to let leather reforge a backpack's quality.
     */
    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (repair.isEmpty()) {
            return false;
        }
        int leatherId = OreDictionary.getOreID("leather");
        for (int id : OreDictionary.getOreIDs(repair)) {
            if (id == leatherId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState blockIn) {
        return false;
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
        return armorType == EntityEquipmentSlot.CHEST && entity instanceof EntityLivingBase;
    }
}
