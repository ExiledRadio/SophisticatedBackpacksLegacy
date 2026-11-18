# Changelog

## 1.2.1 (2026-11-17)

- Fixed the netherite backpack made on Trails & Tales Backport's smithing table (and shift-clicked off Nether Update Backport's) still keeping the diamond backpack's slot counts. It now gets the netherite slots on its first tick in your inventory.

## 1.2.0 (2026-11-12)

**Requires Sophisticated Core Legacy 1.2.0 or later.**

- **Settings screen:** this mod's settings are in Sophisticated Core's settings screen (Config button in Mods). The .cfg file doesn't change.
- Fixed a netherite backpack made on a smithing table (from mods that add one with netherite upgrades) keeping the diamond backpack's slot counts. It now gets the netherite backpack's slots and upgrade slots, as the crafting recipe gives.
- Big backpacks scroll their rows when the screen is taller than the window (from Core's fix; needs that Core).
- **Open a backpack from your inventory:** hover over a backpack in your inventory (survival or creative) and press the open-backpack key, as in the original. It works on backpacks in your main inventory, hotbar, offhand, chest slot and Baubles/Wearables slot. Inside a backpack opened this way, the key over another backpack in your inventory switches to it, and pressed anywhere else goes back to your inventory.
- **Moving from Retro Sophisticated Backpacks:** remove Retro Sophisticated Backpacks, keep this mod, and its backpacks and upgrades turn into these, wherever they are: player inventories, Baubles, chests and other containers, backpacks inside backpacks, and placed backpacks. Contents, upgrades, colors, memory and no-sort slots and the sort setting come along, and upgrades keep their filters and settings. Nothing happens while both mods are installed.
  - Retro's obsidian backpack becomes the Nether Star backpack.
  - Stack upgrades go by multiplier: Retro's starter tier and tiers 1-3 (x2 to x16) become tiers 1-4 here. Retro's tier 4 (x32) becomes tier 4 (x16) and its exponential upgrade becomes tier 1; stacks bigger than the new limit spill into empty slots, and anything still left stays in its slot rather than being lost.
  - A backpack keeps at least as many slots as it had in Retro.

## 1.1.2 (2026-10-30)

- **BaublesEX:** a backpack worn in a BaublesEX slot now counts as worn: the open keybind, the backpack on your back, upgrades and Merge Backpacks all find it. BaublesEX lays out its slots from its config, and the backpack used to be looked for only in classic Baubles' fixed body slot.
- **Thut's Wearables:** backpacks go in its back slot, with the same support as Baubles (open keybind, drawn on your back, upgrades keep working, others can open it from behind if allowed).

## 1.1.1 (2026-10-27)

- Fixed the game crashing at startup (`NoSuchMethodError: ItemStack.getItem()`). The 1.1.0 file was a development build by mistake; this one has the same content, built for normal Minecraft.

## 1.1.0 (2026-10-23)

- **Deposit to Nearby Chests** button, beside sort: each item in the backpack goes into nearby containers (8 blocks by default) that already hold that item, as Quark's deposit button does. No-sort slots stay in the backpack.
- **Merge Backpacks** button: shows while another backpack is near (8 blocks by default), placed on the ground or worn by a player standing close. It pulls the items this backpack already holds out of those backpacks, to gather loot of a kind in one place. Their no-sort slots stay where they are, other players' worn backpacks count only while `allowOpeningOtherPlayerBackpacks` is on, and merged items skip this backpack's void and compacting upgrades.
- Both ranges are in the config (`nearbyActions`); 0 removes the button.

## 1.0.0 (2026-10-21)

First release of Sophisticated Backpacks Legacy, an unofficial 1.12.2 port of P3pp3rF1y's Sophisticated Backpacks (GPLv3, `1.16.x` branch).

**Requires Sophisticated Core Legacy 1.1.0 or later** and Forge 14.23.5.2847 or later. Optional: Baubles (body slot), JEI/HEI.

Contents:
- **Five backpacks:** leather, iron, gold, diamond and Nether Star, plus a **copper** backpack when a mod adds copper and a **netherite** backpack (beside the Nether Star one) when a mod adds netherite, as in the original 1.20. Dyeable on two colours. Wear one in the chest slot or the Baubles body slot, open it with a key, or place it as a block whose upgrades keep working.
- **Upgrades:** pickup, filter, magnet, void, restock, deposit, refill, feeding, compacting, everlasting, crafting, smelting, auto-smelting, smoking, auto-smoking, blasting, auto-blasting, stonecutter, tool swapper, jukebox, tank, battery, pump, XP pump, inception (backpacks inside backpacks), and stack upgrades, with advanced versions where the original has them.
- **Settings:** backpack and player-wide settings, memory slots, no-sort slots, item display on the backpack.
- Shift tooltip with the backpack's contents, sort buttons and key, `/sbackpacks` command, access log, backpacks in dungeon, mineshaft, temple, fortress, mansion and end city loot.
- **Open another player's backpack** by right-clicking it from behind (config `allowOpeningOtherPlayerBackpacks`). This comes from the original's later versions and is written new here; it opens the contents only, not the settings.

Differences from the original:
- Curios is replaced by Baubles (body slot); the top backpack is the Nether Star one, with netherite and copper only when a mod adds them (the netherite backpack is crafted from a diamond backpack and an ingot; 1.12 has no smithing table).
- The smoking, blasting and stonecutter upgrades (and auto-smoking and auto-blasting) are listed and craftable only when a mod adds a smoker, blast furnace or stonecutter (Future MC, VillagerBackport and others); smoking and blasting go by VillagerBackport's rules plus Future MC's lists, and the stonecutter by those mods' stonecutter recipes.
- The backpack follows Mo' Bends' animated body (sneaking included) when Mo' Bends is installed.
- The compacting upgrade also compacts NoTreePunching rocks (`upgrades.alwaysCompactedItems`).
- With an upgrade tab open, shift-clicked items go to the backpack first and into the tab only once it's full, as the current original does (a setting switches it).
- Middle click (the "Sort Storage" key) over a backpack's slots sorts it.
- Inventory Tweaks is turned off for backpack screens, because its click-based sorting can't handle stacks over 64.
