#!/usr/bin/env python2
#coding: utf-8

# This is a gimp script that takes a coin image and converts it into the files
# needed by the app.  An example invocation is as follows:
#
# ln -s ~/path/to/script/image-prep.py ~/.gimp-2.10/plug-ins/coin-collection-android-image-prep.py
# /path/to/gimp --no-interface --batch \
# '(python-fu-coin-collection-android-image-prep RUN-NONINTERACTIVE "/tmp/cc_images_pre/")'
#
# This can be run from the application by navigating to:
# Filters->Python-Fu->CoinCollection->Image-Prep
#
# NOTES:
# - Replace /tmp/cc_images_pre with the path to the files you want to process
# - Images will end up in /tmp/cc_images
# - Tested with gimp version 2.8.16 and python 2.7.12

"""
Processes coin images for the coin collection app.

Coin images are read in from a specified input
directory (e.g. /tmp/cc_images_pre) and the new
images are written to /tmp/cc_images.

If run from the command line, gimp is closed when
the plug-in finishes.
"""

import os
from gimpfu import *

def plugin_main(src_path):

    print "Hello!"

    if not os.path.exists(src_path):
        raise Exception("Unable to access src path: %s" % src_path)

    img_names= os.listdir(src_path)
    print img_names

    # Make the output directory if it doesn't exist
    if not os.path.exists("/tmp/cc_images"):
        os.mkdir("/tmp/cc_images")

    for img_name in img_names:

        # Load the file with gimp
        img = pdb.gimp_file_load(os.path.join(src_path, img_name), os.path.join(src_path, img_name))
        drawable = pdb.gimp_image_get_active_drawable(img)

        # Get the image file name without the extension
        filename = os.path.basename(pdb.gimp_image_get_filename(img))
        filename, _ = os.path.splitext(filename)

        # Add an alpha layer to the image (a transparent background)
        l = pdb.gimp_image_get_active_layer(img)
        pdb.gimp_layer_add_alpha(l)

        # Select the white region around the coin image and delete it
        pdb.gimp_image_select_contiguous_color(img, CHANNEL_OP_REPLACE, drawable, 1.0, 1.0)
        pdb.gimp_edit_clear(drawable)

        # Autocrop the image so that there is a uniform border for all
        # images (none)
        pdb.plug_in_autocrop(img, drawable)

        # Clear the selection
        pdb.gimp_selection_none(img)

        # Resize the image
        pdb.gimp_image_scale(img, 92, 92)

        # Save this image as the first file
        output_filename = "/tmp/cc_images/%s.png" % filename
        pdb.file_png_save_defaults(img, drawable, output_filename, output_filename)

        # Note: 25% images are no longer generated
        # Set the opacity to 25%
        #pdb.gimp_layer_set_opacity(drawable, 25.0)

        # Merge the opacity into the main layer
        #pdb.gimp_image_merge_visible_layers(img, CLIP_TO_IMAGE)

        #drawable = pdb.gimp_image_get_active_drawable(img)

        # Save this image as the second file
        #output_filename = "/tmp/cc_images/%s_25.png" % filename
        #pdb.file_png_save_defaults(img, drawable, output_filename, output_filename)

    # Exit gimp
    if not gimp.Display:
        pdb.gimp_quit(TRUE)
    
register(
    "coin-collection-android-image-prep",
    "Takes directory of coin images and prepares them for use by app",
    globals()["__doc__"],
    "anwilli5",
    "Andrew Williams",
    "2019",
    "<Toolbox>/Xtns/Languages/Python-Fu/CoinCollection/Image-Prep",
    "",
    [
    (PF_STRING, "src_path", "Path to the folder with all the source images", "/tmp/cc_images_pre"),
    ],
    [],
    plugin_main)

main()
