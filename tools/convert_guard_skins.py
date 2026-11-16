#!/usr/bin/env python3
"""Convert Guard Villagers' 128x128 guard atlas into 64x64 player skins.

GuardVillagerModel is a HumanoidModel, so every box uses the standard humanoid unwrap;
only the head is 8x10x8 instead of 8x8x8. Body, arms and legs are copied face to face.

The head cannot just be squeezed: the source is ten rows tall with a wide nose band at the
bottom, which on an eight row head turns into a moustache. Instead the face is composed from
the guard's own pixels (brow band, eyes, cheeks, a shaded nose and a mouth), and the guard's
hat mesh - only a thin band in the atlas - becomes a brimmed hat on the base head and on the
hat overlay layer.

Usage (from the project root):
    python3 tools/convert_guard_skins.py [--source PATH_TO_GUARD_TEXTURES]

The textures default to the sibling GuardVillagers checkout. Previews are written to
build/guard-skin-previews/.
"""
from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw

PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SOURCE = PROJECT_ROOT.parent / "GuardVillagers/src/main/resources/assets/guardvillagers/textures/entity/guard"
OUT_ROOT = PROJECT_ROOT / "src/main/resources/assets/guardvillagerspatch/textures/entity/guard"
PREVIEW_ROOT = PROJECT_ROOT / "build/guard-skin-previews"

VARIANT_COUNT = 7

# part: (u, v, w, h, d) in the 128x128 guard atlas
SRC = {
    "body":      (52, 50, 8, 12, 4),
    "right_arm": (32, 75, 4, 12, 4),
    "left_arm":  (33, 48, 4, 12, 4),
    "right_leg": (16, 48, 4, 12, 4),
    "left_leg":  (16, 28, 4, 12, 4),
}

# the same parts in the 64x64 player skin
DST = {
    "head":      (0, 0, 8, 8, 8),
    "hat":       (32, 0, 8, 8, 8),
    "body":      (16, 16, 8, 12, 4),
    "right_arm": (40, 16, 4, 12, 4),
    "left_arm":  (32, 48, 4, 12, 4),
    "right_leg": (0, 16, 4, 12, 4),
    "left_leg":  (16, 48, 4, 12, 4),
}

HEAD_SRC = (49, 99, 8, 10, 8)
HAT_SRC = (0, 0, 9, 11, 9)


def faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    """Standard box unwrap: (x, y, width, height) per face."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }


def resample(px, rect, out_w: int, out_h: int) -> Image.Image:
    """Nearest-neighbour copy of rect into an out_w x out_h image."""
    x0, y0, w, h = rect
    out = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    for oy in range(out_h):
        sy = y0 + oy * h // out_h
        for ox in range(out_w):
            sx = x0 + ox * w // out_w
            out.putpixel((ox, oy), px[sx, sy])
    return out


def hat_pixels(px):
    """Crown rows and a lighter brim, sampled from the guard's hat band."""
    front = faces(*HAT_SRC)["front"]  # 9x11
    fx, fy, fw, _fh = front
    band_rows = [fy + 2, fy + 3]  # the only painted rows of the hat mesh
    crown = [[px[fx + min(fw - 1, x * fw // 8), row][:3] for x in range(8)] for row in band_rows]
    brim_pixels = [px[fx + x, band_rows[1]][:3] for x in range(fw)]
    brim = tuple(sum(c[i] for c in brim_pixels) // len(brim_pixels) for i in range(3))
    brim = tuple(min(255, c + 0x18) for c in brim)
    return crown, brim


def build_head(px, skin, crown, brim) -> None:
    du, dv, dw, dh, dd = DST["head"]
    dst = faces(du, dv, dw, dh, dd)
    head = faces(*HEAD_SRC)

    fx, fy, _fw, _fh = head["front"]
    src = lambda col, row: px[fx + col, fy + row][:3]  # noqa: E731

    brow = [src(x, 5) for x in range(8)]     # dark brow band with shaded edges
    eyes = [src(x, 6) for x in range(8)]     # white + green eyes
    cheeks = [src(x, 7) for x in range(8)]
    nose_shadow = src(3, 8)                  # guard's nose band, used as the nose base
    jaw = [src(x, 9) for x in range(8)]

    bridge = list(cheeks)
    bridge[3] = bridge[4] = cheeks[1]        # slightly darker bridge
    nose = list(cheeks)
    nose[3] = nose[4] = nose_shadow          # 2px nose base
    mouth = list(jaw)
    mouth[3] = mouth[4] = cheeks[1]          # subtle mouth line

    rows = [crown[0], crown[1], [brim] * 8, brow, eyes, bridge, nose, mouth]
    x0, y0, w, h = dst["front"]
    for y in range(h):
        for x in range(w):
            skin.putpixel((x0 + x, y0 + y), (*rows[y][x], 255))

    # sides and back: hat on the top three rows, guard skin below
    for name in ("right", "left", "back"):
        sx, sy, sw, _sh = head[name]
        dx, dy, dw2, dh2 = dst[name]
        for y in range(dh2):
            for x in range(min(sw, dw2)):
                if y < 2:
                    colour = crown[y][x]
                elif y == 2:
                    colour = brim
                else:
                    colour = px[sx + x, sy + 5 + (y - 3)][:3]
                skin.putpixel((dx + x, dy + y), (*colour, 255))

    # hat on top of the head, guard skin for the neck
    tx, ty, tw, th = dst["top"]
    for y in range(th):
        for x in range(tw):
            skin.putpixel((tx + x, ty + y), (*crown[y % 2][x], 255))
    bx, by, bw, bh = head["bottom"]
    bx2, by2, bw2, bh2 = dst["bottom"]
    for y in range(bh2):
        for x in range(bw2):
            skin.putpixel((bx2 + x, by2 + y), px[bx + x, by + y])


def build_hat_layer(skin, crown, brim) -> None:
    du, dv, dw, dh, dd = DST["hat"]
    for name, (x0, y0, w, h) in faces(du, dv, dw, dh, dd).items():
        if name == "bottom":
            continue
        for y in range(min(3, h)):
            for x in range(w):
                colour = brim if y == 2 else crown[y][x]
                skin.putpixel((x0 + x, y0 + y), (*colour, 255))


def convert(src_path: Path, dst_path: Path) -> Image.Image:
    src = Image.open(src_path).convert("RGBA")
    if src.size != (128, 128):
        raise SystemExit(f"unexpected atlas size {src.size} for {src_path}")

    skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = src.load()

    for part, (su, sv, sw, sh, sd) in SRC.items():
        du, dv, dw, dh, dd = DST[part]
        for name, srect in faces(su, sv, sw, sh, sd).items():
            drect = faces(du, dv, dw, dh, dd)[name]
            skin.paste(resample(px, srect, drect[2], drect[3]), (drect[0], drect[1]))

    crown, brim = hat_pixels(px)
    build_head(px, skin, crown, brim)
    build_hat_layer(skin, crown, brim)
    dst_path.parent.mkdir(parents=True, exist_ok=True)
    skin.save(dst_path)
    return skin


def front_view(skin: Image.Image) -> Image.Image:
    """Rough front elevation for eyeballing the skin without launching the game."""
    view = Image.new("RGBA", (16, 32), (0, 0, 0, 0))
    view.paste(skin.crop((8, 8, 16, 16)), (4, 0))       # head front
    view.paste(skin.crop((44, 20, 48, 32)), (0, 8))     # right arm front
    view.paste(skin.crop((20, 20, 28, 32)), (4, 8))     # body front
    view.paste(skin.crop((36, 52, 40, 64)), (12, 8))    # left arm front
    view.paste(skin.crop((4, 20, 8, 32)), (4, 20))      # right leg front
    view.paste(skin.crop((20, 52, 24, 64)), (8, 20))    # left leg front
    return view


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--source", type=Path, default=DEFAULT_SOURCE, help="guard texture folder")
    args = parser.parse_args()

    if not args.source.is_dir():
        raise SystemExit(f"guard textures not found at {args.source}; pass --source")

    PREVIEW_ROOT.mkdir(parents=True, exist_ok=True)
    heads = Image.new("RGBA", (8 * VARIANT_COUNT, 8), (0, 0, 0, 0))
    for i in range(VARIANT_COUNT):
        skin = convert(args.source / f"guard_{i}.png", OUT_ROOT / f"guard_{i}.png")
        view = front_view(skin)
        view.resize((view.width * 16, view.height * 16), Image.NEAREST).save(
            PREVIEW_ROOT / f"view_{i}.png")
        heads.paste(skin.crop((8, 8, 16, 16)), (8 * i, 0))

    heads.resize((heads.width * 32, heads.height * 32), Image.NEAREST).save(PREVIEW_ROOT / "heads.png")
    print(f"wrote {VARIANT_COUNT} skins to {OUT_ROOT}")
    print(f"previews in {PREVIEW_ROOT}")


if __name__ == "__main__":
    main()
