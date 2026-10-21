/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.scorelegacy.api.IBlockToolSwapUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.PlayerBackpacks;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** The tool swap key, pressed while looking at a block. */
public class BlockToolSwapMessage implements IMessage {
    private BlockPos pos;

    @SuppressWarnings("unused") // used by the network handler
    public BlockToolSwapMessage() {
    }

    public BlockToolSwapMessage(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    public static class Handler implements IMessageHandler<BlockToolSwapMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(BlockToolSwapMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                // Only blocks the player could actually reach; the client is not trusted with the position.
                if (!player.world.isBlockLoaded(message.pos) || player.getDistanceSqToCenter(message.pos) > 64) {
                    return;
                }
                boolean[] anyUpgradeCanInteract = {false};
                boolean[] result = {false};
                PlayerBackpacks.forEach(player, wrapper -> {
                    for (IBlockToolSwapUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IBlockToolSwapUpgrade.class)) {
                        if (!upgrade.canProcessBlockInteract() || result[0]) {
                            continue;
                        }
                        anyUpgradeCanInteract[0] = true;
                        result[0] = upgrade.onBlockInteract(player.world, message.pos, player.world.getBlockState(message.pos), player);
                    }
                    return result[0];
                });
                if (!anyUpgradeCanInteract[0]) {
                    player.sendStatusMessage(new TextComponentTranslation("gui.sbackpackslegacy.status.no_tool_swap_upgrade_present"), true);
                } else if (!result[0]) {
                    player.sendStatusMessage(new TextComponentTranslation("gui.sbackpackslegacy.status.no_tool_found_for_block"), true);
                }
            });
            return null;
        }
    }
}
