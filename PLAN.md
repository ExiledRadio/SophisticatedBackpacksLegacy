# Sophisticated Backpacks Legacy: 1.12.2 backport plan

An unofficial port of P3pp3rF1y's Sophisticated Backpacks to Minecraft 1.12.2 / Forge 14.23.5.


> **Core split, 2026-10-16.** The shared code (upgrade system, storage GUI, filters, network sync,
> liquid experience, and the upgrades Storage also uses) now lives in its own mod,
> **Sophisticated Core Legacy** (`scorelegacy`), in `../SophisticatedCoreLegacy`. Backpacks
> requires it. After changing anything there, run `gradlew build copyToDependents` in that
> workspace; that puts the MCP-named `SophisticatedCoreLegacy-<version>-dev.jar` into `deps/`
> here, which this build compiles and runs against (`compile`, not `deobfCompile`; see
> `build.gradle`). What stayed here: backpack items, block, tile, worn layer, Baubles, backpack
> GUI opening, and the backpack-only upgrades (deposit, restock, refill, inception, everlasting,
> tool swapper). Registry names, NBT and config file are unchanged. A backup of the pre-split
> source is at `G:\Backups\SophisticatedBackpacksLegacy-pre-core-split-2026-10-16`.

## 1. Source and license

| | |
|---|---|
| Port source | `P3pp3rF1y/SophisticatedBackpacks`, branch `1.16.x` |
| Pinned commit | `c650e8a4248351b85537b285a0cc8b5f15a4a4f5` (2022-12-12, mod version 3.15.20, MC 1.16.5) |
| License at that commit | **GNU GPL v3** |
| Off-limits | Anything committed after **2025-02-10**, when newer branches switched to All Rights Reserved |

The port is a derivative of GPLv3 code. We take on these obligations:

- The port ships under **GPLv3**. The repo gets a `LICENSE` with the full GPLv3 text.
- The full source is public. Link it from the CurseForge page and from `mcmod.info`.
- Credit is kept: "Original mod by P3pp3rF1y" in `mcmod.info` credits, the README and the CurseForge description.
- Files we change get a short header: `Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.`
- The port has a distinct name and modid, and is described as unofficial.
- Only the pinned commit is used as reference. If we want a feature that only exists in newer (ARR) versions, we write it from scratch from in-game behaviour, or we ask the author.

### What about Sophisticated Core?

On 1.18+ the mod depends on **Sophisticated Core**, a separate library mod holding the shared GUI framework, upgrade system, settings, filters and utilities. Core was **split out of Backpacks in April 2022 for 1.18**. The 1.16.x branch has no Core dependency: all of that code is still inside Backpacks itself, in `common/gui`, `client/gui`, `upgrades/*`, `settings/*` and `util/*`. Porting from 1.16.x gives us everything without needing Core.

We don't ship a separate Core mod. Inside the port we keep the same split as **packages**: `...sbackpackslegacy.core` (GUI framework, upgrade base classes, filters, settings, utilities) and `...sbackpackslegacy.backpack` (items, block, storage, backpack-specific upgrades). If we later port Sophisticated Storage (chests, barrels), the `core` package can be moved into its own jar then.

Core's own license history matches Backpacks: GPLv3 on its 1.18.x / 1.19.x branches, All Rights Reserved from 2025-02-10. Same rule as above: we don't need it, and if we ever look at it, only pre-2025 GPL commits.

The original **textures, block JSON models and lang strings are GPL as well**, so we can reuse them. We only draw new art where 1.12 needs something that doesn't exist (see §6).

## 2. Identity and toolchain

Same setup as RLCraftTweaks and the other mods:

| Setting | Value |
|---|---|
| Workspace | `E:\MincraftModding\Workspaces\SophisticatedBackpacksLegacy` |
| modid | `sbackpackslegacy` (proposed, see §11) |
| Package | `com.exiledradio.sbackpackslegacy` |
| Forge | `14.23.5.2847`. **Never raise it**: ForgeGradle 2.3 can't consume userdev3 |
| Mappings | `stable_39` |
| Gradle | ForgeGradle2 fork, Java 8 |
| Build settings | Same `reobfJar.outputs.upToDateWhen { false }` guard, same `processResources` inputs block, no `deployToInstance` task |
| Mixins / coremod | **None needed.** The 1.16 mod only uses an access transformer, and 1.12 gets the same with an AT or reflection (`ObfuscationReflectionHelper`) |
| Reference checkout | `Workspaces/SB116Reference/` (read-only clone at the pinned SHA, like `MC1144Reference`) |

Soft dependencies (compileOnly, jars in `deps/`): Baubles 1.5.2, JEI 4.16 (HEI shares the API), InventoryTweaks API, Quark r1.6 (optional).

## 3. Scope

The 1.16.x source has **425 Java files**, 71 textures, 57 item/block models, 52 recipes, 50 advancements and 13 loot tables.

### Kept: 5 backpack tiers + 39 upgrades

| Group | Content | 1.12 notes |
|---|---|---|
| Backpacks | Leather, Iron, Gold, Diamond, *5th tier* | Netherite doesn't exist in 1.12; see §11 |
| Storage | Stack upgrade tiers 1–4, Inception | Inception = backpacks inside backpacks with upgrades working through them |
| Item movement | Pickup, Filter, Magnet, Void, Restock, Deposit, Refill (each with Advanced variants where the original has them) | Direct port |
| Utility | Feeding (+Adv), Compacting (+Adv), Everlasting, Crafting, Smelting, Auto-Smelting, Tool Swapper (+Adv), Jukebox | |
| Fluid / energy | Tank, Battery, Pump (+Adv), XP Pump | Forge 1.12 has `IFluidHandlerItem` and Forge Energy, so these port directly. XP needs our own registered fluid |
| Block | Backpacks can be placed as blocks; upgrades keep working when placed | TileEntity + `ITickable` |
| Settings | Backpack settings tab, Memory slots, No-sort slots, Item display, Context (per-player vs per-backpack) | |
| Misc | Content tooltip preview, sorting buttons, keybinds, `/sbackpacks` command, loot in dungeon chests, access log | |

### Dropped or replaced

| Original | Why | Replacement |
|---|---|---|
| Stonecutter upgrade | No stonecutter in 1.12 | Dropped |
| Smoking / Auto-Smoking upgrade | No smoker | Dropped. Optional: make the Smelting upgrade faster on food (config) |
| Blasting / Auto-Blasting upgrade | No blast furnace | Dropped. Optional: same as above for ores |
| Netherite backpack | No netherite | 5th tier from a 1.12 material (§11) |
| Curios compat | Mod doesn't exist for 1.12 | **Baubles** BODY slot |
| Botania, Crafting Tweaks, TOP compat | Low value for the target pack | Dropped for v1 |
| Data generators | 1.12 has no datagen | Hand-written JSON (recipes, loot, advancements), converted once from the 1.16 data |

That leaves **5 backpacks and 33 upgrade items**.

## 4. API translation table (1.16 → 1.12)

This table drives most of the porting work. Every file goes through these substitutions.

| 1.16 | 1.12 |
|---|---|
| `DeferredRegister` / `RegistryObject` | `@ObjectHolder` + `RegistryEvent.Register<Item/Block/...>` |
| `LazyOptional<T>` capabilities | `hasCapability` / `getCapability` + `ICapabilityProvider` |
| `ContainerType` + `MenuScreens.register` | `IGuiHandler` + `NetworkRegistry.registerGuiHandler`, `player.openGui(...)`. Gui ID encodes "held in hand / Baubles slot / block pos" |
| `Container` / `ContainerScreen` | `Container` / `GuiContainer` (similar shape; drawing is GL1 instead of `MatrixStack`) |
| `MatrixStack`, `IRenderTypeBuffer`, `IVertexBuilder` | `GlStateManager` push/pop/translate, `Tessellator` / `BufferBuilder` |
| `SimpleChannel` + lambda handlers | `SimpleNetworkWrapper` + `IMessage` / `IMessageHandler`, `addScheduledTask` to reach the main thread |
| Tags (`ItemTags`, `Tags.Items`) | OreDictionary |
| `ITextComponent` / `TranslationTextComponent` | `ITextComponent` / `TextComponentTranslation` |
| `CompoundNBT`, `ListNBT` | `NBTTagCompound`, `NBTTagList` |
| `ItemStack.getTag()` | `getTagCompound()` |
| `Item.Properties` | Setters in the constructor (`setMaxStackSize`, `setCreativeTab`) |
| `ItemStackTileEntityRenderer` (ISTER) | `TileEntityItemStackRenderer` via `Item#setTileEntityItemStackRenderer`, **or** a baked dynamic model (preferred, see §5) |
| `IModelGeometry` dynamic model | `ICustomModelLoader` + `IModel` + `ItemOverrideList`, with baked results cached by render-info key |
| `LayerRenderer<AbstractClientPlayer, PlayerModel>` | `LayerRenderer<EntityLivingBase>` added to both `RenderPlayer` skins |
| `SavedData` (`BackpackStorage`) | `WorldSavedData` on the overworld's `MapStorage` |
| JSON lang (`en_us.json`) | `en_us.lang` **plus a `pack.mcmeta` with `pack_format: 3`**, or 1.12 silently renames the file and loses every translation |
| `IRecipeSerializer` custom recipes (backpack upgrade, dye) | `IRecipe` subclasses registered in `RegistryEvent.Register<IRecipe>`, JSON factories via `_factories.json` |
| `@OnlyIn(Dist.CLIENT)` | Client code goes in **separate classes** behind the proxy. Never use `@SideOnly` on methods whose descriptors mention client types: that crashes dedicated servers |
| `Fluid` registry objects | `FluidRegistry.registerFluid` + `FluidRegistry.addBucketForFluid` (optional) |

## 5. Architecture (what stays the same)

The 1.16 design is sound, so we keep it.

- **`BackpackWrapper`** is the central object for one backpack stack: inventory handler, upgrade handler, fluid handler, settings and render info. It's exposed as a capability on the stack, the placed TileEntity and anything holding it.
- **Contents live in `BackpackStorage` (WorldSavedData), keyed by a UUID in the stack's NBT.** The item itself stays tiny. This matters even more in 1.12: a full diamond backpack with stack upgrades in item NBT would blow the packet size limit and cause book-ban style kicks.
- Upgrades follow the `UpgradeItemBase` + `UpgradeWrapperBase` + `UpgradeContainer` + `UpgradeTab` pattern. Each upgrade implements the response interfaces it needs (`IPickupResponseUpgrade`, `ITickableUpgrade`, `IInsertResponseUpgrade` and so on). Those 23 small `api/` interfaces port almost unchanged.
- **Opening a backpack goes through one entry point**: main hand, Baubles slot, inventory slot (keybind) or placed block. That code path is where dupe protection lives.

### Dupe hardening (non-negotiable)

Backpack mods are the #1 source of dupes in 1.12 packs. Rules:

1. The open container checks every tick that the backpack stack it was opened from is **still the same stack in the same place** (identity + UUID). If not, the GUI closes.
2. The open backpack's slot is locked: it can't be clicked, number-key-swapped, Q-dropped or picked up with the cursor. This includes `ClickType.SWAP`, `ClickType.THROW` and `ClickType.PICKUP_ALL`.
3. Inception: a backpack can't contain itself, and depth is capped.
4. UUID copies *(changed in Phase 1)*: every stack, tile and container with the same UUID goes through **one shared live `BackpackContents`** in `BackpackStorage`. Two stacks with the same UUID (creative copy, another mod's dupe) are just two views of the same inventory, so copying a stack can never copy items. This also removes the stale-handler overwrite that causes most backpack dupes: two handlers for one UUID, where the older one saves over the newer one's changes. The original "give the second copy a fresh empty UUID" idea was dropped because there's no reliable way to tell which copy is the "second".
5. Placing and breaking the block moves the UUID, not the contents.
6. Death, drop and `/clear` are tested with the GUI open.

## 6. Art and models (Claude as modeler)

| Asset | Plan |
|---|---|
| 51 item textures (upgrades, slot icons) | Reuse the GPL originals |
| Block textures (cloth, border, clips per tier, modules, tank glass) | Reuse. **New: 5th-tier clip texture** in the original's palette style |
| Block JSON models (`backpack_base`, pouches, tanks, battery, per-tier) | 1.12 uses the same `elements` JSON format. Port by hand: drop `render_type`, check rotations are multiples of 22.5°, convert `parent` paths |
| Worn-on-back model | 1.16 `BackpackModel.java` (301 lines of `ModelRenderer` boxes) → 1.12 `ModelBase`. The box API is almost the same, so this is a direct transcription, then a visual check against the block model |
| Item rendering | Dynamic baked model: base + optional left/right tank, battery, front pouch, cloth/border tint from dye colours. Cache by (tier, colours, tank/battery state) |
| Dye colours | `IItemColor` / `IBlockColor` using the two NBT colours (cloth, border). Port of `ModItemColors` / `ModBlockColors` |
| GUI textures | Reuse `backpack_background_9/12`, `gui_controls`, `icons`, `slots_background` |
| XP fluid | Reuse `xp_still` / `xp_flowing` + `.mcmeta` |
| New upgrade icons (if we add replacements) | Drawn as 16×16 PNGs in the same style: `upgrade_base` frame + symbol |

Art is generated with scripted pixel-editing (Python + PIL) so every change can be reproduced, then checked in-game in runClient.

## 7. Phases

Each phase ends in something that runs in `runClient` **and** `runServer` (dedicated server, to catch sided-class crashes early).

### Phase 0: Setup (half a day): DONE 2026-10-15
Reference cloned and pinned, workspace scaffolded, GPL files in place. `tools/import_assets.py` imported 66 textures, 35 upgrade item models and 5 lang files (1.16 keys converted to 1.12 `item.*.name` / `tile.*.name`; multi-line values stored as a literal `\n` that tooltip code must split on). The empty mod builds and boots in both runServer and runClient.

- Clone the reference repo at the pinned SHA into `Workspaces/SB116Reference`.
- Create the workspace from the RLCraftTweaks template, along with LICENSE (GPLv3), README with credits, `pack.mcmeta`, `mcmod.info`.
- Script a one-time asset import: copy textures/models from the reference and convert `en_us.json` → `en_us.lang` (+ the 4 translations).
- **Done when:** an empty mod loads in runClient and runServer.

### Phase 1: Core backpack (largest phase): DONE 2026-10-15
Tested by hand on a **dedicated** dev server with the dev client connected (`online-mode=false`, console commands fed through a file):

- [x] Open from hand, from the K keybind, and from the placed block; 9-column (leather/iron) and 12-column (nether star) layouts, upgrade slot column with the empty-slot icon
- [x] Contents persist across close/reopen, place → break → drop → pick up, sneak-pickup into hand, and a full server restart
- [x] Open backpack's slot: click, number-key swap, Q and Ctrl+Q are all refused
- [x] Sneak-pickup and `/fill … destroy` each leave exactly one backpack (no second drop)
- [x] Iron tier-upgrade recipe keeps contents and grows the slot count (27 → 54)
- [x] Death with the GUI open drops exactly the inventory's stacks; no extras
- [x] Server-side lang names resolve ("Nether Star Backpack")
- [ ] Not yet exercised in game: dye recipe, hopper/comparator on the placed block, the gold/diamond/nether star recipes (same code path as iron), wearing in the chest armour slot

Found while testing, deferred:
- The 120-slot GUI (294 px tall) is tight at large GUI scales, same as the original 1.16 mod. Scrolling rows belong in Phase 5.
- Vanilla 1.12 sends slot counts as a byte, so stacks over 127 will need custom container sync in Phase 3 (stack upgrades). `BackpackInventoryHandler` already saves real counts as ints.
- Shift-click verified in Phase 3 (using real keybd_event Shift), after fixing the shared-stack bug described there.

Original Phase 1 scope:
- Items: 5 tiers, with capacity from config (originals: 27 / 54 / 81 / 108 / 120 slots, 1 / 2 / 3 / 5 / 7 upgrade slots).
- `BackpackStorage`, `BackpackWrapper`, inventory, upgrade and settings handlers.
- Container + GUI, including the scroll bar for large tiers, 9 vs 12 column backgrounds and upgrade slots (no upgrades yet).
- Opening: right-click in hand, keybind (search hotbar → inventory → Baubles), shift-right-click to place.
- Block + TileEntity, break/drop keeps contents, hopper `IItemHandler` access on the block.
- Recipes: base backpack and tier upgrades that keep contents (custom `IRecipe`), dyeing recipe.
- All of §5's dupe rules.
- **Done when:** you can craft, fill, place, pick up, upgrade tiers, die and log out without losing or duplicating anything. Dupe checklist passes on a dedicated server.

### Phase 2: Rendering: DONE 2026-10-15
How it was built:
- **Worn model:** `BackpackModel` is a box-for-box port of the original (1.12 `ModelBase`). `BackpackLayer` is added to both player skins and to every biped mob renderer in post-init. Worn means the Baubles body slot first, then the chest armour slot (`WornBackpack`).
- **Baubles:** soft dependency. `BaublesCompat` checks `Loader.isModLoaded`, and only `BaublesIntegration` names Baubles classes (checked in the built jar). The UUID is pinned on equip, because Baubles syncs its slots after the open-GUI packet. Worn backpacks tick their upgrades.
- **Block:** generated by `tools/gen_backpack_models.py`. It writes per-tier part models (clips retextured) and a Forge blockstate whose `left_tank` / `right_tank` / `battery` properties pick pouch-or-tank / pouch-or-battery submodels. The properties come from the tile in `getActualState`.
- **Item:** `BackpackItemModel` replaces the item model at bake time and reuses the block's eight north-facing variants, chosen per stack from `BackpackRenderInfo`. Display transforms still come from the item JSON.
- **Render flags** (`renderInfo` in stack NBT) are read everywhere. Phase 4's tank/battery upgrades only have to write them.
- **Art:** the Nether Star rows of `textures/entity/backpack.png` are recoloured diamond rows (same script as the block clips).

Checked in game on a dedicated server (with deobfuscated Baubles in `eclipse/mods`, since `deobfProvided` keeps it off the dev runtime classpath):
- [x] Worn from the chest slot (diamond) and from the Baubles body slot over an iron chestplate (iron), back view; K opens the Baubles-worn one
- [x] Husks wearing nether star / gold backpacks (mob layer, tier clips, tank + battery variant)
- [x] Item icons for all tiers, plus tank/battery variants via NBT; first-person held; dropped item size
- [x] Placed block: full model with pouches, front pouch facing the player; all-variants backpack shows tanks and battery
- [ ] Not checked: sneaking pose, child mobs, Alex (slim) skin, elytra under a Baubles backpack
- [ ] Block selection box doesn't widen for tanks yet (the original's `BackpackShapes` does). Do this in Phase 4 with the tanks.
- [ ] Tank fluid levels and battery charge rendering: Phase 4

Original Phase 2 scope:
- Item model with tier clips and dye tints.
- Back layer on players (and on mobs wearing them, if we port the "mob spawns with backpack" loot feature).
- Baubles BODY slot: wearing and rendering from there.
- Block model + TESR for tank fill levels.
- **Done when:** all 5 tiers look right in hand, in GUI, on the back (both skins, with armour/elytra) and placed.

### Phase 3: Simple upgrades (no complex GUIs): DONE 2026-10-15
How it was built:
- **Upgrade tabs:** each upgrade's container slots are ordinary container slots, parked offscreen (`DISABLED_SLOT_X_POS`) while their tab is closed. Adding or removing an upgrade makes the server reopen the GUI (keeping the held stack). `BackpackContentsMessage` sends the upgrade inventory to the client before every open, so both sides build the same slot list.
- **Counts over 127:** `SyncSlotStackMessage` carries the real count; click results are clamped to 127 for vanilla's byte-sized verification; a full window resend is followed by a high-count resend. `mergeCursorIntoSlot` merges past maxStackSize, and taking from a big stack caps at one normal stack.
- **Tags → ore dictionary:** `PrimaryMatch.TAGS` matches ore dictionary names, and the lang strings say "ore dictionary names". "Match item" compares metadata when the item has subtypes.
- **Inserts copy the stack:** `BackpackInventoryHandler` hands `ItemStackHandler` a copy, because it stores the caller's own object in an empty slot. Vanilla container code then shrinks that object, emptying the slot after the NBT had already saved the full count. That was the cause of shift-clicked items vanishing from the screen.

Verified in game on a dedicated server:
- Pickup with an allow filter; tabs, switches, and reopen on upgrade change; shift-click both ways.
- Stack upgrade: 128 in a slot, display and taking. Removing it is refused, with the error box and the slot tinted red (ported from the original; it used to draw below the GUI, off-screen on tall backpacks).
- Magnet plus advanced compacting (nuggets → ingots). Restock and deposit with a chest (contents conserved).
- Void: a cobblestone filter destroys picked-up cobblestone and lets other items through.
- Feeding: eats bread from the backpack when hungry.
- Everlasting: survives lava. Below the world it is moved to the lowest open space with ground under it. The original lets it fall forever; floating it up got it stuck under bedrock. An intentional improvement, confirmed by the user 2026-10-16.
- Inception: backpacks are refused without it. With it, a nested backpack's own feeding upgrade feeds from the nested inventory.
- Filter upgrade (allow diamond) on a placed backpack: a hopper pulls only diamonds.
- Refill: tops up the player's inventory from the backpack.
- Advanced pickup in ore dictionary mode (`ingotIron`) picks up iron ingots but not gold.

Known gaps: an everlasting backpack rescued from the void in a real world can land in the first cave above bedrock rather than on the surface. Magnet XP pickup is Phase 4 (needs the XP fluid). Filter slot counts come from config, so client and server configs must match. QUICK_CRAFT drag still caps at 64 per slot. Swapping a stack upgrade for a lower tier is refused more strictly than in the original.

Original Phase 3 scope:
Stack tiers 1–4, Pickup, Filter, Magnet, Void, Restock, Deposit, Refill, Feeding, Compacting, Everlasting, Inception, each with Advanced variants.
- The shared filter GUI (`FilterLogic`, `ContentsFilterLogic`, allow/deny, match by NBT/meta/oredict, which replaces tag matching) is built once and reused.
- Stack upgrades need the custom slot rendering for counts above 64 (original `RenderHelper` + count formatting).
- **Done when:** every upgrade works held, in Baubles and placed (where the original supports it).

### Phase 4: Complex upgrades: BUILT 2026-10-16, mostly verified
How it was built:
- **Crafting:** vanilla's `SlotCrafting` over an `InventoryCrafting` that reads and writes the upgrade's 9 slots, so recipe matching, grid consumption, container items and crafting events are vanilla's own. The result slot is sent to the client directly, as vanilla's crafting table does. Shift-click from any upgrade tab slot goes through `BackpackContainer.transferFromUpgradeTabSlot`; the tab chooses backpack-first or inventory-first.
- **Smelting / Auto-Smelting:** `CookingLogic` keeps world-time finish points in the upgrade NBT. 1.12 has one furnace recipe table with fixed 200-tick times (`FurnaceRecipes`, `TileEntityFurnace.getItemBurnTime`).
- **Tool Swapper:** tool types are 1.12 tool classes, with filter slots for every class any registered item reports. Left-click swaps tools (`PlayerInteractEvent.LeftClickBlock`); attacking swaps to the best weapon by attack damage. The swap key (unbound by default) on the advanced version handles shears, hoes, shovels (grass paths) and wrenches (a "wrench" tool class or registry name). 1.12 has no data-driven tool registry like the original's.
- **Jukebox:** the server tracks which backpacks play and stops a disc whose backpack stopped ticking. Clients play `PositionedSoundRecord` for a placed backpack, or a `MovingSound` that follows the carrier. Client handlers go through the proxy.
- **Tank / Battery:** each takes 2 inventory columns. Columns taken are derived from the installed upgrades, not stored. Hidden slots refuse inserts, and adding the upgrade is refused while they hold items (error text plus red slots). The GUI keeps its full width and draws the tank or battery in the freed columns. A placed backpack exposes `IFluidHandler` and `IEnergyStorage`; an item backpack exposes energy, and fluids through `itemFluidHandlerEnabled` (on by default as in the original, which warns that some mods can dupe fluids through it). The model shows a tank or battery once the GUI reopens after the change.
- **Pump / Advanced Pump:** 1.12 fluid wrappers (`BlockLiquidWrapper`, `FluidBlockWrapper`, `FluidUtil.tryPlaceFluid` and `tryFluidTransfer`).
- **XP Pump + liquid experience:** registers `xpjuice` (20 mB per point, as OpenBlocks and EnderIO use) unless another mod already has; other experience fluid names are accepted too. Player experience is read with rounding and set exactly, because 1.12's `addExperience` can't go negative and drifts across many levels. The magnet now also picks up XP orbs into a tank.
- **Upgrade slot sync:** upgrade and upgrade-tab slots always go to the client directly. Vanilla drops slot updates noticed while it handles a click (`isChangingQuantityOnly`), which left a tank showing 0 mB until the GUI was reopened.

Verified in game on a dedicated server: crafting by click and shift-click (3 chests, exactly 24 planks used), smelting, auto-smelting (pulls ore and coal, pushes ingots into the backpack), tank with a bucket in both directions and live display, tank and battery column layout and tooltips, block model showing tank and battery, advanced pump pulling lava from the world, XP pump (20 → 10 levels, exactly 390 points stored, "take all" back to exactly level 20), magnet XP orbs into the tank (50 points = 1,000 mB), basic tool swapper (pickaxe → shovel on grass).

Not verified: battery charging and discharging (no Forge Energy items in dev), jukebox audio (the dev client has no sound assets; play and stop logic ran), advanced tool swapper filters, weapon swap and swap key, basic pump with a bucket in hand, pump output and fluid placement, tank and battery IO slots, fluid and energy capabilities with real pipes and cables.

Known gaps: the original's per-upgrade render effects are not ported (smoke while smelting, music notes while playing), and tank fill level is not shown on the backpack model. No JEI recipe transfer into the crafting grid (Phase 6, JEI compat).

Original Phase 4 scope:
Crafting (3×3 grid in a tab, shift-click into backpack), Smelting / Auto-Smelting, Tool Swapper (+Adv; tool detection via `getToolClasses` + `canHarvestBlock`), Jukebox (moving sound that follows the wearer, `MovingSound`), Tank, Battery, Pump (+Adv), XP Pump (+ registered XP fluid).
- **Done when:** all of these work, and fluid/energy show up on the backpack's capability for other mods' pipes when placed.

### Phase 5: Settings and polish: DONE 2026-10-16
Settings, sorting and memory live in Core (see its PLAN.md); Backpacks adds the item display, tooltip, access log and command.
- **Settings screen** (first tab, gear icon): main settings (per player, or overridden per backpack), no-sort slots, memory slots, item display. Escape goes back to the backpack.
- **Sort button + sort-by button** (name / count / ore dictionary) at the title bar. Memory and no-sort slots are skipped.
- **Memory:** ghost items drawn in the slot; other items refused; memorised items get insertion priority.
- **Item display:** drawn on the worn backpack and, through a TESR, on the front of a placed one (checked facing north and west).
- **Contents tooltip:** hold Shift over a backpack item. The server only answers for backpacks the player carries, wears or has open.
- **Access log + `/sbackpacks list [player] | give <player> <uuid> | removeNonPlayer <onlyEmpty>`** (op level 2). Backpacks opened before the access log existed count as "non-player" for `removeNonPlayer`.
- Verified in the dev client against a dedicated server: everything above, plus keep-tab-open off and shift-click into an open crafting tab first.
- Not done: item display on the inventory icon of the backpack item.

### Phase 6: Compat: DONE 2026-10-16
- **JEI/HEI** (Core's `CoreJeiPlugin`): exclusion areas for the upgrade column, tabs and sort buttons; ghost drag from JEI into filter slots; "+" recipe transfer into an open Crafting Upgrade tab. All three verified with JEI 4.16.
- **Inventory Tweaks:** containers carry `@IgnoreContainer`, so InvTweaks leaves our sort to us. Only checked at compile level: InvTweaks crashes in the dev environment (access transformer), so it needs testing in a real instance.
- **Quark:** adds no chest buttons to our GUI (verified with QuarkRotN r1.6). Its red "invalid slots" tint while Shift is held is Quark's own feature.
- **Quality Tools** (2026-10-16): `BackpackItem.getIsRepairable` accepts leather (ore dictionary `leather`). Backpacks never take damage, so this only matters to the Reforging Station, which accepts an item's repair item by default ("use repair item"). Quality Tools only gives qualities to `ItemArmor`, items whose class implements `IBauble` (ours attaches Baubles through a capability) or items listed by name, so a pack needs a quality file naming the backpacks. Celeritas has `config/qualitytools/Quailities/backpacks.json`: the trinkets pool (the one bauble backpacks such as Wearable Backpacks roll from: Healthy, Undying and so on), applying in any Baubles slot (`baubles_trinket`) and the chest slot. A quality's slots are stored on the stack when it is rolled, so a backpack that already has one keeps its old slots until reforged. Not tested in game yet.
- **Loot:** backpacks injected into 6 chest tables (dungeon, mineshaft, desert pyramid, nether fortress, woodland mansion, end city), `chestLootEnabled` config. 40 dungeon chests gave 3 leather and 1 iron.
- **Baubles keybind priority:** skipped; no clash found in testing.
- **CraftTweaker:** not needed, recipes are JSON.
- JEI parity with the original (added 2026-10-16): per-colour subtypes, so all 16 dyed backpacks plus the two-colour one are separate JEI entries; display-only dye recipes; Crafting Upgrade as a crafting catalyst. Creative/JEI colours come from the dye recipe's own `calculateColor`, so a crafted backpack matches its JEI entry. Core added JEI drag onto empty slots in the memory settings tab. All verified in the dev client.

### Phase 7: Real-instance testing and release
- Test in a copy of the Dregora instance (Cleanroom) and a plain Forge 2860 instance.
- Run Spark during heavy use (magnet + pickup + compacting on 10 players' worth of backpacks) and confirm we're not a hotspot.
- Check side by side with Wearable Backpacks (RLCraft edition) and Simply Backpacks, which are both in Dregora: no keybind clash (default to a different key, e.g. `B` is taken), and no double back render if someone wears both.
- Release: GPL source link, credits, "unofficial port" wording.

## 8. Effort estimate

| Phase | Rough size |
|---|---|
| 0 Setup | Small |
| 1 Core | ~30% of total: wrapper, storage, container, GUI framework and dupe safety |
| 2 Rendering | ~15% |
| 3 Simple upgrades | ~20% |
| 4 Complex upgrades | ~20% |
| 5 Settings / polish | ~10% |
| 6–7 Compat, testing | ~5% plus whatever testing turns up |

About 300–350 Java files in the end, from the original's 425 minus dropped features and datagen. The original GUI framework (`client/gui`, `common/gui`, ~50 files) is the part every upgrade sits on, so it has to be finished and stable before Phase 3 starts.

## 9. Testing checklist (run on a dedicated server each phase)

- [ ] runServer boots (no client classes reachable from common code)
- [ ] Open backpack → move it with number keys / Q / cursor / shift-click → blocked
- [ ] Open backpack → die → contents go with the dropped backpack, not both
- [ ] Open backpack → another player breaks the placed block → GUI closes, no dupe
- [ ] Creative middle-click copy → copy gets empty contents
- [ ] Inception: backpack in itself / A in B in A → refused
- [ ] Logout with GUI open → relog → contents intact
- [ ] Full diamond backpack with stack tier 4 → no packet-size kick when opened, picked up or dropped
- [ ] Hopper into / out of placed backpack respects filters
- [ ] Each upgrade: held, Baubles, placed

## 10. Risks

| Risk | Mitigation |
|---|---|
| Dupes | §5 rules + checklist, tested on a dedicated server, not only singleplayer |
| Client class leaks crash servers | Strict proxy split, runServer every phase |
| Dynamic item model is slow (many colour combos) | Cache baked models by key; fall back to TEISR if baking is too costly |
| GUI framework port is bigger than expected | Phase 1 and 3 both depend on it, so it's built first with only a plain inventory on top |
| Conflict with Wearable Backpacks / Simply Backpacks in RLCraft packs | Different keybind, no shared registry names, render check |
| Upgrade behaviour drifts from the original | Compare against the 1.16.5 mod in a real 1.16 instance when unsure |
| Config-driven slot counts differ between client and server | Container layout depends on filter slot counts; document that server and client configs must match, or sync them on login |

## 11. Decisions for you

1. **Name / modid:** `Sophisticated Backpacks Legacy` / `sbackpackslegacy` (recommended), or another name. The modid shouldn't be `sophisticatedbackpacks`, so nobody mistakes it for an official build.
2. **5th tier material** (replaces netherite): recommended **Obsidian + Nether Star** ("Netherstar backpack") as the endgame tier. Alternatives: Emerald, or just stop at Diamond.
3. **Smoking / blasting replacement:** drop entirely (recommended for v1), or add a config so Smelting is faster on food/ores.
4. **Target pack:** general 1.12.2 release, or tuned for RLCraft/Dregora (e.g. recipes gated behind RLCraft materials, Baubles priority over Wearable Backpacks).

**Additions from play-testing (2026-10-17):**
- **Opening another player's backpack:** right-click the back of a player who wears one (chest slot or Baubles body slot) to open it; config `allowOpeningOtherPlayerBackpacks` (default true). Contents only: their settings screen won't open. New context `BackpackContext.AnotherPlayer` (GUI id 2; settings ids moved to +3); the server sends the stack in `OtherPlayerBackpackMessage` before the GUI opens. The original added this after the 1.16 GPL branch, so it is written fresh here, not ported. **Not tested in game** (needs two players).
- **Backpack layer with Mo' Bends:** Mo' Bends swaps the body part for its own animated one, pivoted at the waist, so the vanilla sneak tilt put the backpack off the body. When the body is a Mo' Bends part, the layer follows it with `postRender`, which already ends at the shoulders (the part's post-offset). The first build also added Mo' Bends' elytra -12 offset and drew the backpack a body-length too high (user screenshot 2026-10-18); removed. Vanilla unchanged. (Aqua Acrobatics doesn't change the sneak pose; checked its mixins.)
- Compacting: `upgrades.alwaysCompactedItems` (see Storage PLAN), default NoTreePunching rocks.
- Middle click on a backpack's slots sorts it (Core).
