/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.init;

import com.exiledradio.sbackpackslegacy.ModConfig;
import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

/**
 * Backpacks and a few upgrades in vanilla chests, as in the original. Each table gets one extra
 * pool that rolls our own table (assets/sbackpackslegacy/loot_tables/inject/chests/*), which is
 * mostly "nothing". The original's bastion and shipwreck tables don't exist in 1.12.
 */
@Mod.EventBusSubscriber(modid = SophisticatedBackpacksLegacy.MODID)
public final class ModLoot {
    private static final String CHESTS_PREFIX = "minecraft:chests/";
    private static final Set<String> CHEST_TABLES = ImmutableSet.of("abandoned_mineshaft", "desert_pyramid", "end_city_treasure", "nether_bridge",
            "simple_dungeon", "woodland_mansion");

    private ModLoot() {}

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!ModConfig.chestLootEnabled) {
            return;
        }
        String name = event.getName().toString();
        if (!name.startsWith(CHESTS_PREFIX) || !CHEST_TABLES.contains(name.substring(CHESTS_PREFIX.length()))) {
            return;
        }
        ResourceLocation inject = new ResourceLocation(SophisticatedBackpacksLegacy.MODID, "inject/chests/" + name.substring(CHESTS_PREFIX.length()));
        LootEntry entry = new LootEntryTable(inject, 1, 0, new LootCondition[0], SophisticatedBackpacksLegacy.MODID + "_inject_entry");
        event.getTable().addPool(new LootPool(new LootEntry[] {entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1),
                SophisticatedBackpacksLegacy.MODID + "_inject_pool"));
    }
}
