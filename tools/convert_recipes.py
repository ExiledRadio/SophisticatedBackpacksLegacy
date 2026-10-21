"""Converts the original's 1.16 upgrade recipes into 1.12 recipe JSON.

Usage:
    python -I tools/convert_recipes.py <SB116Reference dir> <SophisticatedBackpacksLegacy dir> <name> [<name> ...]

Each name is a recipe file in the original's generated data (e.g. pickup_upgrade). Tags become
ore dictionary names, 1.16-only items get their 1.12 equivalents, and the original's
"upgrade_next_tier" recipe type maps to this mod's factory of the same name.
"""
import json
import os
import sys

OLD_NS = "sophisticatedbackpacks"
NEW_NS = "sbackpackslegacy"

TAG_TO_ORE = {
    "forge:chests": "chest",
    "forge:chests/wooden": "chestWood",
    "forge:dusts/redstone": "dustRedstone",
    "forge:ender_pearls": "enderpearl",
    "forge:gems/diamond": "gemDiamond",
    "forge:gems/lapis": "gemLapis",
    "forge:glass": "blockGlass",
    "forge:ingots/gold": "ingotGold",
    "forge:ingots/iron": "ingotIron",
    "forge:leather": "leather",
    "forge:nether_stars": "netherStar",
    "forge:obsidian": "obsidian",
    "forge:storage_blocks/diamond": "blockDiamond",
    "forge:storage_blocks/gold": "blockGold",
    "forge:storage_blocks/iron": "blockIron",
    "forge:storage_blocks/redstone": "blockRedstone",
    "forge:string": "string",
    "minecraft:logs": "logWood",
    # No netherite in 1.12; the stack upgrade's top tier uses Nether Stars instead.
    "forge:storage_blocks/netherite": "netherStar",
}

ITEM_RENAMES = {
    "minecraft:glistering_melon_slice": "minecraft:speckled_melon",
}

# Items with metadata variants need an explicit "data" in 1.12 recipe JSON.
ITEM_DATA = {
    "minecraft:golden_apple": 0,
}


def convert_ingredient(ingredient):
    if "tag" in ingredient:
        tag = ingredient["tag"]
        if tag not in TAG_TO_ORE:
            raise SystemExit("no ore dictionary mapping for tag " + tag)
        return {"type": "forge:ore_dict", "ore": TAG_TO_ORE[tag]}
    item = ingredient["item"].replace(OLD_NS + ":", NEW_NS + ":")
    item = ITEM_RENAMES.get(item, item)
    converted = {"item": item}
    if item in ITEM_DATA:
        converted["data"] = ITEM_DATA[item]
    return converted


def convert(recipe):
    recipe_type = recipe["type"]
    if recipe_type == OLD_NS + ":upgrade_next_tier":
        new_type = NEW_NS + ":upgrade_next_tier"
    elif recipe_type == "minecraft:crafting_shaped":
        new_type = "forge:ore_shaped"
    else:
        raise SystemExit("unsupported recipe type " + recipe_type)
    result = recipe["result"]
    converted_result = {"item": result["item"].replace(OLD_NS + ":", NEW_NS + ":")}
    if result.get("count", 1) != 1:
        converted_result["count"] = result["count"]
    return {
        "type": new_type,
        "pattern": recipe["pattern"],
        "key": {k: convert_ingredient(v) for k, v in recipe["key"].items()},
        "result": converted_result,
    }


def main():
    ref, mod, names = sys.argv[1], sys.argv[2], sys.argv[3:]
    src = os.path.join(ref, "src", "generated", "resources", "data", OLD_NS, "recipes")
    dst = os.path.join(mod, "src", "main", "resources", "assets", NEW_NS, "recipes")
    for name in names:
        with open(os.path.join(src, name + ".json"), encoding="utf-8") as fh:
            recipe = json.load(fh)
        with open(os.path.join(dst, name + ".json"), "w", encoding="utf-8", newline="\n") as fh:
            json.dump(convert(recipe), fh, indent=2)
            fh.write("\n")
        print("converted", name)


if __name__ == "__main__":
    main()
