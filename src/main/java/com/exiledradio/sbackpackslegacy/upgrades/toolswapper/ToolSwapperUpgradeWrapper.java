/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IAttackEntityResponseUpgrade;
import com.exiledradio.scorelegacy.api.IBlockClickResponseUpgrade;
import com.exiledradio.scorelegacy.api.IBlockToolSwapUpgrade;
import com.exiledradio.scorelegacy.api.IEntityToolSwapUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Left-clicking a block with a tool swaps in a better tool from the backpack; attacking with a
 * tool swaps in the best weapon. The advanced version adds filters and the tool swap key, which
 * picks a tool that does something to the block or entity being looked at (shears, hoes,
 * shovels for grass paths, wrenches), cycling through matching tools on repeated presses.
 */
public class ToolSwapperUpgradeWrapper extends UpgradeWrapperBase<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeItem>
        implements IBlockClickResponseUpgrade, IAttackEntityResponseUpgrade, IBlockToolSwapUpgrade, IEntityToolSwapUpgrade {
    private final ToolSwapperFilterLogic filterLogic;
    @Nullable
    private ResourceLocation toolCacheFor = null;
    private final Queue<ItemStack> toolCache = new LinkedList<>();

    public ToolSwapperUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new ToolSwapperFilterLogic(upgrade, upgradeSaveHandler);
    }

    // --- left click on a block ---------------------------------------------------------------

    @Override
    public boolean onBlockClick(EntityPlayer player, BlockPos pos) {
        ToolSwapMode toolSwapMode = getToolSwapMode();
        if (player.isCreative() || player.isSpectator() || toolSwapMode == ToolSwapMode.NO_SWAP) {
            return false;
        }
        ItemStack mainHandItem = player.getHeldItemMainhand();
        if (mainHandItem.getItem() instanceof BackpackItem || (toolSwapMode == ToolSwapMode.ONLY_TOOLS && isSword(mainHandItem))
                || (!isSword(mainHandItem) && !isTool(mainHandItem))) {
            return false;
        }
        IBlockState state = player.world.getBlockState(pos).getActualState(player.world, pos);
        if (getToolTypeEffectiveOnBlock(state, mainHandItem) != null || goodAtBreakingBlock(state, mainHandItem)) {
            return true;
        }
        return tryToSwapTool(player, state, mainHandItem);
    }

    private boolean tryToSwapTool(EntityPlayer player, IBlockState state, ItemStack mainHandItem) {
        IItemHandlerSimpleInserter backpackInventory = storageWrapper.getInventoryHandler();
        int selectedSlot = -1;
        for (int slot = 0; slot < backpackInventory.getSlots(); slot++) {
            ItemStack stack = backpackInventory.getStackInSlot(slot);
            if (!stack.isEmpty() && isAllowedAndGoodAtBreakingBlock(state, stack)) {
                selectedSlot = slot;
                break;
            }
        }
        if (selectedSlot < 0) {
            return false;
        }
        ItemStack tool = backpackInventory.getStackInSlot(selectedSlot);
        if (!hasSpaceInBackpackOrCanPlaceInTheSlotOfSwappedTool(backpackInventory, mainHandItem, tool, selectedSlot)) {
            return false;
        }
        player.setHeldItem(EnumHand.MAIN_HAND, backpackInventory.extractItem(selectedSlot, 1, false));
        backpackInventory.insertItem(mainHandItem, false);
        return true;
    }

    private boolean isAllowedAndGoodAtBreakingBlock(IBlockState state, ItemStack stack) {
        String toolType = getToolTypeEffectiveOnBlock(state, stack);
        return (toolType != null && filterLogic.matchesToolFilter(stack, toolType)) || goodAtBreakingBlock(state, stack);
    }

    private boolean hasSpaceInBackpackOrCanPlaceInTheSlotOfSwappedTool(IItemHandlerSimpleInserter backpackInventory, ItemStack mainHandItem, ItemStack tool, int selectedSlot) {
        return backpackInventory.insertItem(mainHandItem, true).isEmpty()
                || (tool.getCount() == 1 && backpackInventory.isItemValid(selectedSlot, mainHandItem));
    }

    private static boolean goodAtBreakingBlock(IBlockState state, ItemStack stack) {
        return stack.canHarvestBlock(state) && stack.getDestroySpeed(state) > 1.5;
    }

    @Nullable
    private static String getToolTypeEffectiveOnBlock(IBlockState state, ItemStack stack) {
        Block block = state.getBlock();
        for (String type : ToolTypes.of(stack)) {
            if (block.isToolEffective(type, state)) {
                return type;
            }
        }
        return null;
    }

    // --- attacking ---------------------------------------------------------------------------

    @Override
    public boolean onAttackEntity(EntityPlayer player) {
        if (!shouldSwapWeapon()) {
            return false;
        }
        ItemStack mainHandItem = player.getHeldItemMainhand();
        if (isSword(mainHandItem)) {
            return true;
        }
        if (mainHandItem.getItem() instanceof BackpackItem || !isTool(mainHandItem)) {
            return false;
        }
        IItemHandlerSimpleInserter backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
        if (filterLogic.isAllowList()) {
            for (int slot = 0; slot < backpackInventory.getSlots(); slot++) {
                ItemStack stack = backpackInventory.getStackInSlot(slot);
                if (filterLogic.matchesWeaponFilter(stack) && swapWeapon(player, mainHandItem, backpackInventory, stack)) {
                    return true;
                }
            }
            return false;
        }
        return tryToSwapInWeapon(player, mainHandItem, backpackInventory);
    }

    private static boolean isTool(ItemStack stack) {
        return !ToolTypes.of(stack).isEmpty() || stack.getItem() instanceof ItemShears;
    }

    private static boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ItemSword) {
            return true;
        }
        return hasSwordOrNoToolTypes(stack) && !stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).isEmpty();
    }

    private static boolean hasSwordOrNoToolTypes(ItemStack stack) {
        Set<String> toolTypes = ToolTypes.of(stack);
        return toolTypes.isEmpty() || toolTypes.contains(ToolTypes.SWORD);
    }

    /** The best sword if there is one, otherwise the best axe, by attack damage. */
    private boolean tryToSwapInWeapon(EntityPlayer player, ItemStack mainHandItem, IItemHandlerSimpleInserter backpackInventory) {
        ItemStack bestAxe = ItemStack.EMPTY;
        double bestAxeDamage = 0;
        ItemStack bestSword = ItemStack.EMPTY;
        double bestSwordDamage = 0;
        for (int slot = -1; slot < backpackInventory.getSlots(); slot++) {
            ItemStack stack = slot < 0 ? mainHandItem : backpackInventory.getStackInSlot(slot);
            if (slot >= 0 && !filterLogic.matchesWeaponFilter(stack)) {
                continue;
            }
            double damage = getAttackDamage(stack);
            if (damage <= 0) {
                continue;
            }
            if (ToolTypes.of(stack).contains(ToolTypes.AXE)) {
                if (damage > bestAxeDamage) {
                    bestAxe = stack;
                    bestAxeDamage = damage;
                }
            } else if ((stack.getItem() instanceof ItemSword || hasSwordOrNoToolTypes(stack)) && damage > bestSwordDamage) {
                bestSword = stack;
                bestSwordDamage = damage;
            }
        }
        if (!bestSword.isEmpty()) {
            return swapWeapon(player, mainHandItem, backpackInventory, bestSword);
        } else if (!bestAxe.isEmpty()) {
            return swapWeapon(player, mainHandItem, backpackInventory, bestAxe);
        }
        return false;
    }

    /** Attack damage the stack gives when held, or 0 if it gives none. */
    private static double getAttackDamage(ItemStack stack) {
        Collection<AttributeModifier> modifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
        if (modifiers.isEmpty()) {
            return 0;
        }
        AbstractAttributeMap attributes = new AttributeMap();
        IAttributeInstance attackDamage = attributes.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        for (AttributeModifier modifier : modifiers) {
            attackDamage.removeModifier(modifier);
            attackDamage.applyModifier(modifier);
        }
        return attackDamage.getAttributeValue();
    }

    private boolean swapWeapon(EntityPlayer player, ItemStack mainHandItem, IItemHandlerSimpleInserter backpackInventory, ItemStack weapon) {
        if (weapon == mainHandItem) {
            return true;
        }
        // One weapon: a stack-upgraded slot can hold several, and the hand takes one.
        ItemStack oneWeapon = weapon.copy();
        oneWeapon.setCount(1);
        ItemStack extracted = InventoryHelper.extractFromInventory(oneWeapon, backpackInventory, false);
        if (extracted.isEmpty()) {
            return false;
        }
        if (backpackInventory.insertItem(mainHandItem, true).isEmpty()) {
            player.setHeldItem(EnumHand.MAIN_HAND, extracted);
            backpackInventory.insertItem(mainHandItem, false);
            return true;
        }
        backpackInventory.insertItem(extracted, false);
        return false;
    }

    // --- tool swap key -----------------------------------------------------------------------

    @Override
    public boolean canProcessBlockInteract() {
        return upgradeItem.shouldSwapToolOnKeyPress();
    }

    @Override
    public boolean canProcessEntityInteract() {
        return upgradeItem.shouldSwapToolOnKeyPress();
    }

    @Override
    public boolean onBlockInteract(World world, BlockPos pos, IBlockState blockState, EntityPlayer player) {
        if (!upgradeItem.shouldSwapToolOnKeyPress()) {
            return false;
        }
        return tryToSwapTool(player, stack -> itemWorksOnBlock(world, pos, blockState, stack), blockState.getBlock().getRegistryName());
    }

    @Override
    public boolean onEntityInteract(World world, Entity entity, EntityPlayer player) {
        if (!upgradeItem.shouldSwapToolOnKeyPress()) {
            return false;
        }
        return tryToSwapTool(player, stack -> itemWorksOnEntity(stack, entity), net.minecraft.entity.EntityList.getKey(entity));
    }

    private boolean tryToSwapTool(EntityPlayer player, Predicate<ItemStack> isToolValid, @Nullable ResourceLocation targetRegistryName) {
        ItemStack mainHandStack = player.getHeldItemMainhand();
        if (mainHandStack.getItem() instanceof BackpackItem) {
            return false;
        }
        if (toolCacheFor == null || !toolCacheFor.equals(targetRegistryName)) {
            toolCache.clear();
            toolCacheFor = targetRegistryName;
        }
        IItemHandlerSimpleInserter backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
        if (isToolValid.test(mainHandStack) && !hasEquivalentItem(toolCache, mainHandStack)) {
            toolCache.offer(mainHandStack);
        }
        ItemStack found = findToolToSwap(backpackInventory, isToolValid);
        if (found.isEmpty()) {
            return false;
        }
        ItemStack tool = found.copy();
        tool.setCount(1);
        if (found.getCount() == 1 || backpackInventory.insertItem(mainHandStack, true).isEmpty()) {
            player.setHeldItem(EnumHand.MAIN_HAND, InventoryHelper.extractFromInventory(tool, backpackInventory, false));
            backpackInventory.insertItem(mainHandStack, false);
            toolCache.offer(tool);
        }
        return true;
    }

    /** A matching tool not handed out yet for this target; once all were, start over from the oldest. */
    private ItemStack findToolToSwap(IItemHandlerSimpleInserter backpackInventory, Predicate<ItemStack> isValidTool) {
        Set<ItemStack> alreadyGivenBefore = new HashSet<>();
        for (int slot = 0; slot < backpackInventory.getSlots(); slot++) {
            ItemStack stack = backpackInventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!hasEquivalentItem(toolCache, stack)) {
                if (isValidTool.test(stack)) {
                    return stack;
                }
            } else {
                alreadyGivenBefore.add(stack);
            }
        }
        while (!alreadyGivenBefore.isEmpty() && toolCache.peek() != null) {
            ItemStack cached = toolCache.poll();
            if (hasEquivalentItem(alreadyGivenBefore, cached)) {
                return cached;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasEquivalentItem(Collection<ItemStack> stacks, ItemStack stack) {
        for (ItemStack other : stacks) {
            if (ItemStack.areItemsEqualIgnoreDurability(other, stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean itemWorksOnBlock(World world, BlockPos pos, IBlockState blockState, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Block block = blockState.getBlock();
        if (stack.getItem() instanceof ItemShears && block instanceof IShearable && ((IShearable) block).isShearable(stack, world, pos)) {
            return true;
        }
        boolean airAbove = world.isAirBlock(pos.up());
        boolean grassOrDirt = block == Blocks.GRASS || block == Blocks.DIRT;
        if (airAbove && grassOrDirt && (stack.getItem() instanceof ItemHoe || stack.getItem().getToolClasses(stack).contains("hoe"))) {
            return true;
        }
        if (airAbove && block == Blocks.GRASS && ToolTypes.of(stack).contains(ToolTypes.SHOVEL)) {
            return true;
        }
        return isWrench(stack) && (block.hasTileEntity(blockState) || block.getValidRotations(world, pos) != null);
    }

    private static boolean itemWorksOnEntity(ItemStack stack, Entity entity) {
        return stack.getItem() instanceof ItemShears && entity instanceof IShearable
                && ((IShearable) entity).isShearable(stack, entity.world, entity.getPosition());
    }

    /** 1.12 has no common wrench API; mods either declare a "wrench" tool class or name the item one. */
    private static boolean isWrench(ItemStack stack) {
        if (ToolTypes.of(stack).contains("wrench")) {
            return true;
        }
        ResourceLocation name = stack.getItem().getRegistryName();
        return name != null && name.getPath().contains("wrench");
    }

    // --- settings ----------------------------------------------------------------------------

    @Override
    public boolean hideSettingsTab() {
        return !upgradeItem.hasSettingsTab();
    }

    public ToolSwapperFilterLogic getFilterLogic() {
        return filterLogic;
    }

    public boolean shouldSwapWeapon() {
        return NBTHelper.getBoolean(upgrade, "shouldSwapWeapon", true);
    }

    public void setSwapWeapon(boolean shouldSwapWeapon) {
        NBTHelper.setBoolean(upgrade, "shouldSwapWeapon", shouldSwapWeapon);
        save();
    }

    public ToolSwapMode getToolSwapMode() {
        return NBTHelper.getEnum(upgrade, "toolSwapMode", ToolSwapMode::fromName, ToolSwapMode.ANY);
    }

    public void setToolSwapMode(ToolSwapMode toolSwapMode) {
        NBTHelper.setEnum(upgrade, "toolSwapMode", toolSwapMode);
        save();
    }
}
