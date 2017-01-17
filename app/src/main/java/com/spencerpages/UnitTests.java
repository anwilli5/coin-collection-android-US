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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

/**
 * Basic unit test suite - mainly just a way to sanity check that when adding new coins (for
 * instance, as the years progress and near coins are minted), we add in all the ones that we need
 * to.
 */
class UnitTests {
	
	private Context mContext = null;
	
	public boolean runTests(Context context) {

		mContext = context;

        // TODO Replace with better testing!
		
		boolean result = testAllMintMarks();
		if(!result){
		    return false;
		}
		
		result = testPMintMarks();
		if(!result){
		    return false;
		}
		
		result = testDMintMarks();
		if(!result){
		    return false;
		}
		
		result = testSMintMarks();
		if(!result){
		    return false;
		}
		
		result = testOMintMarks();
		if(!result){
		    return false;
		}
		
		result = testCCMintMarks();
		if(!result){
		    return false;
		}

		// All tests passed
		return true;
	}

	// TODO Replace with better tests
	private boolean testAllMintMarks() {
		
		CoinPageCreator creator = new CoinPageCreator();
        creator.testSetContext(mContext);

		boolean showTerritories = true;
		boolean showMintMark = true;
		boolean showP = true;
		boolean showD = true;
		boolean showS = true;
		boolean showCC = true;
		boolean showO = true;
        boolean editDateRange = false;
        boolean showBurnished = false;

		String[] coinTypes = mContext.getResources().getStringArray(R.array.types_of_coins);
		
		int[] startYears = mContext.getResources().getIntArray(R.array.year_of_first_production);
		int[] stopYears = mContext.getResources().getIntArray(R.array.year_of_most_recent_production);
		
		HashMap<String,Integer> info = new HashMap<>();
		info.put("Pennies", 269); // Typically increases by 2
		info.put("Nickels", 177); // Typically increases by 2
		info.put("Dimes", 151); // Typically increases by 2
		info.put("Quarters", 146);
		info.put("State Quarters", 112);
		info.put("National Park Quarters", 80); // Typically increases by 10
		info.put("Half-Dollars", 100); // Typically increases by 2
		info.put("Eisenhower Dollars", 14);
		info.put("Susan B. Anthony Dollars", 11);
		info.put("Sacagawea/Native American Dollars", 36); // Typically increases by 2
		info.put("Presidential Dollars", 78); // Typically increases by 4
		info.put("Indian Head Cents", 55);
		info.put("Liberty Head Nickels", 33);
		info.put("Buffalo Nickels", 64);
		info.put("Mercury Dimes", 77);
		info.put("Barber Dimes", 74);
		info.put("Barber Quarters", 74);
		info.put("Standing Liberty Quarters", 37);
		info.put("Barber Half Dollars", 73);
		info.put("Walking Liberty Half Dollars", 65);
		info.put("Franklin Half Dollars", 35);
		info.put("Morgan Dollars", 96);
		info.put("Peace Dollars", 24);
		info.put("American Eagle Silver Dollars", 32); // Typically increases by 1
		info.put("First Spouse Gold Coins", 41); // Typically increases by 4
		
		Object[] keys = info.keySet().toArray();
		
        for(int i = 0; i < info.size(); i++){
        	
        	String coinType = (String) keys[i];
        	Integer size = info.get(coinType);
        	
        	int j = 0;
        	for( ; j < coinTypes.length; j++){
        		
        		String coinName = coinTypes[j];
        		if(coinName.equals(coinType)){
        			break;
        		}
        	}

            int startYear = startYears[j];
            int stopYear = stopYears[j];

			creator.testSetInternalState(showMintMark,editDateRange,showTerritories,showBurnished,
            showP, showD, showS, showO, showCC, startYear, stopYear);
        
			creator.makeTable(coinType);

            ArrayList<String> identifierList = creator.testGetIdentifierList();
            ArrayList<String> mintList = creator.testGetMintList();

            Integer size1 = identifierList.size();
			Integer size2 = mintList.size();
			
			if( !size1.equals(size) ||
		        !size2.equals(size)    ){
				
				for(int k = 0; k < size1; k++){
					Log.e(MainApplication.APP_NAME, identifierList.get(k) + " " + mintList.get(k));
				}
				
				Log.e(MainApplication.APP_NAME, "Failed sanity check - " + coinType + " - (" + String.valueOf(size1) + " : " + String.valueOf(size2) + ") != " + String.valueOf(size));
				return false;
				
			} else {
                creator.testClearLists();
			}
		}
		
		return true;
	}

    private boolean testPMintMarks() {
		
		CoinPageCreator creator = new CoinPageCreator();
        creator.testSetContext(mContext);

        boolean showTerritories = true;
        boolean showMintMark = true;
        boolean showP = true;
        boolean showD = false;
        boolean showS = false;
        boolean showCC = false;
        boolean showO = false;
        boolean editDateRange = false;
        boolean showBurnished = false;

		String[] coinTypes = mContext.getResources().getStringArray(R.array.types_of_coins);
		
		int[] startYears = mContext.getResources().getIntArray(R.array.year_of_first_production);
		int[] stopYears = mContext.getResources().getIntArray(R.array.year_of_most_recent_production);
		
		HashMap<String,Integer> info = new HashMap<>();
		// TODO Do these
		//info.put("Pennies", 261);
		//info.put("Nickels", 229);
		//info.put("Dimes", 143);
		//info.put("Quarters", 146);
		//info.put("State Quarters", 112);
		//info.put("National Park Quarters", 40);
		//info.put("Half-Dollars", 92);
		//info.put("Eisenhower Dollars", 14);
		//info.put("Susan B. Anthony Dollars", 11);
		//info.put("Sacagawea Dollars", 28);
		//info.put("Presidential Dollars", 56);
		//info.put("Indian Head Cents", 55);
		info.put("Liberty Head Nickels", 31);
		//info.put("Buffalo Nickels", 64);
		//info.put("Mercury Dimes", 77);
		//info.put("Barber Dimes", 74);
		info.put("Barber Quarters", 25);
		info.put("Standing Liberty Quarters", 15);
		info.put("Barber Half Dollars", 24);
		info.put("Walking Liberty Half Dollars", 20);
		info.put("Franklin Half Dollars", 16);
		info.put("Morgan Dollars", 28);
		info.put("Peace Dollars", 10);
		//info.put("First Spouse Gold Coins", 25);
		
		Object[] keys = info.keySet().toArray();
		
        for(int i = 0; i < info.size(); i++){
        	
        	String coinType = (String) keys[i];
        	Integer size = info.get(coinType);
        	
        	int j = 0;
        	for( ; j < coinTypes.length; j++){
        		
        		String coinName = coinTypes[j];
        		if(coinName.equals(coinType)){
        			break;
        		}
        	}

            int startYear = startYears[j];
            int stopYear = stopYears[j];

            creator.testSetInternalState(showMintMark,editDateRange,showTerritories,showBurnished,
                    showP, showD, showS, showO, showCC, startYear, stopYear);

            creator.makeTable(coinType);

            ArrayList<String> identifierList = creator.testGetIdentifierList();
            ArrayList<String> mintList = creator.testGetMintList();

            Integer size1 = identifierList.size();
            Integer size2 = mintList.size();
			
			if( !size1.equals(size) ||
		        !size2.equals(size)    ){
				
				for(int k = 0; k < size1; k++){
					Log.e(MainApplication.APP_NAME, identifierList.get(k) + " " + mintList.get(k));
				}
				
				Log.e(MainApplication.APP_NAME, "Failed sanity check - " + coinType + " - (" + String.valueOf(size1) + " : " + String.valueOf(size2) + ") != " + String.valueOf(size));
				return false;
				
			} else {
                creator.testClearLists();
			}
		}
		
		return true;
	}
    
    private boolean testDMintMarks() {
		
		CoinPageCreator creator = new CoinPageCreator();
        creator.testSetContext(mContext);

        boolean showTerritories = true;
        boolean showMintMark = true;
        boolean showP = false;
        boolean showD = true;
        boolean showS = false;
        boolean showCC = false;
        boolean showO = false;
        boolean editDateRange = false;
        boolean showBurnished = false;

		String[] coinTypes = mContext.getResources().getStringArray(R.array.types_of_coins);
		
		int[] startYears = mContext.getResources().getIntArray(R.array.year_of_first_production);
		int[] stopYears = mContext.getResources().getIntArray(R.array.year_of_most_recent_production);
		
		HashMap<String,Integer> info = new HashMap<>();
		// TODO Do these
		//info.put("Pennies", 261);
		//info.put("Nickels", 229);
		//info.put("Dimes", 143);
		//info.put("Quarters", 146);
		//info.put("State Quarters", 112);
		//info.put("National Park Quarters", 40);
		//info.put("Half-Dollars", 92);
		//info.put("Eisenhower Dollars", 14);
		//info.put("Susan B. Anthony Dollars", 11);
		//info.put("Sacagawea Dollars", 28);
		//info.put("Presidential Dollars", 56);
		//info.put("Indian Head Cents", 55);
		info.put("Liberty Head Nickels", 1);
		//info.put("Buffalo Nickels", 64);
		//info.put("Mercury Dimes", 77);
		//info.put("Barber Dimes", 74);
		info.put("Barber Quarters", 10);
		info.put("Standing Liberty Quarters", 10);
		info.put("Barber Half Dollars", 7);
		info.put("Walking Liberty Half Dollars", 21);
		info.put("Franklin Half Dollars", 14);
		info.put("Morgan Dollars", 1);
		info.put("Peace Dollars", 5);
		//info.put("First Spouse Gold Coins", 25);
		
		Object[] keys = info.keySet().toArray();
		
        for(int i = 0; i < info.size(); i++){
        	
        	String coinType = (String) keys[i];
        	Integer size = info.get(coinType);
        	
        	int j = 0;
        	for( ; j < coinTypes.length; j++){
        		
        		String coinName = coinTypes[j];
        		if(coinName.equals(coinType)){
        			break;
        		}
        	}

            int startYear = startYears[j];
            int stopYear = stopYears[j];

            creator.testSetInternalState(showMintMark,editDateRange,showTerritories,showBurnished,
                    showP, showD, showS, showO, showCC, startYear, stopYear);

            creator.makeTable(coinType);

            ArrayList<String> identifierList = creator.testGetIdentifierList();
            ArrayList<String> mintList = creator.testGetMintList();

            Integer size1 = identifierList.size();
            Integer size2 = mintList.size();
			
			if( !size1.equals(size) ||
		        !size2.equals(size)    ){
				
				for(int k = 0; k < size1; k++){
					Log.e(MainApplication.APP_NAME, identifierList.get(k) + " " + mintList.get(k));
				}
				
				Log.e(MainApplication.APP_NAME, "Failed sanity check - " + coinType + " - (" + String.valueOf(size1) + " : " + String.valueOf(size2) + ") != " + String.valueOf(size));
				return false;
				
			} else {
				creator.testClearLists();
			}
		}
		
		return true;
	}
    
    private boolean testSMintMarks() {
		
		CoinPageCreator creator = new CoinPageCreator();
        creator.testSetContext(mContext);

        boolean showTerritories = true;
        boolean showMintMark = true;
        boolean showP = false;
        boolean showD = false;
        boolean showS = true;
        boolean showCC = false;
        boolean showO = false;
        boolean editDateRange = false;
        boolean showBurnished = false;

		String[] coinTypes = mContext.getResources().getStringArray(R.array.types_of_coins);
		
		int[] startYears = mContext.getResources().getIntArray(R.array.year_of_first_production);
		int[] stopYears = mContext.getResources().getIntArray(R.array.year_of_most_recent_production);
		
		HashMap<String,Integer> info = new HashMap<>();
		// TODO Do these
		//info.put("Pennies", 261);
		//info.put("Nickels", 229);
		//info.put("Dimes", 143);
		//info.put("Quarters", 146);
		//info.put("State Quarters", 112);
		//info.put("National Park Quarters", 40);
		//info.put("Half-Dollars", 92);
		//info.put("Eisenhower Dollars", 14);
		//info.put("Susan B. Anthony Dollars", 11);
		//info.put("Sacagawea Dollars", 28);
		//info.put("Presidential Dollars", 56);
		//info.put("Indian Head Cents", 55);
		info.put("Liberty Head Nickels", 1);
		//info.put("Buffalo Nickels", 64);
		//info.put("Mercury Dimes", 77);
		info.put("Barber Dimes", 24);
		info.put("Barber Quarters", 21);
		info.put("Standing Liberty Quarters", 12);
		info.put("Barber Half Dollars", 24);
		info.put("Walking Liberty Half Dollars", 24);
		info.put("Franklin Half Dollars", 5);
		info.put("Morgan Dollars", 28);
		info.put("Peace Dollars", 9);
		//info.put("First Spouse Gold Coins", 25);
		
		Object[] keys = info.keySet().toArray();
		
        for(int i = 0; i < info.size(); i++){
        	
        	String coinType = (String) keys[i];
        	Integer size = info.get(coinType);
        	
        	int j = 0;
        	for( ; j < coinTypes.length; j++){
        		
        		String coinName = coinTypes[j];
        		if(coinName.equals(coinType)){
        			break;
        		}
        	}

            int startYear = startYears[j];
            int stopYear = stopYears[j];

            creator.testSetInternalState(showMintMark,editDateRange,showTerritories,showBurnished,
                    showP, showD, showS, showO, showCC, startYear, stopYear);

            creator.makeTable(coinType);

            ArrayList<String> identifierList = creator.testGetIdentifierList();
            ArrayList<String> mintList = creator.testGetMintList();

            Integer size1 = identifierList.size();
            Integer size2 = mintList.size();
			
			if( !size1.equals(size) ||
		        !size2.equals(size)    ){
				
				for(int k = 0; k < size1; k++){
					Log.e(MainApplication.APP_NAME, identifierList.get(k) + " " + mintList.get(k));
				}
				
				Log.e(MainApplication.APP_NAME, "Failed sanity check - " + coinType + " - (" + String.valueOf(size1) + " : " + String.valueOf(size2) + ") != " + String.valueOf(size));
				return false;
				
			} else {
                creator.testClearLists();
			}
		}
		
		return true;
	}
    
    private boolean testOMintMarks() {
		
		CoinPageCreator creator = new CoinPageCreator();
        creator.testSetContext(mContext);

        boolean showTerritories = true;
        boolean showMintMark = true;
        boolean showP = false;
        boolean showD = false;
        boolean showS = false;
        boolean showCC = false;
        boolean showO = true;
        boolean editDateRange = false;
        boolean showBurnished = false;

		String[] coinTypes = mContext.getResources().getStringArray(R.array.types_of_coins);
		
		int[] startYears = mContext.getResources().getIntArray(R.array.year_of_first_production);
		int[] stopYears = mContext.getResources().getIntArray(R.array.year_of_most_recent_production);
		
		HashMap<String,Integer> info = new HashMap<>();

		info.put("Barber Dimes", 17);
		info.put("Barber Quarters", 18);
		info.put("Barber Half Dollars", 18);
		info.put("Morgan Dollars", 26);
		
		Object[] keys = info.keySet().toArray();
		
        for(int i = 0; i < info.size(); i++){
        	
        	String coinType = (String) keys[i];
        	Integer size = info.get(coinType);
        	
        	int j = 0;
        	for( ; j < coinTypes.length; j++){
        		
        		String coinName = coinTypes[j];
        		if(coinName.equals(coinType)){
        			break;
        		}
        	}

            int startYear = startYears[j];
            int stopYear = stopYears[j];

            creator.testSetInternalState(showMintMark,editDateRange,showTerritories,showBurnished,
                    showP, showD, showS, showO, showCC, startYear, stopYear);

            creator.makeTable(coinType);

            ArrayList<String> identifierList = creator.testGetIdentifierList();
            ArrayList<String> mintList = creator.testGetMintList();

            Integer size1 = identifierList.size();
            Integer size2 = mintList.size();
			
			if( !size1.equals(size) ||
		        !size2.equals(size)    ){
				
				for(int k = 0; k < size1; k++){
					Log.e(MainApplication.APP_NAME, identifierList.get(k) + " " + mintList.get(k));
				}
				
				Log.e(MainApplication.APP_NAME, "Failed sanity check - " + coinType + " - (" + String.valueOf(size1) + " : " + String.valueOf(size2) + ") != " + String.valueOf(size));
				return false;
				
			} else {
                creator.testClearLists();
			}
		}
		
		return true;
	}
    
    private boolean testCCMintMarks() {
		
		CoinPageCreator creator = new CoinPageCreator();
        creator.testSetContext(mContext);

        boolean showTerritories = true;
        boolean showMintMark = true;
        boolean showP = false;
        boolean showD = false;
        boolean showS = false;
        boolean showCC = true;
        boolean showO = false;
        boolean editDateRange = false;
        boolean showBurnished = false;

		String[] coinTypes = mContext.getResources().getStringArray(R.array.types_of_coins);
		
		int[] startYears = mContext.getResources().getIntArray(R.array.year_of_first_production);
		int[] stopYears = mContext.getResources().getIntArray(R.array.year_of_most_recent_production);
		
		HashMap<String,Integer> info = new HashMap<>();

		info.put("Morgan Dollars", 13);
		
		Object[] keys = info.keySet().toArray();
		
        for(int i = 0; i < info.size(); i++){
        	
        	String coinType = (String) keys[i];
        	Integer size = info.get(coinType);
        	
        	int j = 0;
        	for( ; j < coinTypes.length; j++){
        		
        		String coinName = coinTypes[j];
        		if(coinName.equals(coinType)){
        			break;
        		}
        	}

            int startYear = startYears[j];
            int stopYear = stopYears[j];

            creator.testSetInternalState(showMintMark,editDateRange,showTerritories,showBurnished,
                    showP, showD, showS, showO, showCC, startYear, stopYear);

            creator.makeTable(coinType);

            ArrayList<String> identifierList = creator.testGetIdentifierList();
            ArrayList<String> mintList = creator.testGetMintList();

            Integer size1 = identifierList.size();
            Integer size2 = mintList.size();
			
			if( !size1.equals(size) ||
		        !size2.equals(size)    ){
				
				for(int k = 0; k < size1; k++){
					Log.e(MainApplication.APP_NAME, identifierList.get(k) + " " + mintList.get(k));
				}
				
				Log.e(MainApplication.APP_NAME, "Failed sanity check - " + coinType + " - (" + String.valueOf(size1) + " : " + String.valueOf(size2) + ") != " + String.valueOf(size));
				return false;
				
			} else {
                creator.testClearLists();
			}
		}
		
		return true;
	}
}
