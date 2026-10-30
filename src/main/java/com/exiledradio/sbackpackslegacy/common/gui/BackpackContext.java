/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.common.gui;

import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.compat.baubles.BaublesCompat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Where an open backpack lives. Encoded into the three ints of FML's openGui so both sides build
 * the same container. The context also answers "is the backpack still exactly where it was
 * opened from?", which is what keeps the open GUI from becoming a dupe tool.
 */
public abstract class BackpackContext {
    public static final int GUI_ITEM = 0;
    public static final int GUI_BLOCK = 1;
    public static final int GUI_ANOTHER_PLAYER = 2;

    /** The backpack stack instance this context currently points at, or EMPTY. */
    public abstract ItemStack getBackpackStack(EntityPlayer player);

    /** Index in player.inventory.mainInventory that must not be touched while open, or -1. */
    public int getLockedMainInventorySlot() {
        return -1;
    }

    public abstract boolean isWithinReach(EntityPlayer player);

    public abstract int getGuiId();

    public abstract int getX();

    public abstract int getY();

    public abstract int getZ();

    public static BackpackContext fromGuiIds(int id, int x, int y, int z) {
        if (id == GUI_BLOCK) {
            return new Block(new BlockPos(x, y, z));
        }
        if (id == GUI_ANOTHER_PLAYER) {
            return new AnotherPlayer(x);
        }
        return new Item(InventoryKind.byId(x), y);
    }

    public enum InventoryKind {
        // Sent by ordinal: add new kinds at the end.
        MAIN, OFFHAND, ARMOR, BAUBLE, WEARABLE;

        static InventoryKind byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : MAIN;
        }
    }

    public static class Item extends BackpackContext {
        private static final int CHEST_ARMOR_INDEX = 2;
        private final InventoryKind kind;
        private final int slot;

        public Item(InventoryKind kind, int slot) {
            this.kind = kind;
            this.slot = slot;
        }

        @Override
        public ItemStack getBackpackStack(EntityPlayer player) {
            ItemStack stack;
            switch (kind) {
                case OFFHAND:
                    stack = player.inventory.offHandInventory.get(0);
                    break;
                case ARMOR:
                    stack = player.inventory.armorInventory.get(CHEST_ARMOR_INDEX);
                    break;
                case BAUBLE:
                    stack = BaublesCompat.getStack(player, slot);
                    break;
                case WEARABLE:
                    stack = com.exiledradio.sbackpackslegacy.compat.wearables.WearablesCompat.getBackStack(player);
                    break;
                case MAIN:
                default:
                    stack = slot >= 0 && slot < player.inventory.mainInventory.size() ? player.inventory.mainInventory.get(slot) : ItemStack.EMPTY;
            }
            return stack.getItem() instanceof BackpackItem ? stack : ItemStack.EMPTY;
        }

        @Override
        public int getLockedMainInventorySlot() {
            return kind == InventoryKind.MAIN ? slot : -1;
        }

        @Override
        public boolean isWithinReach(EntityPlayer player) {
            return true;
        }

        @Override
        public int getGuiId() {
            return GUI_ITEM;
        }

        @Override
        public int getX() {
            return kind.ordinal();
        }

        @Override
        public int getY() {
            return slot;
        }

        @Override
        public int getZ() {
            return 0;
        }
    }

    /**
     * The backpack another player has on their back, opened by right-clicking it from behind.
     * The client can't rely on its copy of someone else's equipment being up to date, so the
     * server sends the stack just before the GUI opens (see OtherPlayerBackpackMessage).
     */
    public static class AnotherPlayer extends BackpackContext {
        private static final double MAX_DISTANCE_SQ = 64.0D;
        private static int clientEntityId = -1;
        private static ItemStack clientStack = ItemStack.EMPTY;
        private final int entityId;

        public AnotherPlayer(int entityId) {
            this.entityId = entityId;
        }

        /** Client side: the wearer's backpack as the server sent it. */
        public static void setClientStack(int entityId, ItemStack stack) {
            clientEntityId = entityId;
            clientStack = stack;
        }

        @javax.annotation.Nullable
        public EntityPlayer getWearer(EntityPlayer player) {
            net.minecraft.entity.Entity entity = player.world.getEntityByID(entityId);
            return entity instanceof EntityPlayer && entity != player && entity.isEntityAlive() ? (EntityPlayer) entity : null;
        }

        @Override
        public ItemStack getBackpackStack(EntityPlayer player) {
            if (player.world.isRemote) {
                return entityId == clientEntityId ? clientStack : ItemStack.EMPTY;
            }
            EntityPlayer wearer = getWearer(player);
            com.exiledradio.sbackpackslegacy.backpack.WornBackpack worn = wearer == null ? null : com.exiledradio.sbackpackslegacy.backpack.WornBackpack.find(wearer);
            return worn == null ? ItemStack.EMPTY : worn.getBackpack();
        }

        @Override
        public boolean isWithinReach(EntityPlayer player) {
            EntityPlayer wearer = getWearer(player);
            return wearer != null && com.exiledradio.sbackpackslegacy.ModConfig.allowOpeningOtherPlayerBackpacks
                    && player.getDistanceSq(wearer) <= MAX_DISTANCE_SQ;
        }

        @Override
        public int getGuiId() {
            return GUI_ANOTHER_PLAYER;
        }

        @Override
        public int getX() {
            return entityId;
        }

        @Override
        public int getY() {
            return 0;
        }

        @Override
        public int getZ() {
            return 0;
        }
    }

    public static class Block extends BackpackContext {
        private final BlockPos pos;

        public Block(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public ItemStack getBackpackStack(EntityPlayer player) {
            TileEntity te = player.world.getTileEntity(pos);
            return te instanceof BackpackTileEntity ? ((BackpackTileEntity) te).getBackpack() : ItemStack.EMPTY;
        }

        @Override
        public boolean isWithinReach(EntityPlayer player) {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }

        @Override
        public int getGuiId() {
            return GUI_BLOCK;
        }

        @Override
        public int getX() {
            return pos.getX();
        }

        @Override
        public int getY() {
            return pos.getY();
        }

        @Override
        public int getZ() {
            return pos.getZ();
        }
    }
}
