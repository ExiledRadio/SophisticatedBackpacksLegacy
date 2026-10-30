package com.exiledradio.sbackpackslegacy.compat.baubles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

/**
 * Entry point for everything Baubles. Callers check {@link #isLoaded()} first; only then is
 * {@link BaublesIntegration} (which names Baubles classes) ever touched or class-loaded.
 */
public final class BaublesCompat {
    private static boolean loaded = false;

    private BaublesCompat() {}

    public static void init() {
        loaded = Loader.isModLoaded("baubles");
        if (loaded) {
            MinecraftForge.EVENT_BUS.register(new BaublesIntegration());
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * The Baubles slot holding a backpack, or -1 (none, or no Baubles). Looked up rather than taken
     * from the body slot's fixed index: BaublesEX lays its slots out from its config.
     */
    public static int findBackpackSlot(EntityPlayer player) {
        return loaded ? BaublesIntegration.findBackpackSlot(player) : -1;
    }

    /** The stack in the given Baubles slot, or EMPTY without Baubles. */
    public static ItemStack getStack(EntityPlayer player, int slot) {
        return loaded ? BaublesIntegration.getStack(player, slot) : ItemStack.EMPTY;
    }
}
