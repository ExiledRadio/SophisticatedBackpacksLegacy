/*
 * Written for Sophisticated Backpacks Legacy by ExiledRadio, GPLv3. Dev environment only.
 */
package com.exiledradio.sbackpackslegacy.command;

import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTier;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.backpack.NearbyBackpackActions;
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
 * <li>{@code show x y z} lists a backpack's or container's non-empty slots.</li>
 * </ul>
 */
public class DevCommand extends CommandBase {
    @Override
    public String getName() {
        return "sbackpacksdev";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sbackpacksdev place|show|deposit|merge <x> <y> <z> | set <x> <y> <z> <slot> <item> <count> | nosort <x> <y> <z> <slot>";
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
            case "show": {
                IItemHandler handler = handler(world, pos);
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

    private static void reply(ICommandSender sender, String text) {
        sender.sendMessage(new TextComponentString(text));
    }
}
