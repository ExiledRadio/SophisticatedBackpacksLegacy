/*
 * Sophisticated Backpacks Legacy - an unofficial 1.12.2 port of Sophisticated Backpacks.
 * Original mod (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio, also GPLv3.
 */
package com.exiledradio.sbackpackslegacy;

import net.minecraftforge.fml.common.Mod;
import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = SophisticatedBackpacksLegacy.MODID,
        name = SophisticatedBackpacksLegacy.NAME,
        version = SophisticatedBackpacksLegacy.VERSION,
        dependencies = "required-after:" + SophisticatedCoreLegacy.MODID + "@[1.2.0,)"
        // No acceptableRemoteVersions: backpack GUIs and upgrades sync over custom packets,
        // so client and server must both have the mod.
)
public class SophisticatedBackpacksLegacy {

    public static final String MODID = "sbackpackslegacy";
    public static final String NAME = "Sophisticated Backpacks Legacy";
    // Replaced at build time by ForgeGradle from mod_version in gradle.properties.
    // Shows literally as "@VERSION@" in IDE dev runs; that is expected.
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static SophisticatedBackpacksLegacy instance;

    // Client-only code lives behind ClientProxy so that no client class is reachable from
    // common code: @SideOnly on methods doesn't stop a dedicated server from verifying them.
    @SidedProxy(
            clientSide = "com.exiledradio.sbackpackslegacy.client.ClientProxy",
            serverSide = "com.exiledradio.sbackpackslegacy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        com.exiledradio.scorelegacy.util.RecipeHelper.addAlwaysCompactedSource(() -> ModConfig.upgrades.alwaysCompactedItems);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        event.registerServerCommand(new com.exiledradio.sbackpackslegacy.command.BackpacksCommand());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        com.exiledradio.scorelegacy.compat.MaterialCompat.loadCompatRecipes(MODID);
        proxy.postInit(event);
    }
}
