/*
 * Coin Collection, an Android app that helps users track the coins that they've collected
 * Copyright (C) 2010-2016 Andrew Williams
 *
 * This file is part of Coin Collection.
 *
 * Coin Collection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coin Collection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coin Collection.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.spencerpages.collections;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * CollectionInfo is the abstract base class for each collection type supported
 * by the app.  It provides an outline for the API that the app core expects to
 * call for a given collection type.
 */
public abstract class CollectionInfo {

    /**
     * Returns the image id (R.drawable.image_name) that should be
     * displayed for collections of this type given the specified coin
     * identifier, coin mint, and whether the coin is in the
     * collection.  The convention used in Coin Collection for determining
     * the value to return is:
     *
     * For simple collections:
     *  - The obverse coin image is returned if inCollection
     *  - R.drawable.openslot is returned if not inCollection
     *
     * For collections with special/individualized coins:
     *  - The reverse (uniquely-styled side) is returned if inCollection
     *  - The reverse at a 25% opacity is returned if not inCollection (there
     *    is a script in the repo that will generate the 25% opacity images
     *    automatically from thee regular images.  See image-prep.py for more
     *    details.)
     *
     * @param identifier the coin identifier (Ex: '2009', 'George Washington')
     * @param mint the coin mint (Ex: '', ' D'.  NOTE: may change from ' D' to
     *             'D" in the future.)
     * @param inCollection Boolean.TRUE if the coin is in the collection
     * @return the id of an image to use for this coin
     */
    abstract public int getCoinSlotImage(
            String identifier,
            String mint,
            Boolean inCollection);

    /**
     * Returns a string used to identify this collection type
     *
     * @return Coin type (Ex: 'Presidential Dollars')
     */
    abstract public String getCoinType();

    /**
     * Returns the image identifier (R.drawable.image_name) that should be used
     * in the collection list views (Ex: the first view displayd by the app.)
     * The convention in Coin Collection is to use the coin reverse image for
     * this.
     *
     * @return the id of an image to use for this coin
     */
    abstract public int getCoinImageIdentifier();

    /**
     * Populates the parameters HashMap with the applicable options and the
     * associated default values that should be used when creating a
     * collection.  The available parameters and documentation on each can be
     * found in CoinPageCreator.
     *
     * @param parameters the HashMap that this method should populate with
     *                   CoinPageCreator.OPT_* values and their associated
     *                   default values.
     */
    abstract public void getCreationParameters(
            HashMap<String, Object> parameters);

    /**
     * Populates the identifierList and mintList ArrayLists with coin
     * identifers and mint identifiers based on the values in
     * the parameters HashMap. NOTE: The parameters HashMap has the same keys
     * that were added by the getCreationParameters call, but will have had
     * the associated values changed based on user action.
     *
     * @param parameters the HashMap that this method should use to get the
     *                   values needed to create the collection.
     * @param identifierList the ArrayList to populate with coin identifiers
     *                       (Ex: '2009', 'George Washington')
     * @param mintList the ArrayList to populate with coin mints (Ex: '', ' P')
     */
    abstract public void populateCollectionLists(
            HashMap<String, Object> parameters,
            ArrayList<String> identifierList,
            ArrayList<String> mintList);

    /**
     * Performs any updates to a collection's database tables.  This allows
     * new coins to be added, incorrect coins to be fixed, etc.
     *
     * TODO Document collection database fields
     *
     * The function should only operate on the tableName provided, and MUST
     * return the total number of coins added or removed from the collection
     * (this is used to keep an accurate count in the database table that
     * contains metadata about each of the collections.)
     *
     * @param db the SQLiteDatabase db object to use when making updates
     * @param tableName the name of the table to make updates to
     * @param oldVersion the previous database version
     * @param newVersion the new database version
     * @return the total number of collections added or removed from the collection
     */
    abstract public int onCollectionDatabaseUpgrade(
            SQLiteDatabase db,
            String tableName,
            int oldVersion,
            int newVersion);

    /**
     * Returns an attribution string that should be printed on the info page.
     * Most commonly the coin pictures will need attribution to the author.
     * If no attribution is required, an empty string can be returned.
     *
     * @return the attribution string to display
     */
    abstract public String getAttributionString();
}
