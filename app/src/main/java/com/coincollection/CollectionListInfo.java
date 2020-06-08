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

    CollectionListInfo() {
    }

    CollectionListInfo(String name, int max, int collected, int index, int displayType) {
        mCollectionName = name;
        mTotalCoinsInCollection = max;
        mTotalCoinsCollected = collected;
        mCollectionTypeIndex = index;
        mDisplayType = displayType;
    }

    public String getName(){
    	return mCollectionName;
    }
    
    public void setName(String name){
    	mCollectionName = name;
    }

    int getMax(){
    	return mTotalCoinsInCollection;
    }
    
    void setMax(int max){
    	mTotalCoinsInCollection = max;
    }
    
    public int getCollected(){
        return mTotalCoinsCollected;
    }
    
    public void setCollected(int total){
        mTotalCoinsCollected = total;
    }

    int getCollectionTypeIndex(){
        return mCollectionTypeIndex;
    }

    void setCollectionTypeIndex(int index){
        mCollectionTypeIndex = index;
    }

    int getDisplayType(){
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

    CollectionInfo getCollectionObj() {
        return MainApplication.COLLECTION_TYPES[mCollectionTypeIndex];
    }

    /* We make this object Parcelable so that the list can be passed between Activities in the case
     * where a screen orientation change occurs.
     */
    private CollectionListInfo(Parcel in) {
        mCollectionName = in.readString();
        mTotalCoinsInCollection = in.readInt();
        mTotalCoinsCollected = in.readInt();
        mCollectionTypeIndex = in.readInt();
        mDisplayType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCollectionName);
        dest.writeInt(mTotalCoinsInCollection);
        dest.writeInt(mTotalCoinsCollected);
        dest.writeInt(mCollectionTypeIndex);
        dest.writeInt(mDisplayType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollectionListInfo> CREATOR = new Creator<CollectionListInfo>() {
        @Override
        public CollectionListInfo createFromParcel(Parcel in) {
            return new CollectionListInfo(in);
        }

        @Override
        public CollectionListInfo[] newArray(int size) {
            return new CollectionListInfo[size];
        }
    };
}
