/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.command;

import com.exiledradio.sbackpackslegacy.api.CapabilityBackpackWrapper;
import com.exiledradio.sbackpackslegacy.backpack.AccessLogRecord;
import com.exiledradio.sbackpackslegacy.backpack.BackpackStorage;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Admin recovery tool, as in the original:
 * {@code /sbackpacks list [player]} lists backpacks by who opened them last;
 * {@code /sbackpacks give <player> <uuid>} hands out a backpack that opens those contents;
 * {@code /sbackpacks removeNonPlayer <onlyEmpty>} drops contents of backpacks no player ever opened.
 */
public class BackpacksCommand extends CommandBase {
    private static final String LANG = "commands.sbackpackslegacy.";

    @Nonnull
    @Override
    public String getName() {
        return "sbackpacks";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("sophisticatedbackpackslegacy");
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return LANG + "usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        switch (args[0]) {
            case "list":
                list(sender, args.length > 1 ? args[1] : null);
                break;
            case "give":
                if (args.length < 3) {
                    throw new WrongUsageException(getUsage(sender));
                }
                give(server, sender, args[1], args[2]);
                break;
            case "removeNonPlayer":
                if (args.length < 2) {
                    throw new WrongUsageException(getUsage(sender));
                }
                int removed = BackpackStorage.get().removeNonPlayerBackpackContents(parseBoolean(args[1]));
                sender.sendMessage(new TextComponentTranslation(LANG + "remove_non_player.success", removed));
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private static void list(ICommandSender sender, @Nullable String playerName) {
        List<AccessLogRecord> logs = new ArrayList<>();
        for (AccessLogRecord record : BackpackStorage.get().getAccessLogs().values()) {
            if (playerName == null || record.getPlayerName().equalsIgnoreCase(playerName)) {
                logs.add(record);
            }
        }
        logs.sort(Comparator.comparingLong(AccessLogRecord::getAccessTime).reversed());
        sender.sendMessage(new TextComponentTranslation(LANG + "list.header"));
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        for (AccessLogRecord record : logs) {
            TextComponentString name = new TextComponentString(record.getBackpackName());
            name.getStyle().setColor(TextFormatting.GREEN)
                    .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sbackpacks give @p " + record.getBackpackUuid()))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new TextComponentTranslation("chat.sbackpackslegacy.backpack_uuid.tooltip", record.getBackpackUuid())));
            TextComponentString line = new TextComponentString("");
            line.appendSibling(name);
            // 1.12 chat has no RGB colours, so the colours are shown as hex values.
            line.appendText(", ");
            line.appendSibling(new TextComponentTranslation(LANG + "list.cloth_color"));
            line.appendText(String.format(" #%06X ", record.getClothColor() & 0xFFFFFF));
            line.appendSibling(new TextComponentTranslation(LANG + "list.trim_color"));
            line.appendText(String.format(" #%06X, %s, %s", record.getTrimColor() & 0xFFFFFF, record.getPlayerName(), dateFormat.format(new Date(record.getAccessTime()))));
            // The UUID in the line too: a console can't hover or click.
            TextComponentString uuid = new TextComponentString(" " + record.getBackpackUuid());
            uuid.getStyle().setColor(TextFormatting.DARK_GRAY);
            line.appendSibling(uuid);
            sender.sendMessage(line);
        }
    }

    private static void give(MinecraftServer server, ICommandSender sender, String target, String uuidString) throws CommandException {
        EntityPlayerMP player = getPlayer(server, sender, target);
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new CommandException(LANG + "give.bad_uuid", uuidString);
        }
        AccessLogRecord record = BackpackStorage.get().getAccessLogs().get(uuid);
        if (record == null) {
            throw new CommandException(LANG + "give.unknown_uuid", uuidString);
        }
        Item item = Item.REGISTRY.getObject(record.getBackpackItem());
        if (item == null) {
            throw new CommandException(LANG + "give.unknown_uuid", uuidString);
        }
        ItemStack backpack = new ItemStack(item);
        BackpackWrapper wrapper = CapabilityBackpackWrapper.get(backpack);
        if (wrapper == null) {
            throw new CommandException(LANG + "give.unknown_uuid", uuidString);
        }
        wrapper.setContentsUuid(uuid);
        wrapper.setColors(record.getClothColor(), record.getTrimColor());
        if (!backpack.getDisplayName().equals(record.getBackpackName())) {
            backpack.setStackDisplayName(record.getBackpackName());
        }
        if (!player.inventory.addItemStackToInventory(backpack)) {
            player.dropItem(backpack, false);
        }
        player.inventoryContainer.detectAndSendChanges();
        sender.sendMessage(new TextComponentTranslation(LANG + "give.success", player.getDisplayName()));
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "give", "removeNonPlayer");
        }
        if (args.length == 2 && ("give".equals(args[0]) || "list".equals(args[0]))) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        if (args.length == 2 && "removeNonPlayer".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        return Collections.emptyList();
    }
}
