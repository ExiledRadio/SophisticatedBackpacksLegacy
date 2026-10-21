/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.scorelegacy.api.IItemHandlerInteractionUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.exiledradio.scorelegacy.api.IAttackEntityResponseUpgrade;
import com.exiledradio.scorelegacy.api.IBlockClickResponseUpgrade;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

@Mod.EventBusSubscriber(modid = SophisticatedBackpacksLegacy.MODID)
public final class BackpackEvents {
    private BackpackEvents() {}

    /** How far from straight behind the wearer (in the horizontal plane) still counts as their back: cos(75°). */
    private static final double BEHIND_COS = 0.26D;

    /**
     * Right-clicking another player's back opens the backpack they wear, as in the original's
     * later versions (written fresh here: the only GPL version, 1.16, doesn't have it).
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!com.exiledradio.sbackpackslegacy.ModConfig.allowOpeningOtherPlayerBackpacks || event.getHand() != net.minecraft.util.EnumHand.MAIN_HAND
                || !(event.getTarget() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer opener = event.getEntityPlayer();
        EntityPlayer wearer = (EntityPlayer) event.getTarget();
        if (opener.isSpectator() || WornBackpack.find(wearer) == null || !isBehind(opener, wearer)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);
        if (!opener.world.isRemote) {
            com.exiledradio.sbackpackslegacy.common.gui.GuiHandler.openBackpack(opener,
                    new com.exiledradio.sbackpackslegacy.common.gui.BackpackContext.AnotherPlayer(wearer.getEntityId()));
        }
    }

    private static boolean isBehind(EntityPlayer opener, EntityPlayer wearer) {
        double dx = opener.posX - wearer.posX;
        double dz = opener.posZ - wearer.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-4D) {
            return false;
        }
        // The body's facing (yaw 0 faces +Z); the backpack sits opposite it.
        float yaw = wearer.renderYawOffset * 0.017453292F;
        double facingX = -net.minecraft.util.math.MathHelper.sin(yaw);
        double facingZ = net.minecraft.util.math.MathHelper.cos(yaw);
        return (dx * facingX + dz * facingZ) / length <= -BEHIND_COS;
    }

    /**
     * Pickup upgrades take an item lying on the ground only if the backpacks can take all of it;
     * otherwise vanilla pickup runs as normal.
     */
    /** Server side only: the swapped hand item reaches the client with the normal slot sync. */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }
        BlockPos pos = event.getPos();
        PlayerBackpacks.forEach(player, wrapper -> {
            for (IBlockClickResponseUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IBlockClickResponseUpgrade.class)) {
                if (upgrade.onBlockClick(player, pos)) {
                    return true;
                }
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }
        PlayerBackpacks.forEach(player, wrapper -> {
            for (IAttackEntityResponseUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IAttackEntityResponseUpgrade.class)) {
                if (upgrade.onAttackEntity(player)) {
                    return true;
                }
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        EntityItem itemEntity = event.getItem();
        EntityPlayer player = event.getEntityPlayer();
        if (itemEntity.getItem().isEmpty() || player.world.isRemote) {
            return;
        }
        World world = player.world;
        ItemStack[] simulated = {itemEntity.getItem().copy()};
        PlayerBackpacks.forEach(player, wrapper -> {
            simulated[0] = PlayerBackpacks.runPickupOnBackpack(world, null, simulated[0], wrapper, true);
            return simulated[0].isEmpty();
        });
        if (!simulated[0].isEmpty()) {
            return;
        }
        ItemStack[] remaining = {itemEntity.getItem().copy()};
        PlayerBackpacks.forEach(player, wrapper -> {
            remaining[0] = PlayerBackpacks.runPickupOnBackpack(world, player, remaining[0], wrapper, false);
            return remaining[0].isEmpty();
        });
        itemEntity.setItem(ItemStack.EMPTY);
        itemEntity.setDead();
        event.setCanceled(true);
    }

    /**
     * Sneak-right-click on an inventory block with a backpack in hand runs restock and deposit.
     * Returns true if the backpack had such upgrades (the click is then used up).
     */
    public static boolean tryInventoryInteraction(World world, BlockPos pos, EnumFacing face, ItemStack backpack, EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)) {
            return false;
        }
        IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
        BackpackWrapper wrapper = com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper.get(backpack);
        if (itemHandler == null || wrapper == null) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        if (wrapper.getContentsUuid() == null) {
            return false;
        }
        List<IItemHandlerInteractionUpgrade> upgrades = wrapper.getUpgradeHandler().getWrappersThatImplement(IItemHandlerInteractionUpgrade.class);
        if (upgrades.isEmpty()) {
            return false;
        }
        upgrades.forEach(upgrade -> upgrade.onHandlerInteract(itemHandler, player));
        return true;
    }
}
