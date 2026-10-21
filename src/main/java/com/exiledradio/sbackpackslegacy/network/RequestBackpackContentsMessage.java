/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.network;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.PlayerBackpacks;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import com.exiledradio.scorelegacy.util.ItemStackKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The client wants to preview a backpack's contents in its tooltip. Only answered for backpacks
 * the player can actually see: carried, worn, or in the container they have open. The original
 * answered for any UUID, which would let a modified client read other players' backpacks.
 */
public class RequestBackpackContentsMessage implements IMessage {
    private static final int MAX_PREVIEW_STACKS = 81;
    private UUID uuid;

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public RequestBackpackContentsMessage() {
    }

    public RequestBackpackContentsMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static class Handler implements IMessageHandler<RequestBackpackContentsMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RequestBackpackContentsMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                BackpackWrapper wrapper = findVisibleBackpack(player, message.uuid);
                if (wrapper != null) {
                    PacketHandler.CHANNEL.sendTo(new BackpackTooltipContentsMessage(message.uuid, summarize(wrapper)), player);
                }
            });
            return null;
        }
    }

    @Nullable
    private static BackpackWrapper findVisibleBackpack(EntityPlayerMP player, UUID uuid) {
        BackpackWrapper[] found = {null};
        PlayerBackpacks.forEach(player, wrapper -> {
            if (uuid.equals(wrapper.getContentsUuid())) {
                found[0] = wrapper;
                return true;
            }
            return false;
        });
        if (found[0] == null && player.openContainer != null) {
            for (Slot slot : player.openContainer.inventorySlots) {
                ItemStack stack = slot.getStack();
                if (stack.getItem() instanceof BackpackItem) {
                    BackpackWrapper wrapper = CapabilityBackpackWrapper.get(stack);
                    if (wrapper != null && uuid.equals(wrapper.getContentsUuid())) {
                        return wrapper;
                    }
                }
            }
        }
        return found[0];
    }

    /** Upgrades, the contents merged by kind and sorted by count, and tank / battery levels. */
    private static NBTTagCompound summarize(BackpackWrapper wrapper) {
        NBTTagCompound summary = new NBTTagCompound();
        NBTTagList upgrades = new NBTTagList();
        for (IUpgradeWrapper upgrade : wrapper.getUpgradeHandler().getSlotWrappers().values()) {
            NBTTagCompound entry = upgrade.getUpgradeStack().writeToNBT(new NBTTagCompound());
            if (upgrade.canBeDisabled()) {
                entry.setBoolean("switchEnabled", upgrade.isEnabled());
            }
            upgrades.appendTag(entry);
        }
        summary.setTag("upgrades", upgrades);

        InventoryHandler inventory = wrapper.getInventoryHandler();
        Map<ItemStackKey, Integer> compacted = new LinkedHashMap<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                compacted.merge(new ItemStackKey(stack), stack.getCount(), Integer::sum);
            }
        }
        List<Map.Entry<ItemStackKey, Integer>> sorted = new ArrayList<>(compacted.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < sorted.size() && i < MAX_PREVIEW_STACKS; i++) {
            NBTTagCompound entry = sorted.get(i).getKey().getStack().writeToNBT(new NBTTagCompound());
            entry.setInteger("realCount", sorted.get(i).getValue());
            items.appendTag(entry);
        }
        summary.setTag("items", items);
        summary.setInteger("multiplier", inventory.getStackSizeMultiplier());

        IEnergyStorage energy = wrapper.getEnergyStorage();
        if (energy != null) {
            summary.setInteger("energy", energy.getEnergyStored());
        }
        StorageFluidHandler fluids = wrapper.getFluidHandler();
        if (fluids != null) {
            NBTTagList tanks = new NBTTagList();
            for (IFluidTankProperties tank : fluids.getTankProperties()) {
                FluidStack contents = tank.getContents();
                tanks.appendTag(contents == null ? new NBTTagCompound() : contents.writeToNBT(new NBTTagCompound()));
            }
            summary.setTag("tanks", tanks);
        }
        return summary;
    }
}
