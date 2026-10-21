"""Generates the per-tier backpack block models and Forge blockstates.

Usage:
    python -I tools/gen_backpack_models.py <SophisticatedBackpacksLegacy dir> <SB116Reference dir>

The original builds each backpack at runtime from parts (body, side pouches or tanks, front
pouch or battery) and swaps in the tier's clip texture. Here the same parts become plain JSON:

- models/block/backpack_<part>.json: the original part models (GPL), namespace rewritten.
- models/block/<tier>/<part>.json: a part with the tier's clips, for the parts that have clips.
- blockstates/<tier>.json: a Forge blockstate that picks pouch-or-tank and pouch-or-battery
  from the block's left_tank / right_tank / battery properties.

Safe to re-run; it overwrites only the files it generates.
"""
import json
import os
import sys

NS = "sbackpackslegacy"
TIERS = {
    "backpack": "leather_clips",
    "copper_backpack": "copper_clips",
    "iron_backpack": "iron_clips",
    "gold_backpack": "gold_clips",
    "diamond_backpack": "diamond_clips",
    "netherite_backpack": "netherite_clips",
    "nether_star_backpack": "nether_star_clips",
}
PARTS = ["base", "left_pouch", "right_pouch", "front_pouch", "left_tank", "right_tank", "battery"]
# Tanks have no clips, so every tier shares the original part model.
PARTS_WITH_CLIPS = ["base", "left_pouch", "right_pouch", "front_pouch", "battery"]
FACING_Y = {"north": 0, "east": 90, "south": 180, "west": 270}


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def part_model(tier, part):
    if part in PARTS_WITH_CLIPS:
        return "%s:%s/%s" % (NS, tier, part)
    return "%s:backpack_%s" % (NS, part)


def main():
    mod, ref = sys.argv[1], sys.argv[2]
    assets = os.path.join(mod, "src", "main", "resources", "assets", NS)
    ref_models = os.path.join(ref, "src", "main", "resources", "assets", "sophisticatedbackpacks", "models", "block")

    for part in PARTS:
        with open(os.path.join(ref_models, "backpack_%s.json" % part), encoding="utf-8") as fh:
            text = fh.read().replace("sophisticatedbackpacks:", NS + ":")
        model = json.loads(text)
        # Blockbench metadata that 1.12's model loader doesn't use.
        for key in ("credit", "groups", "texture_size"):
            model.pop(key, None)
        write_json(os.path.join(assets, "models", "block", "backpack_%s.json" % part), model)

    for tier, clips in TIERS.items():
        for part in PARTS_WITH_CLIPS:
            write_json(os.path.join(assets, "models", "block", tier, "%s.json" % part), {
                "parent": "%s:block/backpack_%s" % (NS, part),
                "textures": {"clips": "%s:block/%s" % (NS, clips)},
            })

        def side(name, pouch, tank):
            return {
                "false": {"submodel": {name: {"model": part_model(tier, pouch)}}},
                "true": {"submodel": {name: {"model": part_model(tier, tank)}}},
            }

        write_json(os.path.join(assets, "blockstates", "%s.json" % tier), {
            "forge_marker": 1,
            "defaults": {"model": part_model(tier, "base")},
            "variants": {
                "facing": {facing: ({"y": y} if y else {}) for facing, y in FACING_Y.items()},
                "left_tank": side("left", "left_pouch", "left_tank"),
                "right_tank": side("right", "right_pouch", "right_tank"),
                "battery": side("front", "front_pouch", "battery"),
            },
        })
    print("generated models for", len(TIERS), "tiers")


if __name__ == "__main__":
    main()
