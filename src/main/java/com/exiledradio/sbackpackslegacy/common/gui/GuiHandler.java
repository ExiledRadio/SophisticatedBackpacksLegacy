package com.exiledradio.sbackpackslegacy.common.gui;

import com.exiledradio.sbackpackslegacy.backpack.AccessLogRecord;
import com.exiledradio.sbackpackslegacy.backpack.BackpackStorage;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.sbackpackslegacy.network.BackpackContentsMessage;
import com.exiledradio.sbackpackslegacy.network.PacketHandler;
import java.util.UUID;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    /** Settings screens use the backpack GUI ids plus this. */
    private static final int SETTINGS_ID_OFFSET = 3;

    /** Server side: opens a backpack GUI for the given context, if a backpack is there. */
    public static void openBackpack(EntityPlayer player, BackpackContext context) {
        open(player, context, 0);
    }

    /** Server side: opens the settings screen of the backpack in the given context. */
    public static void openBackpackSettings(EntityPlayer player, BackpackContext context) {
        open(player, context, SETTINGS_ID_OFFSET);
    }

    private static void open(EntityPlayer player, BackpackContext context, int idOffset) {
        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }
        // Someone else's backpack: its contents, not its settings.
        if (context instanceof BackpackContext.AnotherPlayer && idOffset != 0) {
            return;
        }
        ItemStack stack = context.getBackpackStack(player);
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
        if (wrapper == null) {
            return;
        }
        // The client builds its container from its own copy of the stack, so the UUID and slot
        // counts have to be in that copy before the open-GUI packet arrives. Both packets go out
        // in order on the same connection.
        wrapper.getOrCreateContentsUuid();
        wrapper.getNumberOfInventorySlots();
        wrapper.getNumberOfUpgradeSlots();
        syncBackpackStackToClient((EntityPlayerMP) player, context);
        // Upgrades live in the contents, not the stack; the client needs them to build the same slots.
        UUID uuid = wrapper.getContentsUuid();
        if (uuid != null) {
            UpgradeHandler upgradeHandler = wrapper.getUpgradeHandler();
            PacketHandler.CHANNEL.sendTo(new BackpackContentsMessage(uuid, upgradeHandler.serializeNBT()), (EntityPlayerMP) player);
        }

        if (uuid != null && idOffset == 0) {
            //noinspection ConstantConditions - registered items have registry names
            BackpackStorage.get().putAccessLog(new AccessLogRecord(stack.getItem().getRegistryName(), uuid, player.getName(), stack.getDisplayName(),
                    wrapper.getClothColor(), wrapper.getBorderColor(), System.currentTimeMillis()));
        }
        player.openGui(SophisticatedBackpacksLegacy.instance, context.getGuiId() + idOffset, player.world, context.getX(), context.getY(), context.getZ());
    }

    private static void syncBackpackStackToClient(EntityPlayerMP player, BackpackContext context) {
        if (context instanceof BackpackContext.AnotherPlayer) {
            PacketHandler.CHANNEL.sendTo(new com.exiledradio.sbackpackslegacy.network.OtherPlayerBackpackMessage(context.getX(),
                    context.getBackpackStack(player)), player);
        } else if (context instanceof BackpackContext.Block) {
            BackpackTileEntity te = BackpackTileEntity.at(player.world, new BlockPos(context.getX(), context.getY(), context.getZ()));
            SPacketUpdateTileEntity packet = te == null ? null : te.getUpdatePacket();
            if (packet != null) {
                player.connection.sendPacket(packet);
            }
        } else {
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id >= SETTINGS_ID_OFFSET) {
            return BackpackSettingsContainer.create(player, BackpackContext.fromGuiIds(id - SETTINGS_ID_OFFSET, x, y, z));
        }
        return BackpackContainer.create(player, BackpackContext.fromGuiIds(id, x, y, z));
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id >= SETTINGS_ID_OFFSET) {
            BackpackSettingsContainer settings = BackpackSettingsContainer.create(player, BackpackContext.fromGuiIds(id - SETTINGS_ID_OFFSET, x, y, z));
            return settings == null ? null : SophisticatedBackpacksLegacy.proxy.createBackpackSettingsGui(settings);
        }
        BackpackContainer container = BackpackContainer.create(player, BackpackContext.fromGuiIds(id, x, y, z));
        // Built by the client proxy so this common class never names a client-only type.
        return container == null ? null : SophisticatedBackpacksLegacy.proxy.createBackpackGui(container);
    }
}
