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

package com.spencerpages;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object used to represent each collection in the various list of collections
 * (the main page and the reorder page)
 */
public class CollectionListInfo implements Parcelable{
	private String mCollectionName;
    private int mTotalCoinsInCollection;
    private int mTotalCoinsCollected;
    private String mCoinType;
    private int mCoinReverseImageIdentifier;
    private int mCoinObverseImageIdentifier;

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

    /** Constructor required for Parcelability */
    private CollectionListInfo(Parcel in){
        String[] strings = new String[2];
        int[] ints = new int[4];

        in.readStringArray(strings);
        mCollectionName = strings[0];
        mCoinType = strings[1];

        in.readIntArray(ints);
        mTotalCoinsInCollection = ints[0];
        mTotalCoinsCollected = ints[1];
        mCoinReverseImageIdentifier = ints[2];
        mCoinObverseImageIdentifier = ints[3];
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
            mCoinType,
        };

        int[] ints = new int[] {
            mTotalCoinsInCollection,
            mTotalCoinsCollected,
            mCoinReverseImageIdentifier,
            mCoinObverseImageIdentifier,
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
    
    public String getType(){
    	return mCoinType;
    }
    
    public void setType(String type){
    	mCoinType = type;
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

    public int getCoinReverseImageIdentifier() {
        return mCoinReverseImageIdentifier;
    }

    public void setCoinReverseImageIdentifier(int identifier) {
        mCoinReverseImageIdentifier = identifier;
    }

    public int getCoinObverseImageIdentifier() {
        return mCoinObverseImageIdentifier;
    }

    public void setCoinObverseImageIdentifier(int identifier){
        mCoinObverseImageIdentifier = identifier;
    }
}
