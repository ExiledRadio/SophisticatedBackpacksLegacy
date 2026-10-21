package com.exiledradio.sbackpackslegacy.init;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.sbackpackslegacy.backpack.BackpackBlock;
import com.exiledradio.sbackpackslegacy.backpack.BackpackItem;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTier;
import com.exiledradio.sbackpackslegacy.backpack.BackpackTileEntity;
import com.exiledradio.sbackpackslegacy.crafting.BackpackDyeRecipe;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import com.exiledradio.sbackpackslegacy.upgrades.everlasting.EverlastingBackpackItemEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = SophisticatedBackpacksLegacy.MODID)
public final class ModContent {
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(SophisticatedBackpacksLegacy.MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(BackpackItem.forTier(BackpackTier.LEATHER));
        }
    };

    private static final List<BackpackItem> BACKPACK_ITEMS = new ArrayList<>();

    private ModContent() {}

    public static List<BackpackItem> getBackpackItems() {
        return Collections.unmodifiableList(BACKPACK_ITEMS);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (BackpackTier tier : BackpackTier.values()) {
            event.getRegistry().register(new BackpackBlock(tier));
        }
        GameRegistry.registerTileEntity(BackpackTileEntity.class, new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "backpack"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (BackpackTier tier : BackpackTier.values()) {
            BackpackItem item = new BackpackItem(tier, CREATIVE_TAB);
            BACKPACK_ITEMS.add(item);
            event.getRegistry().register(item);
        }
        ModUpgrades.registerItems(event.getRegistry(), CREATIVE_TAB);
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EverlastingBackpackItemEntity.class)
                .id(new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "everlasting_backpack"), 0)
                .name(SophisticatedBackpacksLegacy.MODID + ".everlasting_backpack")
                .tracker(64, 20, true)
                .build());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new BackpackDyeRecipe().setRegistryName(SophisticatedBackpacksLegacy.MODID, "backpack_dye"));
    }
}
