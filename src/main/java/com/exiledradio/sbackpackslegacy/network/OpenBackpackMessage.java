/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.WornBackpack;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.sbackpackslegacy.common.gui.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/**
 * Sent by the open-backpack keybind. Without a target the server picks which backpack to open;
 * with one (the key pressed while hovering a backpack in an inventory screen) it opens that one.
 */
public class OpenBackpackMessage implements IMessage {
    private static final int NO_TARGET = -1;
    private int kind = NO_TARGET;
    private int slot;

    public OpenBackpackMessage() {
    }

    public OpenBackpackMessage(BackpackContext.InventoryKind kind, int slot) {
        this.kind = kind.ordinal();
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // 1.1.2 and older clients send no payload.
        if (buf.readableBytes() >= 8) {
            kind = buf.readInt();
            slot = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(kind);
        buf.writeInt(slot);
    }

    public static class Handler implements IMessageHandler<OpenBackpackMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(OpenBackpackMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.kind == NO_TARGET) {
                    openFirst(player);
                } else {
                    openTarget(player, message);
                }
            });
            return null;
        }

        private static void openFirst(EntityPlayerMP player) {
            // Only from the plain game screen: never swap one open container for another.
            if (player.openContainer != player.inventoryContainer) {
                return;
            }
            BackpackContext context = findBackpack(player);
            if (context != null) {
                GuiHandler.openBackpack(player, context);
            }
        }

        /**
         * The hovered backpack, from the inventory screen or from the player-inventory part of an
         * open backpack (which switches to it). Nothing else: other containers would be closed,
         * and a carried stack would be dropped when the current container closes.
         */
        private static void openTarget(EntityPlayerMP player, OpenBackpackMessage message) {
            if (message.kind < 0 || message.kind >= BackpackContext.InventoryKind.values().length
                    || !player.inventory.getItemStack().isEmpty()) {
                return;
            }
            boolean fromBackpack = player.openContainer instanceof BackpackContainer;
            if (player.openContainer != player.inventoryContainer && !fromBackpack) {
                return;
            }
            BackpackContext context = new BackpackContext.Item(BackpackContext.InventoryKind.values()[message.kind], message.slot);
            // The context checks the slot range and that a backpack is there.
            ItemStack stack = context.getBackpackStack(player);
            if (stack.isEmpty() || stack.getCount() != 1) {
                return;
            }
            if (fromBackpack && ((BackpackContainer) player.openContainer).getOpenedStack() == stack) {
                return;
            }
            GuiHandler.openBackpack(player, context);
        }

        /** Worn backpack first (Baubles, then chest), then the hotbar and main inventory, then the offhand. */
        @Nullable
        private static BackpackContext findBackpack(EntityPlayerMP player) {
            WornBackpack worn = WornBackpack.find(player);
            if (worn != null) {
                return worn.getContext();
            }
            for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
                ItemStack stack = player.inventory.mainInventory.get(slot);
                if (stack.getItem() instanceof BackpackItem) {
                    return new BackpackContext.Item(BackpackContext.InventoryKind.MAIN, slot);
                }
            }
            BackpackContext offhand = new BackpackContext.Item(BackpackContext.InventoryKind.OFFHAND, 0);
            return offhand.getBackpackStack(player).isEmpty() ? null : offhand;
        }
    }
}
