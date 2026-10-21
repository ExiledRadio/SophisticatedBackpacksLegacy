"""One-time import of GPL assets from the 1.16.x reference checkout into this mod.

Usage (run from anywhere, with Windows paths):
    python -I tools/import_assets.py <SB116Reference dir> <SophisticatedBackpacksLegacy dir>

Copies textures and the plain upgrade item models, and converts the 1.16 JSON lang files
into 1.12 .lang files. Backpack/block models and blockstates are not copied: they need hand
porting to the 1.12 model formats in Phase 2.

Safe to re-run: it overwrites what it wrote last time and touches nothing else.
"""
import json
import os
import shutil
import sys

OLD_NS = "sophisticatedbackpacks"
NEW_NS = "sbackpackslegacy"

# Features that don't exist in 1.12 (no stonecutter, smoker, blast furnace or netherite).
DROPPED = (
    "stonecutter",
    "smoking",
    "blasting",
    "netherite",
)

RENAMED = {
    "netherite_backpack": "nether_star_backpack",
}

ENGLISH_ADDITIONS = {
    "item.%s.nether_star_backpack.name" % NEW_NS: "Nether Star Backpack",
    "tile.%s.nether_star_backpack.name" % NEW_NS: "Nether Star Backpack",
    "fluid.xpjuice": "Liquid Experience",
}

# 1.16 item tags are ore dictionary names in 1.12; say so in the English text. The original
# also spells two item names in lower case, unlike every other upgrade.
ENGLISH_OVERRIDES = {
    "item.%s.feeding_upgrade.name" % NEW_NS: "Feeding Upgrade",
    "item.%s.advanced_feeding_upgrade.name" % NEW_NS: "Advanced Feeding Upgrade",
    "gui.%s.upgrades.buttons.match_tags" % NEW_NS: "Match Ore Dictionary Names",
    "gui.%s.upgrades.tag_list.title" % NEW_NS: "Ore Dictionary Names",
    "gui.%s.upgrades.tag_list.empty" % NEW_NS: "No names selected",
    "gui.%s.upgrades.buttons.add_tag" % NEW_NS: "Add Ore Dictionary Name",
    "gui.%s.upgrades.buttons.add_tag.no_item" % NEW_NS: "<- Click an item into the slot to add its ore dictionary names",
    "gui.%s.upgrades.buttons.add_tag.no_additional_tags" % NEW_NS: "All of the item's names are already added",
    "gui.%s.upgrades.buttons.remove_tag" % NEW_NS: "Remove Ore Dictionary Name",
    "gui.%s.upgrades.buttons.remove_tag.empty" % NEW_NS: "No names to remove",
    "gui.%s.upgrades.buttons.match_any_tag" % NEW_NS: "Match Any Name",
    "gui.%s.upgrades.buttons.match_all_tags" % NEW_NS: "Match All Names",
}

# 1.12 lang files are matched case-sensitively by locale code in lower_UPPER form.
LOCALE_CASE = {
    "en_us": "en_us",
    "ko_kr": "ko_kr",
    "pt_br": "pt_br",
    "ru_ru": "ru_ru",
    "zh_cn": "zh_cn",
}


def is_dropped(name):
    return any(word in name for word in DROPPED)


def copy_tree(src, dst, counter):
    for root, _dirs, files in os.walk(src):
        rel = os.path.relpath(root, src)
        for f in files:
            if is_dropped(f):
                continue
            out_dir = os.path.join(dst, rel)
            os.makedirs(out_dir, exist_ok=True)
            shutil.copyfile(os.path.join(root, f), os.path.join(out_dir, f))
            counter[0] += 1


def port_item_models(src, dst):
    """Copy plain item/generated upgrade models; skip anything with a custom loader or parent."""
    os.makedirs(dst, exist_ok=True)
    copied, skipped = [], []
    for f in sorted(os.listdir(src)):
        if is_dropped(f):
            continue
        with open(os.path.join(src, f), encoding="utf-8") as fh:
            model = json.load(fh)
        if model.get("parent") != "item/generated" or "loader" in model:
            skipped.append(f)
            continue
        text = json.dumps(model, indent=2).replace(OLD_NS + ":", NEW_NS + ":")
        with open(os.path.join(dst, f), "w", encoding="utf-8", newline="\n") as fh:
            fh.write(text + "\n")
        copied.append(f)
    return copied, skipped


def convert_key(key):
    for old, new in RENAMED.items():
        key = key.replace("." + old, "." + new)
    parts = key.split(".")
    if parts[1:2] == [OLD_NS]:
        parts[1] = NEW_NS
    elif parts[0] == "itemGroup" and parts[1:] == [OLD_NS]:
        parts[1] = NEW_NS
    # 1.12 names items "item.<key>.name" and blocks "tile.<key>.name"; sub-keys like
    # item.<ns>.<name>.tooltip stay as they are.
    if parts[0] == "block":
        parts[0] = "tile"
    if parts[0] in ("item", "tile") and len(parts) == 3:
        parts.append("name")
    return ".".join(parts)


def convert_value(value):
    # .lang files are one entry per line. Newlines become a literal \n, which our tooltip
    # code splits on (vanilla 1.12 does not unescape lang values).
    return value.replace(OLD_NS + ":", NEW_NS + ":").replace("\r", "").replace("\n", "\\n")


def convert_lang(src_dir, dst_dir):
    os.makedirs(dst_dir, exist_ok=True)
    counts = {}
    for f in sorted(os.listdir(src_dir)):
        locale = f[:-len(".json")]
        with open(os.path.join(src_dir, f), encoding="utf-8") as fh:
            entries = json.load(fh)
        lines = []
        for key, value in entries.items():
            if is_dropped(key):
                continue
            # Translations still name the netherite tier; leave the new tier English-only
            # there rather than ship a wrong name.
            if locale != "en_us" and any(old in key for old in RENAMED):
                continue
            new_key = convert_key(key)
            if locale == "en_us" and new_key in ENGLISH_OVERRIDES:
                value = ENGLISH_OVERRIDES[new_key]
            lines.append("%s=%s" % (new_key, convert_value(value)))
        if locale == "en_us":
            lines += ["%s=%s" % kv for kv in ENGLISH_ADDITIONS.items()]
        out = os.path.join(dst_dir, LOCALE_CASE.get(locale, locale) + ".lang")
        with open(out, "w", encoding="utf-8", newline="\n") as fh:
            fh.write("# Converted from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3.\n")
            fh.write("\n".join(lines) + "\n")
        counts[locale] = len(lines)
    return counts


def main():
    ref, mod = sys.argv[1], sys.argv[2]
    src_assets = os.path.join(ref, "src", "main", "resources", "assets", OLD_NS)
    dst_assets = os.path.join(mod, "src", "main", "resources", "assets", NEW_NS)

    textures = [0]
    # Folder names are kept (item/, block/): 1.12 resolves "<ns>:item/x" the same way, so the
    # models don't need their texture paths rewritten.
    copy_tree(os.path.join(src_assets, "textures"), os.path.join(dst_assets, "textures"), textures)
    print("textures copied:", textures[0])

    copied, skipped = port_item_models(os.path.join(src_assets, "models", "item"),
                                       os.path.join(dst_assets, "models", "item"))
    print("item models copied:", len(copied))
    print("item models left for Phase 2:", ", ".join(skipped))

    counts = convert_lang(os.path.join(src_assets, "lang"), os.path.join(dst_assets, "lang"))
    print("lang entries:", counts)


if __name__ == "__main__":
    main()
