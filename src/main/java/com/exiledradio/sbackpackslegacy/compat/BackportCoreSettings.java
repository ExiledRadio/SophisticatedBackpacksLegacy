/*
 * Sophisticated Backpacks Legacy - an unofficial 1.12.2 port of Sophisticated Backpacks.
 * Ported to 1.12.2 by ExiledRadio, GPLv3.
 */
package com.exiledradio.sbackpackslegacy.compat;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import net.minecraftforge.fml.common.Loader;

/**
 * Puts this mod's config on Backport Core's shared settings screen, when Core is installed.
 * Soft: called through reflection, so the mod needs no Core at runtime, and an older Core
 * without the settings package is ignored.
 */
public final class BackportCoreSettings {

    private BackportCoreSettings() {
    }

    public static void register() {
        if (!Loader.isModLoaded("backportcore")) {
            return;
        }
        try {
            Class.forName("com.exiledradio.backportcore.settings.BackportSettings")
                    .getMethod("register", String.class, Class[].class)
                    .invoke(null, SophisticatedBackpacksLegacy.MODID, new Class<?>[0]);
        } catch (Throwable t) {
            SophisticatedBackpacksLegacy.LOGGER.debug("Backport Core has no settings screen; skipping", t);
        }
    }
}
