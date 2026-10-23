/*
 * Written for Sophisticated Backpacks Legacy by ExiledRadio, GPLv3.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.backpack.NearbyBackpackActions;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** One of the backpack screen's loot buttons, run on the server against the backpack the player has open. */
public class BackpackActionMessage implements IMessage {
    public static final int DEPOSIT_TO_NEARBY_CHESTS = 0;
    public static final int MERGE_BACKPACKS = 1;

    private int action;

    public BackpackActionMessage() {}

    public BackpackActionMessage(int action) {
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
    }

    public static class Handler implements IMessageHandler<BackpackActionMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(BackpackActionMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!(player.openContainer instanceof BackpackContainer) || !player.openContainer.canInteractWith(player)) {
                    return;
                }
                BackpackContainer container = (BackpackContainer) player.openContainer;
                if (message.action == DEPOSIT_TO_NEARBY_CHESTS) {
                    NearbyBackpackActions.depositToNearbyChests(player, container);
                } else if (message.action == MERGE_BACKPACKS) {
                    NearbyBackpackActions.mergeBackpacks(player, container);
                }
                container.detectAndSendChanges();
            });
            return null;
        }
    }
}
