"""Generates the 5th backpack tier's clip textures.

Usage:
    python -I tools/make_nether_star_clips.py <SB116Reference dir> <SophisticatedBackpacksLegacy dir>

Takes the original's diamond clips (GPL) and swaps their five-tone colour ramp for a Nether Star
palette, darkest to lightest. The grey hinge pixels are shared by every tier and left alone.

Writes two files:
- textures/block/nether_star_clips.png: block and item model clips (from diamond_clips.png).
- textures/entity/backpack.png: the worn-model texture. Each tier's clips are three rows in
  columns 0-27; the netherite rows (41-43) are replaced with recoloured diamond rows (38-40).
"""
import os
import sys

from PIL import Image

# Darkest to lightest: lavender shadow up to the near-white star core.
NETHER_STAR_RAMP = [
    (112, 96, 146),
    (150, 136, 186),
    (196, 188, 222),
    (234, 231, 214),
    (255, 255, 244),
]


def luminance(rgb):
    r, g, b = rgb[:3]
    return 0.299 * r + 0.587 * g + 0.114 * b


DIAMOND_ROWS = (38, 41)
NETHERITE_ROWS = (41, 44)
CLIP_COLUMNS = 28


def ramp_mapping(pixels):
    colours = {p[:3] for p in pixels if p[3] > 0}
    # The tier-coloured pixels are the saturated ones; the greys (r == g == b) are the hinges.
    ramp = sorted((c for c in colours if not (c[0] == c[1] == c[2])), key=luminance)
    if len(ramp) != len(NETHER_STAR_RAMP):
        raise SystemExit("expected %d ramp colours in the diamond clips, found %d" % (len(NETHER_STAR_RAMP), len(ramp)))
    return dict(zip(ramp, NETHER_STAR_RAMP))


def recolour(p, mapping):
    return mapping[p[:3]] + (p[3],) if p[:3] in mapping else p


def main():
    ref, mod = sys.argv[1], sys.argv[2]
    src_textures = os.path.join(ref, "src", "main", "resources", "assets", "sophisticatedbackpacks", "textures")
    dst_textures = os.path.join(mod, "src", "main", "resources", "assets", "sbackpackslegacy", "textures")

    im = Image.open(os.path.join(src_textures, "block", "diamond_clips.png")).convert("RGBA")
    mapping = ramp_mapping(list(im.getdata()))
    out = Image.new("RGBA", im.size)
    out.putdata([recolour(p, mapping) for p in im.getdata()])
    dst = os.path.join(dst_textures, "block", "nether_star_clips.png")
    out.save(dst)
    print("wrote", dst)

    entity = Image.open(os.path.join(src_textures, "entity", "backpack.png")).convert("RGBA")
    px = entity.load()
    diamond = [px[x, y] for y in range(*DIAMOND_ROWS) for x in range(CLIP_COLUMNS)]
    mapping = ramp_mapping(diamond)
    for row in range(NETHERITE_ROWS[1] - NETHERITE_ROWS[0]):
        for x in range(CLIP_COLUMNS):
            px[x, NETHERITE_ROWS[0] + row] = recolour(px[x, DIAMOND_ROWS[0] + row], mapping)
    dst = os.path.join(dst_textures, "entity", "backpack.png")
    entity.save(dst)
    print("wrote", dst)


if __name__ == "__main__":
    main()
