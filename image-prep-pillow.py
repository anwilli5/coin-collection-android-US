#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Processes coin images for the coin collection app using Pillow.

This is a standalone alternative to image-prep.py (GIMP plugin).
Requires: pip install Pillow

Usage:
    python3 image-prep-pillow.py /tmp/cc_images_pre
    python3 image-prep-pillow.py /tmp/cc_images_pre --output /tmp/cc_images
"""

import argparse
import os
import sys

from PIL import Image, ImageDraw


def remove_white_background(img):
    """Flood-fill from all four corners to replace white background with transparency."""
    w, h = img.size
    for x, y in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]:
        ImageDraw.floodfill(img, (x, y), (0, 0, 0, 0), thresh=30)
    return img


def process_images(src_path, output_path):
    if not os.path.exists(src_path):
        print(f"Error: Unable to access src path: {src_path}", file=sys.stderr)
        sys.exit(1)

    img_names = os.listdir(src_path)
    print(f"Found images: {img_names}")

    os.makedirs(output_path, exist_ok=True)

    for img_name in img_names:
        input_file = os.path.join(src_path, img_name)

        try:
            img = Image.open(input_file).convert("RGBA")
        except Exception as e:
            print(f"Warning: Could not load {img_name}, skipping ({e})")
            continue

        filename, _ = os.path.splitext(img_name)

        # Remove white background via flood fill from corners
        remove_white_background(img)

        # Autocrop to bounding box of non-transparent content
        bbox = img.getbbox()
        if bbox:
            img = img.crop(bbox)

        # Scale to 92x92
        img = img.resize((92, 92), Image.LANCZOS)

        # Save as PNG
        out_file = os.path.join(output_path, f"{filename}.png")
        img.save(out_file, "PNG")

        print(f"  Processed: {img_name} -> {out_file}")

    print("Done!")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Process coin images for the coin collection app.")
    parser.add_argument("src_path", help="Directory containing source images")
    parser.add_argument("--output", default="/tmp/cc_images",
                        help="Output directory (default: /tmp/cc_images)")
    args = parser.parse_args()
    process_images(args.src_path, args.output)
