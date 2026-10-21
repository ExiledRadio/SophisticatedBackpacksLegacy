/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackRenderInfo;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.sbackpackslegacy.common.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class BackpackBlock extends Block {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    // Render-only: filled from the tile in getActualState, never saved in the block's metadata.
    public static final PropertyBool LEFT_TANK = PropertyBool.create("left_tank");
    public static final PropertyBool RIGHT_TANK = PropertyBool.create("right_tank");
    public static final PropertyBool BATTERY = PropertyBool.create("battery");
    private static final Map<BackpackTier, BackpackBlock> BY_TIER = new EnumMap<>(BackpackTier.class);

    // The body of the original's voxel shape, facing north; rotated for the other directions.
    private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(3.25 / 16, 0, 4 / 16D, 12.75 / 16, 14.5 / 16, 11.5 / 16);
    private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(3.25 / 16, 0, 4.5 / 16, 12.75 / 16, 14.5 / 16, 12 / 16D);
    private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(4 / 16D, 0, 3.25 / 16, 11.5 / 16, 14.5 / 16, 12.75 / 16);
    private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(4.5 / 16, 0, 3.25 / 16, 12 / 16D, 14.5 / 16, 12.75 / 16);

    private final BackpackTier tier;

    public BackpackBlock(BackpackTier tier) {
        super(Material.CLOTH);
        this.tier = tier;
        setRegistryName(SophisticatedBackpacksLegacy.MODID, tier.getRegistryName());
        setTranslationKey(SophisticatedBackpacksLegacy.MODID + "." + tier.getRegistryName());
        setHardness(0.8F);
        setSoundType(SoundType.CLOTH);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(LEFT_TANK, false).withProperty(RIGHT_TANK, false).withProperty(BATTERY, false));
        BY_TIER.put(tier, this);
    }

    @Nullable
    public static BackpackBlock forTier(BackpackTier tier) {
        return BY_TIER.get(tier);
    }

    public BackpackTier getTier() {
        return tier;
    }

    // --- state -------------------------------------------------------------------------------

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LEFT_TANK, RIGHT_TANK, BATTERY);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        BackpackRenderInfo renderInfo = te instanceof BackpackTileEntity ? BackpackRenderInfo.of(((BackpackTileEntity) te).getBackpack()) : BackpackRenderInfo.DEFAULT;
        return state.withProperty(LEFT_TANK, renderInfo.hasLeftTank())
                .withProperty(RIGHT_TANK, renderInfo.hasRightTank())
                .withProperty(BATTERY, renderInfo.hasBattery());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    // --- shape and rendering -----------------------------------------------------------------

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case EAST:
                return EAST_AABB;
            case NORTH:
            default:
                return NORTH_AABB;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    // --- tile entity -------------------------------------------------------------------------

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new BackpackTileEntity();
    }

    // --- interaction -------------------------------------------------------------------------

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        if (player.isSneaking() && player.getHeldItem(hand).isEmpty()) {
            pickUp(world, pos, state, player, hand);
            return true;
        }
        GuiHandler.openBackpack(player, new BackpackContext.Block(pos));
        return true;
    }

    /** Sneak + right-click with an empty hand puts the backpack straight into that hand. */
    private static void pickUp(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand) {
        BackpackTileEntity te = BackpackTileEntity.at(world, pos);
        if (te == null) {
            return;
        }
        ItemStack backpack = te.removeBackpack();
        player.setHeldItem(hand, backpack);
        player.getCooldownTracker().setCooldown(backpack.getItem(), 5);
        world.setBlockToAir(pos);
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, player);
        world.playSound(null, pos, soundType.getBreakSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    /** Items thrown onto a placed backpack go through its pickup upgrades. */
    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (world.isRemote || !(entity instanceof EntityItem) || entity.isDead) {
            return;
        }
        BackpackTileEntity te = BackpackTileEntity.at(world, pos);
        BackpackWrapper wrapper = te == null ? null : te.getBackpackWrapper();
        if (wrapper == null || wrapper.getContentsUuid() == null) {
            return;
        }
        EntityItem itemEntity = (EntityItem) entity;
        ItemStack remaining = PlayerBackpacks.runPickupOnBackpack(world, null, itemEntity.getItem().copy(), wrapper, false);
        if (remaining.getCount() < itemEntity.getItem().getCount()) {
            if (remaining.isEmpty()) {
                itemEntity.setDead();
            } else {
                itemEntity.setItem(remaining);
            }
        }
    }

    // --- drops -------------------------------------------------------------------------------

    /**
     * The backpack is dropped here, while the tile still exists, for every way the block can go
     * (player, explosion, piston, /setblock). Creative breaking drops it too, rather than
     * silently deleting a full backpack.
     */
    @Override
    public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        BackpackTileEntity te = BackpackTileEntity.at(world, pos);
        if (te != null && !te.getBackpack().isEmpty()) {
            spawnAsEntity(world, pos, te.removeBackpack());
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        // Nothing: breakBlock already dropped the real backpack.
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        return new ItemStack(BackpackItem.forTier(tier));
    }

    // --- comparator --------------------------------------------------------------------------

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        BackpackTileEntity te = BackpackTileEntity.at(world, pos);
        BackpackWrapper wrapper = te == null ? null : te.getBackpackWrapper();
        if (wrapper == null) {
            return 0;
        }
        IItemHandler handler = wrapper.getInventoryForInputOutput();
        float totalFilled = 0;
        boolean isEmpty = true;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                totalFilled += stack.getCount() / (handler.getSlotLimit(slot) / ((float) 64 / stack.getMaxStackSize()));
                isEmpty = false;
            }
        }
        float percentFilled = totalFilled / handler.getSlots();
        return MathHelper.floor(percentFilled * 14.0F) + (isEmpty ? 0 : 1);
    }
}
