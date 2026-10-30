package com.exiledradio.sbackpackslegacy.compat.wearables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

/**
 * Entry point for Thut's Wearables. Callers check {@link #isLoaded()} first; only then is
 * {@link WearablesIntegration} (which names Thut's classes) ever touched or class-loaded.
 */
public final class WearablesCompat {
    private static boolean loaded = false;

    private WearablesCompat() {}

    public static void init() {
        loaded = Loader.isModLoaded("thut_wearables");
        if (loaded) {
            MinecraftForge.EVENT_BUS.register(new WearablesIntegration());
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    /** The stack in the player's Wearables back slot, or EMPTY without Thut's Wearables. */
    public static ItemStack getBackStack(EntityPlayer player) {
        return loaded ? WearablesIntegration.getBackStack(player) : ItemStack.EMPTY;
    }
}
