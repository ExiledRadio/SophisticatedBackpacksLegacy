package com.exiledradio.sbackpackslegacy.compat.retro;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTier;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Turns Retro Sophisticated Backpacks' items, blocks and placed backpacks into ours once that mod
 * is removed, so a world can switch over without losing anything. Only active while Retro is not
 * installed: with both mods loaded, each keeps its own items.
 *
 * <p>Three parts: registry remaps (every stored stack and block, wherever it is, resolves to our
 * item or block), a capability hook that reads the data Retro kept on the stack (its upgrades'
 * settings are rewritten into ours at once; a backpack's contents are kept on the stack and moved
 * into our storage the first time the server uses it, see {@link RetroBackpackImporter}), and a
 * tile entity registered under Retro's id for placed backpacks.
 */
public final class RetroBackpacksCompat {
    public static final String RETRO_MODID = "retro_sophisticated_backpacks";
    private static final Map<String, String> ITEM_NAMES = new HashMap<>();
    private static final Map<String, BackpackTier> BLOCK_TIERS = new HashMap<>();
    @Nullable
    private static Field capNbtField;
    private static boolean active;

    static {
        BLOCK_TIERS.put("backpack_leather", BackpackTier.LEATHER);
        BLOCK_TIERS.put("backpack_iron", BackpackTier.IRON);
        BLOCK_TIERS.put("backpack_gold", BackpackTier.GOLD);
        BLOCK_TIERS.put("backpack_diamond", BackpackTier.DIAMOND);
        // Retro's top tier stands in for the original's netherite one, as our Nether Star one does.
        BLOCK_TIERS.put("backpack_obsidian", BackpackTier.NETHER_STAR);

        // Stack upgrades go by multiplier: Retro's run 2, 4, 8, 16, 32 (starter to tier 4), ours
        // 2, 4, 8, 16 (tier 1 to 4). Retro adds several together, ours multiply, so a mix never
        // ends up smaller. Its exponential upgrade only switched adding to multiplying, which ours
        // always do; it becomes the smallest stack upgrade.
        ITEM_NAMES.put("stack_upgrade_starter_tier", "stack_upgrade_tier_1");
        ITEM_NAMES.put("stack_upgrade_tier_1", "stack_upgrade_tier_2");
        ITEM_NAMES.put("stack_upgrade_tier_2", "stack_upgrade_tier_3");
        ITEM_NAMES.put("stack_upgrade_tier_3", "stack_upgrade_tier_4");
        ITEM_NAMES.put("stack_upgrade_tier_4", "stack_upgrade_tier_4");
        ITEM_NAMES.put("exponential_stack_upgrade", "stack_upgrade_tier_1");
        for (String same : new String[] {"upgrade_base", "crafting_upgrade", "inception_upgrade", "pickup_upgrade", "advanced_pickup_upgrade",
                "feeding_upgrade", "advanced_feeding_upgrade", "deposit_upgrade", "advanced_deposit_upgrade", "restock_upgrade",
                "advanced_restock_upgrade", "filter_upgrade", "advanced_filter_upgrade"}) {
            ITEM_NAMES.put(same, same);
        }
    }

    private RetroBackpacksCompat() {}

    /** Called from pre-init. */
    public static void init() {
        active = !Loader.isModLoaded(RETRO_MODID);
        if (!active) {
            return;
        }
        try {
            capNbtField = ReflectionHelper.findField(ItemStack.class, "capNBT");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            SophisticatedBackpacksLegacy.LOGGER.warn("Can't read item capability data; Retro Sophisticated Backpacks upgrades will convert without their settings", e);
        }
        MinecraftForge.EVENT_BUS.register(RetroBackpacksCompat.class);
    }

    public static boolean isActive() {
        return active;
    }

    /** Called from block registration, after ours: placed Retro backpacks load as this tile. */
    public static void registerTileEntity() {
        if (active) {
            GameRegistry.registerTileEntity(RetroBackpackTileEntity.class, new ResourceLocation(RETRO_MODID, "backpack"));
        }
    }

    @SubscribeEvent
    public static void onMissingItems(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (!RETRO_MODID.equals(mapping.key.getNamespace())) {
                continue;
            }
            Item target = getItem(mapping.key.getPath());
            if (target != null) {
                mapping.remap(target);
            } else {
                SophisticatedBackpacksLegacy.LOGGER.warn("No replacement for Retro Sophisticated Backpacks item {}", mapping.key);
            }
        }
    }

    @SubscribeEvent
    public static void onMissingBlocks(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
            BackpackTier tier = RETRO_MODID.equals(mapping.key.getNamespace()) ? BLOCK_TIERS.get(mapping.key.getPath()) : null;
            if (tier != null) {
                mapping.remap(BackpackBlock.forTier(tier));
            }
        }
    }

    @Nullable
    private static Item getItem(String retroPath) {
        BackpackTier tier = BLOCK_TIERS.get(retroPath);
        if (tier != null) {
            return BackpackItem.forTier(tier);
        }
        String ours = ITEM_NAMES.get(retroPath);
        if (ours == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(SophisticatedBackpacksLegacy.MODID, ours));
        return item == null || item == net.minecraft.init.Items.AIR ? null : item;
    }

    /**
     * Retro kept its data in the stack's capability tag ("ForgeCaps", under "Parent"); our items
     * have no capability data there, so any found on one of ours came over with a remapped Retro
     * stack. Runs before Forge hands that tag to the capabilities, while the stack is otherwise
     * complete, and moves the data into the stack's own tag, which is what our items read.
     */
    @SubscribeEvent
    public static void onAttachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (capNbtField == null || !isOurItem(stack.getItem())) {
            return;
        }
        NBTTagCompound retroData = getRetroData(stack);
        if (retroData == null) {
            return;
        }
        if (stack.getItem() instanceof BackpackItem) {
            RetroBackpackImporter.stashBackpackData(stack, retroData);
        } else {
            RetroBackpackImporter.convertUpgradeSettings(stack, retroData);
        }
    }

    private static boolean isOurItem(Item item) {
        ResourceLocation name = item.getRegistryName();
        return name != null && SophisticatedBackpacksLegacy.MODID.equals(name.getNamespace());
    }

    @Nullable
    private static NBTTagCompound getRetroData(ItemStack stack) {
        try {
            NBTTagCompound capNbt = (NBTTagCompound) capNbtField.get(stack);
            if (capNbt == null || !capNbt.hasKey("Parent", 10)) {
                return null;
            }
            NBTTagCompound parent = capNbt.getCompoundTag("Parent");
            return parent.isEmpty() ? null : parent;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
