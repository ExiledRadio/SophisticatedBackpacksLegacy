/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.WornBackpack;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.sbackpackslegacy.common.gui.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** Sent by the open-backpack keybind; the server picks which backpack to open. */
public class OpenBackpackMessage implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
        //no payload
    }

    @Override
    public void toBytes(ByteBuf buf) {
        //no payload
    }

    public static class Handler implements IMessageHandler<OpenBackpackMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(OpenBackpackMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                // Only from the plain game screen: never swap one open container for another.
                if (player.openContainer != player.inventoryContainer) {
                    return;
                }
                BackpackContext context = findBackpack(player);
                if (context != null) {
                    GuiHandler.openBackpack(player, context);
                }
            });
            return null;
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
