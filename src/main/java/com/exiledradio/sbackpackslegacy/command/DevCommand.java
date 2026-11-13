/*
 * Written for Sophisticated Backpacks Legacy by ExiledRadio, GPLv3. Dev environment only.
 */
package com.exiledradio.sbackpackslegacy.command;

import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTier;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.backpack.NearbyBackpackActions;
import com.exiledradio.sbackpackslegacy.backpack.WornBackpack;
import com.exiledradio.sbackpackslegacy.compat.baubles.BaublesCompat;
import com.exiledradio.sbackpackslegacy.compat.wearables.WearablesCompat;
import com.exiledradio.sbackpackslegacy.compat.wearables.WearablesDevCheck;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContainer;
import com.exiledradio.sbackpackslegacy.common.gui.BackpackContext;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Dev-only checks for the loot buttons, driven from the server console:
 * <ul>
 * <li>{@code place x y z} puts down an empty leather backpack;</li>
 * <li>{@code set x y z slot item count} fills a backpack or container slot;</li>
 * <li>{@code nosort x y z slot} makes a backpack slot a no-sort slot;</li>
 * <li>{@code deposit x y z} and {@code merge x y z}: a fake player standing at the backpack runs the button;</li>
 * <li>{@code show x y z} lists a backpack's or container's non-empty slots;</li>
 * <li>{@code worn x y z}: a fake player wears a backpack in Thut's Wearables, then in Baubles, and
 * reports where {@link WornBackpack} finds it.</li>
 * </ul>
 */
public class DevCommand extends CommandBase {
    @Override
    public String getName() {
        return "sbackpacksdev";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sbackpacksdev place|show|deposit|merge|worn <x> <y> <z> | set <x> <y> <z> <slot> <item> <count> | nosort <x> <y> <z> <slot>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 4) {
            throw new WrongUsageException(getUsage(sender));
        }
        WorldServer world = (WorldServer) sender.getEntityWorld();
        BlockPos pos = parseBlockPos(sender, args, 1, false);
        switch (args[0]) {
            case "place": {
                world.setBlockState(pos, BackpackBlock.forTier(BackpackTier.LEATHER).getDefaultState());
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof BackpackTileEntity) {
                    ((BackpackTileEntity) tile).setBackpack(new ItemStack(BackpackItem.forTier(BackpackTier.LEATHER)));
                }
                reply(sender, "Placed backpack at " + pos + ": " + (tile instanceof BackpackTileEntity));
                return;
            }
            case "set": {
                IItemHandler handler = handler(world, pos);
                int slot = parseInt(args[4]);
                ItemStack stack = new ItemStack(getItemByText(sender, args[5]), parseInt(args[6], 0));
                if (handler instanceof InventoryHandler) {
                    ((InventoryHandler) handler).setStackInSlot(slot, stack);
                } else if (handler instanceof net.minecraftforge.items.IItemHandlerModifiable) {
                    ((net.minecraftforge.items.IItemHandlerModifiable) handler).setStackInSlot(slot, stack);
                } else {
                    throw new CommandException("Can't set slots at " + pos);
                }
                reply(sender, "Set " + pos + " slot " + slot + " to " + stack.getCount() + "x" + args[5]);
                return;
            }
            case "nosort": {
                BackpackWrapper wrapper = wrapper(world, pos);
                NoSortSettingsCategory noSort = wrapper.getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class);
                noSort.selectSlot(parseInt(args[4]));
                reply(sender, "No-sort slot " + args[4] + " at " + pos);
                return;
            }
            case "deposit":
            case "merge": {
                FakePlayer player = FakePlayerFactory.getMinecraft(world);
                player.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                BackpackContainer container = BackpackContainer.create(player, new BackpackContext.Block(pos));
                if (container == null) {
                    throw new CommandException("No backpack at " + pos);
                }
                if ("deposit".equals(args[0])) {
                    NearbyBackpackActions.depositToNearbyChests(player, container);
                } else {
                    NearbyBackpackActions.mergeBackpacks(player, container);
                }
                reply(sender, args[0] + " done at " + pos);
                return;
            }
            case "worn": {
                FakePlayer player = FakePlayerFactory.getMinecraft(world);
                player.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                ItemStack backpack = new ItemStack(BackpackItem.forTier(BackpackTier.LEATHER));
                StringBuilder out = new StringBuilder("Worn:");
                if (WearablesCompat.isLoaded()) {
                    out.append(' ').append(WearablesDevCheck.putOnBack(player, backpack.copy()));
                    out.append(" -> ").append(describe(WornBackpack.find(player)));
                    WearablesDevCheck.putOnBack(player, ItemStack.EMPTY);
                }
                if (BaublesCompat.isLoaded()) {
                    baubles.api.cap.IBaublesItemHandler handler = baubles.api.BaublesApi.getBaublesHandler(player);
                    int slot = baubles.api.BaubleType.BODY.getValidSlots()[0];
                    handler.setStackInSlot(slot, backpack.copy());
                    out.append(" | baubles slot ").append(slot).append(" found at ").append(BaublesCompat.findBackpackSlot(player));
                    out.append(" -> ").append(describe(WornBackpack.find(player)));
                    handler.setStackInSlot(slot, ItemStack.EMPTY);
                }
                reply(sender, out.toString());
                return;
            }
            case "show": {
                IItemHandler handler = handler(world, pos);
                if (args.length >= 5) {
                    reply(sender, describeStack(handler.getStackInSlot(parseInt(args[4], 0, handler.getSlots() - 1))));
                    return;
                }
                StringBuilder out = new StringBuilder("Show " + pos + ":");
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        out.append(' ').append(slot).append('=').append(stack.getCount()).append(stack.getItem().getRegistryName().getPath());
                    }
                }
                reply(sender, out.toString());
                return;
            }
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private static BackpackWrapper wrapper(WorldServer world, BlockPos pos) throws CommandException {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof BackpackTileEntity) || ((BackpackTileEntity) tile).getBackpackWrapper() == null) {
            throw new CommandException("No backpack at " + pos);
        }
        return ((BackpackTileEntity) tile).getBackpackWrapper();
    }

    private static IItemHandler handler(WorldServer world, BlockPos pos) throws CommandException {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof BackpackTileEntity) {
            return wrapper(world, pos).getInventoryHandler();
        }
        if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        }
        throw new CommandException("No inventory at " + pos);
    }

    private static String describe(WornBackpack worn) {
        if (worn == null) {
            return "not found";
        }
        BackpackContext context = worn.getContext();
        String kind = context.getGuiId() == BackpackContext.GUI_ITEM ? BackpackContext.InventoryKind.values()[context.getX()].name() : "gui " + context.getGuiId();
        return "found " + kind + " slot " + context.getY() + " chestSlot=" + worn.isInChestSlot();
    }

    /** {@code show x y z slot}: one stack in a container; a backpack's contents, upgrades and settings. */
    private static String describeStack(ItemStack stack) {
        BackpackWrapper wrapper = com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper.get(stack);
        if (wrapper == null) {
            return stack.getCount() + "x" + stack.getItem().getRegistryName() + " tag=" + stack.getTagCompound();
        }
        StringBuilder out = new StringBuilder(String.valueOf(stack.getItem().getRegistryName()));
        IItemHandler inventory = wrapper.getInventoryHandler();
        out.append(" slots=").append(inventory.getSlots()).append('/').append(wrapper.getNumberOfUpgradeSlots())
                .append(" colors=").append(Integer.toHexString(wrapper.getClothColor())).append(',').append(Integer.toHexString(wrapper.getBorderColor()))
                .append(" sort=").append(wrapper.getSortBy().getName()).append(" uuid=").append(wrapper.getContentsUuid()).append("\n items:");
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack item = inventory.getStackInSlot(slot);
            if (!item.isEmpty()) {
                out.append(' ').append(slot).append('=').append(item.getCount()).append(item.getItem().getRegistryName().getPath());
                if (item.getTagCompound() != null) {
                    out.append(item.getTagCompound());
                }
            }
        }
        out.append(" limit(cobble)=").append(((com.exiledradio.scorelegacy.inventory.InventoryHandler) inventory).getBaseStackLimit(new ItemStack(net.minecraft.init.Blocks.COBBLESTONE)));
        out.append("\n upgrades:");
        IItemHandler upgrades = wrapper.getUpgradeHandler();
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack upgrade = upgrades.getStackInSlot(slot);
            if (!upgrade.isEmpty()) {
                out.append(' ').append(slot).append('=').append(upgrade.getItem().getRegistryName().getPath()).append(upgrade.getTagCompound());
            }
        }
        out.append("\n settings: ").append(wrapper.getSettingsHandler().getNbt());
        return out.toString();
    }

    private static void reply(ICommandSender sender, String text) {
        sender.sendMessage(new TextComponentString(text));
    }
}
