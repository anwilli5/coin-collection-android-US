#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# This is a GIMP 3.0+ plug-in (Python 3) that takes a coin image and converts
# it into the files needed by the app. An example invocation is as follows:
#
# 1. Install the plug-in:
#    mkdir -p ~/.config/GIMP/3.0/plug-ins/coin-collection-android-image-prep
#    cp image-prep.py ~/.config/GIMP/3.0/plug-ins/coin-collection-android-image-prep/
#    chmod u+x ~/.config/GIMP/3.0/plug-ins/coin-collection-android-image-prep/image-prep.py
#
# 2. Run in batch mode:
#    /path/to/gimp --batch-interpreter=python-fu-eval -ib \
#    "import sys; sys.path.insert(0, '.'); exec(open('image-prep.py').read())" \
#    --quit
#
#    Or invoke the registered procedure directly:
#    /path/to/gimp -idf --batch-interpreter=python-fu-eval -b \
#    "procedure = Gimp.get_pdb().lookup_procedure('coin-collection-android-image-prep'); \
#     config = procedure.create_config(); \
#     config.set_property('src-path', '/tmp/cc_images_pre'); \
#     procedure.run(config)" -b "(gimp-quit 0)"
#
# This can also be run from the application by navigating to:
# Filters->CoinCollection->Image-Prep
#
# NOTES:
# - Replace /tmp/cc_images_pre with the path to the files you want to process
# - Images will end up in /tmp/cc_images
# - Requires GIMP 3.0.8+ with Python 3 support

"""
Processes coin images for the coin collection app.

Coin images are read in from a specified input
directory (e.g. /tmp/cc_images_pre) and the new
images are written to /tmp/cc_images.
"""

import os
import sys

import gi
gi.require_version('Gimp', '3.0')
from gi.repository import Gimp
from gi.repository import GObject
from gi.repository import GLib
from gi.repository import Gio

PLUG_IN_PROC = "coin-collection-android-image-prep"


def process_images(procedure, config, data):
    src_path = config.get_property('src-path')

    print("Processing coin images...")

    if not os.path.exists(src_path):
        return procedure.new_return_values(
            Gimp.PDBStatusType.CALLING_ERROR,
            GLib.Error(f"Unable to access src path: {src_path}"))

    img_names = os.listdir(src_path)
    print(f"Found images: {img_names}")

    # Make the output directory if it doesn't exist
    if not os.path.exists("/tmp/cc_images"):
        os.mkdir("/tmp/cc_images")

    for img_name in img_names:
        input_path = os.path.join(src_path, img_name)

        # Load the file with GIMP
        img = Gimp.file_load(
            Gimp.RunMode.NONINTERACTIVE,
            Gio.File.new_for_path(input_path))

        if img is None:
            print(f"Warning: Could not load {img_name}, skipping")
            continue

        # Get the layer (loaded images have one layer)
        layers = img.list_layers()
        layer = layers[0]

        # Get the image file name without the extension
        filename, _ = os.path.splitext(img_name)

        # Add an alpha layer to the image (a transparent background)
        layer.add_alpha()

        # Select the white region around the coin image and delete it
        img.select_contiguous_color(
            Gimp.ChannelOps.REPLACE, layer, 1.0, 1.0)
        layer.edit_clear()

        # Autocrop the image so that there is a uniform border for all
        # images (none)
        Gimp.get_pdb().run_procedure('plug-in-autocrop', [
            GObject.Value(Gimp.RunMode, Gimp.RunMode.NONINTERACTIVE),
            GObject.Value(Gimp.Image, img),
            GObject.Value(Gimp.Drawable, layer),
        ])

        # Clear the selection
        Gimp.Selection.none(img)

        # Resize the image
        img.scale(92, 92)

        # Refresh the drawable reference after scale
        layer = img.list_layers()[0]

        # Save this image as a PNG file
        output_path = f"/tmp/cc_images/{filename}.png"
        Gimp.file_save(
            Gimp.RunMode.NONINTERACTIVE,
            img,
            Gio.File.new_for_path(output_path),
            None)

        # Clean up
        img.delete()

        print(f"  Processed: {img_name} -> {output_path}")

    print("Done!")
    return procedure.new_return_values(Gimp.PDBStatusType.SUCCESS, None)


class CoinCollectionImagePrep(Gimp.PlugIn):
    def do_query_procedures(self):
        return [PLUG_IN_PROC]

    def do_create_procedure(self, name):
        if name != PLUG_IN_PROC:
            return None

        procedure = Gimp.Procedure.new(
            self, name, Gimp.PDBProcType.PLUGIN,
            process_images, None)

        procedure.set_menu_label("Coin Collection Image-Prep")
        procedure.set_attribution("anwilli5", "Andrew Williams", "2025")
        procedure.add_menu_path("<Image>/Filters/CoinCollection")
        procedure.set_documentation(
            "Takes directory of coin images and prepares them for use by app",
            "Processes coin images for the coin collection app. "
            "Coin images are read in from a specified input directory "
            "(e.g. /tmp/cc_images_pre) and the new images are written "
            "to /tmp/cc_images.",
            None)

        procedure.add_string_argument(
            "src-path",
            "Source Path",
            "Path to the folder with all the source images",
            "/tmp/cc_images_pre",
            GObject.ParamFlags.READWRITE)

        return procedure


Gimp.main(CoinCollectionImagePrep.__gtype__, sys.argv)
