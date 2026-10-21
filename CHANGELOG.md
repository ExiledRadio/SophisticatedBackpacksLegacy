# Changelog

## 1.0.0 (2026-10-17)

First release of Sophisticated Backpacks Legacy, an unofficial 1.12.2 port of P3pp3rF1y's Sophisticated Backpacks (GPLv3, `1.16.x` branch).

**Requires Sophisticated Core Legacy 1.1.0 or later** and Forge 14.23.5.2847 or later. Optional: Baubles (body slot), JEI/HEI.

Contents:
- **Five backpacks:** leather, iron, gold, diamond and Nether Star, plus a **copper** backpack when a mod adds copper and a **netherite** backpack (beside the Nether Star one) when a mod adds netherite, as in the original 1.20. Dyeable on two colours. Wear one in the chest slot or the Baubles body slot, open it with a key, or place it as a block whose upgrades keep working.
- **Upgrades:** pickup, filter, magnet, void, restock, deposit, refill, feeding, compacting, everlasting, crafting, smelting, auto-smelting, tool swapper, jukebox, tank, battery, pump, XP pump, inception (backpacks inside backpacks), and stack upgrades, with advanced versions where the original has them.
- **Settings:** backpack and player-wide settings, memory slots, no-sort slots, item display on the backpack.
- Shift tooltip with the backpack's contents, sort buttons and key, `/sbackpacks` command, access log, backpacks in dungeon, mineshaft, temple, fortress, mansion and end city loot.
- **Open another player's backpack** by right-clicking it from behind (config `allowOpeningOtherPlayerBackpacks`). This comes from the original's later versions and is written new here; it opens the contents only, not the settings.

Differences from the original:
- Curios is replaced by Baubles (body slot); the top backpack is the Nether Star one, with netherite and copper only when a mod adds them (the netherite backpack is crafted from a diamond backpack and an ingot; 1.12 has no smithing table).
- The smoking, blasting and stonecutter upgrades (and auto-smoking and auto-blasting) are listed and craftable only when a mod adds a smoker, blast furnace or stonecutter (Future MC, VillagerBackport and others); smoking and blasting go by VillagerBackport's rules plus Future MC's lists, and the stonecutter by those mods' stonecutter recipes.
- The backpack follows Mo' Bends' animated body (sneaking included) when Mo' Bends is installed.
- The compacting upgrade also compacts NoTreePunching rocks (`upgrades.alwaysCompactedItems`).
- Middle click (the "Sort Storage" key) over a backpack's slots sorts it.
- Inventory Tweaks is turned off for backpack screens, because its click-based sorting can't handle stacks over 64.
