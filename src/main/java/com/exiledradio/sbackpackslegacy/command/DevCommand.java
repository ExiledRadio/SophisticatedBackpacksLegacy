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
        if (args.length >= 2 && "cancelplace".equals(args[0])) {
            cancelPlacement = "on".equals(args[1]);
            reply(sender, "Cancel block placement: " + cancelPlacement);
            return;
        }
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
            case "useon": {
                // useon x y z face: a sneaking fake player places a filled backpack on that face, through
                // ItemStack.onItemUse as the game does (so Forge's place event runs).
                FakePlayer player = FakePlayerFactory.getMinecraft(world);
                player.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                player.setSneaking(true);
                ItemStack backpack = filledBackpack();
                player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, backpack);
                net.minecraft.util.EnumFacing face = args.length > 4 ? net.minecraft.util.EnumFacing.byName(args[4]) : net.minecraft.util.EnumFacing.UP;
                net.minecraft.util.EnumActionResult result = backpack.onItemUse(player, world, pos, net.minecraft.util.EnumHand.MAIN_HAND, face, 0.5F, 1F, 0.5F);
                ItemStack held = player.getHeldItemMainhand();
                player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, ItemStack.EMPTY);
                player.setSneaking(false);
                reply(sender, "Use on " + pos + ": " + result + ", held " + held.getCount() + ", block there " + world.getBlockState(pos.offset(face)).getBlock().getRegistryName()
                        + ", backpack items near " + countBackpackItems(world, pos));
                return;
            }
            case "harvest": {
                // harvest x y z [tool|-] [sneak]: a survival fake player breaks the block.
                FakePlayer player = FakePlayerFactory.getMinecraft(world);
                player.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                player.interactionManager.setGameType(net.minecraft.world.GameType.SURVIVAL);
                player.setSneaking(args.length > 5 && "sneak".equals(args[5]));
                player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, args.length > 4 && !"-".equals(args[4]) ? new ItemStack(getItemByText(sender, args[4])) : ItemStack.EMPTY);
                int before = countBackpackItems(world, pos);
                boolean broken = player.interactionManager.tryHarvestBlock(pos);
                player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, ItemStack.EMPTY);
                player.setSneaking(false);
                reply(sender, "Harvest " + pos + ": " + broken + ", backpack items near before " + before + " after " + countBackpackItems(world, pos));
                return;
            }
            case "lockcheck": {
                // Opens a filled backpack from the fake player's main slot 3 and tries every way of moving it.
                FakePlayer player = FakePlayerFactory.getMinecraft(world);
                player.inventory.clear();
                ItemStack backpack = filledBackpack();
                player.inventory.setInventorySlotContents(3, backpack);
                BackpackContainer container = BackpackContainer.create(player, new BackpackContext.Item(BackpackContext.InventoryKind.MAIN, 3));
                if (container == null) {
                    throw new CommandException("No container");
                }
                int lockedSlot = -1;
                for (net.minecraft.inventory.Slot slot : container.inventorySlots) {
                    if (slot.inventory == player.inventory && slot.getSlotIndex() == 3) {
                        lockedSlot = slot.slotNumber;
                    }
                }
                StringBuilder out = new StringBuilder("Lock check (slot " + lockedSlot + "):");
                container.slotClick(lockedSlot, 0, net.minecraft.inventory.ClickType.PICKUP, player);
                out.append(" pickup ").append(player.inventory.getStackInSlot(3) == backpack);
                container.slotClick(lockedSlot, 0, net.minecraft.inventory.ClickType.QUICK_MOVE, player);
                out.append(", shift ").append(player.inventory.getStackInSlot(3) == backpack);
                container.slotClick(0, 3, net.minecraft.inventory.ClickType.SWAP, player);
                out.append(", number key onto storage ").append(player.inventory.getStackInSlot(3) == backpack);
                container.slotClick(lockedSlot, 1, net.minecraft.inventory.ClickType.THROW, player);
                out.append(", throw ").append(player.inventory.getStackInSlot(3) == backpack);
                container.slotClick(-999, 0, net.minecraft.inventory.ClickType.QUICK_CRAFT, player);
                container.slotClick(lockedSlot, 1, net.minecraft.inventory.ClickType.QUICK_CRAFT, player);
                container.slotClick(-999, 2, net.minecraft.inventory.ClickType.QUICK_CRAFT, player);
                out.append(", drag ").append(player.inventory.getStackInSlot(3) == backpack);
                out.append(", canInteract ").append(container.canInteractWith(player));
                player.inventory.setItemStack(ItemStack.EMPTY);
                player.inventory.clear();
                reply(sender, out.toString());
                return;
            }
            case "memoryinsert": {
                // memoryinsert x y z slot memItem insertItem: memorizes the slot for memItem, then inserts
                // insertItem into that slot and anywhere through the block's item capability, as a hopper would.
                BackpackWrapper wrapper = wrapper(world, pos);
                int slot = parseInt(args[4]);
                com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory memory = wrapper.getSettingsHandler()
                        .getTypeCategory(com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory.class);
                memory.setFilter(slot, new ItemStack(getItemByText(sender, args[5])));
                IItemHandler io = world.getTileEntity(pos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, net.minecraft.util.EnumFacing.UP);
                ItemStack left = io.insertItem(slot, new ItemStack(getItemByText(sender, args[6]), 5), false);
                reply(sender, "Memory insert into slot " + slot + ": left " + left.getCount() + " of 5, slot now " + wrapper.getInventoryHandler().getStackInSlot(slot));
                return;
            }
            case "craftupgrade": {
                // craftupgrade x y z [click|shift]: a crafting table at the position, eight iron ingots
                // around a filled leather backpack, the result taken by a click or a shift-click.
                world.setBlockState(pos, net.minecraft.init.Blocks.CRAFTING_TABLE.getDefaultState());
                FakePlayer player = FakePlayerFactory.getMinecraft(world);
                player.inventory.clear();
                player.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                if (player.connection == null) {
                    // The workbench sends its result slot straight to the player's connection.
                    net.minecraft.network.NetworkManager manager = new net.minecraft.network.NetworkManager(net.minecraft.network.EnumPacketDirection.CLIENTBOUND);
                    try {
                        // A channel that swallows packets, so the recipe book and slot updates have somewhere to go.
                        java.lang.reflect.Field channel = net.minecraft.network.NetworkManager.class.getDeclaredField("channel");
                        channel.setAccessible(true);
                        channel.set(manager, new io.netty.channel.embedded.EmbeddedChannel());
                    } catch (ReflectiveOperationException e) {
                        throw new CommandException(e.toString());
                    }
                    new net.minecraft.network.NetHandlerPlayServer(server, manager, player);
                }
                for (net.minecraft.item.crafting.IRecipe recipe : net.minecraft.item.crafting.CraftingManager.REGISTRY) {
                    // Already known recipes are not sent to the (fake) connection.
                    player.getRecipeBook().unlock(recipe);
                }
                net.minecraft.inventory.ContainerWorkbench bench = new net.minecraft.inventory.ContainerWorkbench(player.inventory, world, pos);
                player.openContainer = bench;
                ItemStack backpack = filledBackpack();
                java.util.UUID uuid = new BackpackWrapper(backpack).getContentsUuid();
                for (int i = 0; i < 9; i++) {
                    bench.craftMatrix.setInventorySlotContents(i, i == 4 ? backpack : new ItemStack(net.minecraft.init.Items.IRON_INGOT));
                }
                ItemStack shown = bench.getSlot(0).getStack();
                boolean shift = args.length > 4 && "shift".equals(args[4]);
                if (shift) {
                    bench.slotClick(0, 0, net.minecraft.inventory.ClickType.QUICK_MOVE, player);
                } else {
                    bench.slotClick(0, 0, net.minecraft.inventory.ClickType.PICKUP, player);
                }
                int leftInGrid = 0;
                for (int i = 0; i < 9; i++) {
                    leftInGrid += bench.craftMatrix.getStackInSlot(i).getCount();
                }
                int backpacks = 0;
                String found = "";
                java.util.List<ItemStack> all = new java.util.ArrayList<>(player.inventory.mainInventory);
                all.add(player.inventory.getItemStack());
                for (ItemStack stack : all) {
                    if (stack.getItem() instanceof BackpackItem) {
                        backpacks++;
                        BackpackWrapper wrapper = new BackpackWrapper(stack);
                        found += " " + stack.getItem().getRegistryName().getPath() + " sameUuid=" + uuid.equals(wrapper.getContentsUuid()) + " (" + uuid + " / " + wrapper.getContentsUuid() + ")"
                                + " slots=" + wrapper.getNumberOfInventorySlots() + " slot0=" + wrapper.getInventoryHandler().getStackInSlot(0);
                    }
                }
                player.openContainer = player.inventoryContainer;
                player.inventory.setItemStack(ItemStack.EMPTY);
                player.inventory.clear();
                reply(sender, "Craft upgrade (" + (shift ? "shift" : "click") + "): result shown " + shown.getItem().getRegistryName()
                        + ", left in grid " + leftInGrid + ", backpacks with player " + backpacks + found);
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

    private static boolean cancelPlacement = false;

    /** Cancels every block placement while "cancelplace on" is set, as a protection mod would. */
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy.MODID)
    public static final class PlacementCanceller {
        private PlacementCanceller() {}

        @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
        public static void onPlace(net.minecraftforge.event.world.BlockEvent.PlaceEvent event) {
            if (cancelPlacement) {
                event.setCanceled(true);
            }
        }
    }

    /** A leather backpack with contents (UUID assigned) holding 10 diamonds. */
    private static ItemStack filledBackpack() {
        ItemStack backpack = new ItemStack(BackpackItem.forTier(BackpackTier.LEATHER));
        BackpackWrapper wrapper = new BackpackWrapper(backpack);
        wrapper.getInventoryHandler().setStackInSlot(0, new ItemStack(net.minecraft.init.Items.DIAMOND, 10));
        return backpack;
    }

    private static int countBackpackItems(WorldServer world, BlockPos pos) {
        int count = 0;
        for (net.minecraft.entity.item.EntityItem item : world.getEntitiesWithinAABB(net.minecraft.entity.item.EntityItem.class, new net.minecraft.util.math.AxisAlignedBB(pos).grow(3))) {
            if (!item.isDead && item.getItem().getItem() instanceof BackpackItem) {
                count += item.getItem().getCount();
            }
        }
        return count;
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

    private static void reply(ICommandSender sender, String text) {
        sender.sendMessage(new TextComponentString(text));
    }
}
