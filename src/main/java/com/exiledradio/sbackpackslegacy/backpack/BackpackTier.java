package com.exiledradio.sbackpackslegacy.backpack;

import com.exiledradio.scorelegacy.compat.MaterialCompat;

import javax.annotation.Nullable;

import com.exiledradio.sbackpackslegacy.ModConfig;

import java.util.function.Supplier;

/**
 * The backpack tiers. Registry names match the original mod; the top tier is the Nether Star. The
 * original 1.20's copper and netherite backpacks are there too, listed and craftable only when
 * another mod adds copper or netherite (see Core's MaterialCompat); netherite sits beside the
 * Nether Star with the same slots.
 */
public enum BackpackTier {
    LEATHER("backpack", "leather_clips", () -> ModConfig.leather, null),
    COPPER("copper_backpack", "copper_clips", () -> ModConfig.copper, MaterialCompat.Material.COPPER),
    IRON("iron_backpack", "iron_clips", () -> ModConfig.iron, null),
    GOLD("gold_backpack", "gold_clips", () -> ModConfig.gold, null),
    DIAMOND("diamond_backpack", "diamond_clips", () -> ModConfig.diamond, null),
    NETHERITE("netherite_backpack", "netherite_clips", () -> ModConfig.netherite, MaterialCompat.Material.NETHERITE),
    NETHER_STAR("nether_star_backpack", "nether_star_clips", () -> ModConfig.nether_star, null);

    private final String registryName;
    private final String clipsTexture;
    private final Supplier<ModConfig.Tier> config;
    @Nullable
    private final MaterialCompat.Material material;

    BackpackTier(String registryName, String clipsTexture, Supplier<ModConfig.Tier> config, @Nullable MaterialCompat.Material material) {
        this.registryName = registryName;
        this.clipsTexture = clipsTexture;
        this.config = config;
        this.material = material;
    }

    /** Whether this tier is listed and craftable: always, except copper and netherite without a mod adding them. */
    public boolean isAvailable() {
        return material == null || material.isAvailable();
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getClipsTexture() {
        return clipsTexture;
    }

    public int getInventorySlots() {
        return config.get().inventorySlotCount;
    }

    public int getUpgradeSlots() {
        return config.get().upgradeSlotCount;
    }
}
