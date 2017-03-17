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

package com.coincollection;

import android.os.Parcel;
import android.os.Parcelable;

import com.spencerpages.MainApplication;

/**
 * Object used to represent each collection in the various list of collections
 * (the main page and the reorder page)
 */
public class CollectionListInfo implements Parcelable{
	private String mCollectionName;
    private int mTotalCoinsInCollection;
    private int mTotalCoinsCollected;
    private int mCollectionTypeIndex;
    private int mDisplayType;

    /* We make this object Parcelable so that the list can be passed between Activities in the case
     * where a screen orientation change occurs.
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CollectionListInfo createFromParcel(Parcel in){
            return new CollectionListInfo(in);
        }
        public CollectionListInfo[] newArray(int size){
            return new CollectionListInfo[size];
        }
    };

    public CollectionListInfo() {
    }

    public CollectionListInfo(String name, int max, int collected, int index, int displayType) {
        mCollectionName = name;
        mTotalCoinsInCollection = max;
        mTotalCoinsCollected = collected;
        mCollectionTypeIndex = index;
        mDisplayType = displayType;
    }

    /** Constructor required for Parcelability */
    private CollectionListInfo(Parcel in){
        String[] strings = new String[1];
        int[] ints = new int[3];

        in.readStringArray(strings);
        mCollectionName = strings[0];

        in.readIntArray(ints);
        mTotalCoinsInCollection = ints[0];
        mTotalCoinsCollected = ints[1];
        mCollectionTypeIndex = ints[2];
        mDisplayType = ints[3];
    }

    @Override
    public int describeContents(){
        // return 0 - none of our contents need special handling
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){

        String[] strings = new String[] {
            mCollectionName,
        };

        int[] ints = new int[] {
            mTotalCoinsInCollection,
            mTotalCoinsCollected,
            mCollectionTypeIndex,
            mDisplayType
        };

        dest.writeStringArray(strings);
        dest.writeIntArray(ints);
    }

    public String getName(){
    	return mCollectionName;
    }
    
    public void setName(String name){
    	mCollectionName = name;
    }

    public int getMax(){
    	return mTotalCoinsInCollection;
    }
    
    public void setMax(int max){
    	mTotalCoinsInCollection = max;
    }
    
    public int getCollected(){
        return mTotalCoinsCollected;
    }
    
    public void setCollected(int total){
        mTotalCoinsCollected = total;
    }

    public int getCollectionTypeIndex(){
        return mCollectionTypeIndex;
    }

    public void setCollectionTypeIndex(int index){
        mCollectionTypeIndex = index;
    }

    public int getDisplayType(){
        return mDisplayType;
    }

    public void setDisplayType(int displayType){
        mDisplayType = displayType;
    }

    public String getType(){
        return MainApplication.COLLECTION_TYPES[mCollectionTypeIndex].getCoinType();
    }

    public int getCoinImageIdentifier() {
        return MainApplication.COLLECTION_TYPES[mCollectionTypeIndex].getCoinImageIdentifier();
    }

    public CollectionInfo getCollectionObj() {
        return MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
    }

}
