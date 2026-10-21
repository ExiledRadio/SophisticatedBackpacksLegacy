/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.scorelegacy.api.IEntityToolSwapUpgrade;
import com.exiledradio.sbackpackslegacy.backpack.PlayerBackpacks;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** The tool swap key, pressed while looking at an entity. */
public class EntityToolSwapMessage implements IMessage {
    private int entityId;

    @SuppressWarnings("unused") // used by the network handler
    public EntityToolSwapMessage() {
    }

    public EntityToolSwapMessage(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<EntityToolSwapMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(EntityToolSwapMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                Entity entity = player.world.getEntityByID(message.entityId);
                if (entity == null || player.getDistanceSq(entity) > 64) {
                    return;
                }
                boolean[] anyUpgradeCanInteract = {false};
                boolean[] result = {false};
                PlayerBackpacks.forEach(player, wrapper -> {
                    for (IEntityToolSwapUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IEntityToolSwapUpgrade.class)) {
                        if (!upgrade.canProcessEntityInteract() || result[0]) {
                            continue;
                        }
                        anyUpgradeCanInteract[0] = true;
                        result[0] = upgrade.onEntityInteract(player.world, entity, player);
                    }
                    return result[0];
                });
                if (!anyUpgradeCanInteract[0]) {
                    player.sendStatusMessage(new TextComponentTranslation("gui.sbackpackslegacy.status.no_tool_swap_upgrade_present"), true);
                } else if (!result[0]) {
                    player.sendStatusMessage(new TextComponentTranslation("gui.sbackpackslegacy.status.no_tool_found_for_entity"), true);
                }
            });
            return null;
        }
    }
}
