/*
 * Copyright (C) 2008 Google Inc.
 * Modified by Andrew Williams
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.spencerpages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

/**
 * Adapter based on the Simple Notes Database Access Helper Class on the Android site.
 * 
 * This Adapter is used to get information that the user has entered regarding his or her coin
 * collections (from the backing database.)
 */
public class DatabaseAdapter {

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "CoinCollection";

    /** DATABASE_VERSION Tracks the current database version, and is essential for periodic
     *                   database updating.  It should be raised anytime we need to insert new
     *                   coins into a user's collections (Ex: yearly coin addition, bug fixes).
     *
     *                   Version 2 - Used in Versions 1 and 1.1 of the app
     *                   Version 3 - Used in Version 1.2 and 1.3 of the app
     *                   Version 4 - Used in Version 1.4, 1.4.1, and 1.5 of the app
     *                   Version 5 - Used in Version 1.6 of the app
     *                   Version 6 - Used in Version 2.0, 2.0.1 of the app
     *                   Version 7 - Used in Version 2.1, 2.1.1 of the app
     *                   Version 8 - Used in Version 2.2.1 of the app
	 *                   Version 9 - Used in Version 2.3 of the app

	 */
    public static final int DATABASE_VERSION = 9;

    private final Context mContext;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// This is called if the DB doesn't exist (A fresh installation)
			createCollectionInfoTable(db);
		}
		
		public void createCollectionInfoTable(SQLiteDatabase db){

            // v2.2.1 - Until this version all fields had '_id' created with 'autoincrement'
            // which is unnecessary for our purposes.  Removing to improve performance.
			String makeCollectionInfoTable = "CREATE TABLE collection_info (_id integer primary key,"
					+ " name text not null,"
					+ " coinType text not null,"
					+ " total integer,"
					+ " display integer default " + Integer.toString(MainApplication.SIMPLE_DISPLAY) + ","
				    + " displayOrder integer"
                    + ");";
				
				db.execSQL(makeCollectionInfoTable);
		}

		// We could implement this now that our targeted API level is >= 11, but not necessary
		//@Override
		//public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(MainApplication.APP_NAME, "Upgrading database from version " + oldVersion + " to "
			        + newVersion);

			// NOTE There is no good way to add values to the table other than adding them to the
            // end. Doing otherwise would likely be intensive. For now it doesn't seem to be an
            // issue.

            // TODO Make this cleaner by moving each update code into its own function.

			if(oldVersion == 2){

				// Get all of the created tables
				Cursor resultCursor = db.query("collection_info", new String[] {"name", "coinType",
				"total"}, null, null, null, null, "_id");
				// THanks! http://stackoverflow.com/questions/2810615/how-to-retrieve-data-from-cursor-class
				if (resultCursor.moveToFirst()){
					do{

						String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
						String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
						int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));
						
						int originalTotal = total;

						// Need to fix many bugs
						switch (coinType) {
							case "Pennies": {

								// Remove 1921 D Penny
								int value = db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1921", " D"});

								// TODO What should we do?
								// We can't add the new identifiers, just delete the old ones
								//value += db.delete("[" + name + "]", "coinIdentifier=?", new String[] { "2009" });

								total = total - value;

								break;
							}
							case "Nickels": {
								// Remove 1955s nickel
								int value = db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1955", " S"});
								// Remove 1965-1967 D Nickel
								value += db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1965", " D"});
								value += db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1966", " D"});
								value += db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1967", " D"});

								// We can't add the new identifiers, just delete the old ones
								// TODO What should we do
								//value += db.delete("[" + name + "]", "coinIdentifier=?", new String[] { "2004" });
								//value += db.delete("[" + name + "]", "coinIdentifier=?", new String[] { "2005" });

								total = total - value;

								break;
							}
							case "Quarters": {
								// Remove 1965 - 1967 D quarters
								int value = db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1965", " D"});
								value += db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1966", " D"});
								value += db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"1967", " D"});
								total = total - value;

								break;
							}
							case "National Park Quarters": {
								// Add in 2012 National Park Quarters

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("El Yunque");
								newCoinIdentifiers.add("Chaco Culture");
								newCoinIdentifiers.add("Acadia");
								newCoinIdentifiers.add("Hawaii Volcanoes");
								newCoinIdentifiers.add("Denali");

								// Add these coins, mimicing which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);

								break;
							}
							case "Half-Dollars":
								// Need to add in 1968 - 1970 for Half Dollars unless it doesn't exist
								// NOTE - We can't fix this, because adding will mess up the _id fields, which we use to do ordering
								// We could make a new table and copy over all of the data, but that presents a lot of challenges

								break;
							case "Eisenhower Dollars": {
								// Take out Eisenhower dollars > 1978
								int totalRemoved = 0;
								for (int i = 1979; i <= 2012; i++) {
									int value = db.delete("[" + name + "]", "coinIdentifier=?", new String[]{String.valueOf(i)});
									totalRemoved += value;
								}

								// Take out Eisenhower dollars with S marks
								int value = db.delete("[" + name + "]", "coinMint=?", new String[]{" S"});
								totalRemoved += value;

								total = total - totalRemoved;

								break;
							}
							case "Susan B. Anthony Dollars": {
								// Remove 1982 Susan B Anthony's
								int value = db.delete("[" + name + "]", "coinIdentifier=?", new String[]{"1982"});
								total = total - value;

								break;
							}
							case "Presidential Dollars": {
								// Add in 2012 Presidential Dollars

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("Chester Arthur");
								newCoinIdentifiers.add("Grover Cleveland 1");
								newCoinIdentifiers.add("Benjamin Harrison");
								newCoinIdentifiers.add("Grover Cleveland 2");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);
								break;
							}
						}
						
						// For each collection, if there were changes then update the total
						if(originalTotal != total){
							// Update the total
							ContentValues values = new ContentValues();
							values.put("total", total);
							db.update("collection_info", values, "name=? AND coinType=?", new String[] { name, coinType });
						}
					}while(resultCursor.moveToNext());
				}
				resultCursor.close();
			}
			
			// Updates in version 1.4
            if(oldVersion >= 2 && oldVersion <= 3){

				// We need to add in columns to each table associated with a collection
				// Get all of the created tables
				Cursor resultCursor = db.query("collection_info", new String[] {"name", "coinType",
				"total"}, null, null, null, null, "_id");
				if (resultCursor.moveToFirst()){
					do{
						
						String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
						String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
						int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));
						
						// If we add coins, keep track so we can update the total only if necessary
						int originalTotal = total;

						switch (coinType) {
							case "Pennies":

								// 1. Bug fix: The bicentennials should not display mint mark " P"
								ContentValues values = new ContentValues();
								values.put("coinMint", "");
								// This shortcut works because pennies never carried the " P" mint mark
								db.update("[" + name + "]", values, "coinMint=?", new String[]{" P"});

								// 3. 1909 V.D.B. - Can't do anything since it is in the middle of the collection
								break;
							case "Presidential Dollars": {
								// Add in 2013 Presidential Dollars

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("William McKinley");
								newCoinIdentifiers.add("Theodore Roosevelt");
								newCoinIdentifiers.add("William Howard Taft");
								newCoinIdentifiers.add("Woodrow Wilson");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);
								break;
							}
							case "National Park Quarters": {
								// Add in 2013 National Park Quarters

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("White Mountain");
								newCoinIdentifiers.add("Perry’s Victory");
								newCoinIdentifiers.add("Great Basin");
								newCoinIdentifiers.add("Fort McHenry");
								newCoinIdentifiers.add("Mount Rushmore");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);
								break;
							}
							case "First Spouse Gold Coins": {
								// Add in 2012 First Spouse Gold Coins

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("Alice Paul");
								newCoinIdentifiers.add("Frances Cleveland 1");
								newCoinIdentifiers.add("Caroline Harrison");
								newCoinIdentifiers.add("Frances Cleveland 2");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);

								break;
							}
							case "Susan B. Anthony Dollars":

								break;
						}
						
						
						// For each collection, add in 2013 coins if necessary
						
						if(coinType.equals("Pennies")           || coinType.equals("Nickels")      || 
						   coinType.equals("Dimes")             || coinType.equals("Half-Dollars") ||
						   coinType.equals("Sacagawea Dollars")){
						
							// First, we need to determine whether we should add 2013 to this album
							Cursor lastYearCursor = db.query("[" + name + "]", new String[] {"coinIdentifier"}, null, null, null, null, "_id DESC", "1");
							boolean shouldAdd2013 = false;
							if(lastYearCursor.moveToFirst()){
								String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex("coinIdentifier"));
								if(lastYear.equals("2012")){
								    // If the collection included 2012 coins, it's likely they want 2013 in there too
									// Also, in earlier versions it was possible to make a collection end after 2012...
									// If they already have 2013 in their collection, don't add
									shouldAdd2013 = true;
								}
							}
							lastYearCursor.close();
							
							if(shouldAdd2013){
								// For each collection, we need to know which mint marks are present
								// Get the distinct columns from the coinMint field
								Cursor coinMintCursor = db.query(true, "[" + name + "]", new String[] {"coinMint"}, null, null, null, null, "_id", null);
								
							    // We need to determine which mint marks to add
								boolean hasAddedP = false;
							    if(coinMintCursor.moveToFirst()){
							    	do{
								        String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));
								        
							        	ContentValues new2013CoinValues = new ContentValues();
										switch (coinMint) {
											case "":
												if (!hasAddedP) {
													new2013CoinValues.put("coinIdentifier", "2013");
													new2013CoinValues.put("coinMint", "");
													// Fall through to insert coin info
												} else {
													// Do nothing... This collection has mint marks displayed, so the only
													// values we want to add are " P" and " D"
													continue;
												}
												break;
											case " P":
												new2013CoinValues.put("coinIdentifier", "2013");
												new2013CoinValues.put("coinMint", " P");
												hasAddedP = true;
												// Fall through to insert coin info

												// We may have mistakenly added the "" mint mark, since earlier
												// Philly minted coins didn't have the mint mark.  In this case,
												// delete it
												if (1 == db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"2013", ""})) {
													total--;
												}
												break;
											case " D":
												new2013CoinValues.put("coinIdentifier", "2013");
												new2013CoinValues.put("coinMint", " D");
												// Fall through to insert coin info
												break;
											default:

												// Don't want to add in any " S"s
												continue;
										}
								        
							        	if(db.insert("[" + name + "]", null, new2013CoinValues) != -1){
							        	    total++;
							        	}
								        
							    	}while(coinMintCursor.moveToNext());
							    }
							    
								// All done with the coin mint information
								coinMintCursor.close();
							}
						}
						
						// Last thing is to update the total for each collection
						if(originalTotal != total){
						    ContentValues values = new ContentValues();
						    values.put("total", total);
						    db.update("collection_info", values, "name=? AND coinType=?", new String[] { name, coinType });
						}
						
						// Move to the next collection
					} while(resultCursor.moveToNext());
			    }
			    resultCursor.close();
			}
			
			// Updates in version 1.6
            if(oldVersion >= 2 && oldVersion <= 4){

				// Get all of the created tables
				Cursor resultCursor = db.query("collection_info", new String[] {"name", "coinType",
				"total"}, null, null, null, null, "_id");
				if (resultCursor.moveToFirst()){
					do{
						
						String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
						String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
						int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));
						
						// If we add coins, keep track so we can update the total only if necessary
						int originalTotal = total;

						switch (coinType) {
							case "Pennies":

								break;
							case "Presidential Dollars": {
								// Add in 2014 Presidential Dollars

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("Warren G. Harding");
								newCoinIdentifiers.add("Calvin Coolidge");
								newCoinIdentifiers.add("Herbert Hoover");
								newCoinIdentifiers.add("Franklin D. Roosevelt");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);
								break;
							}
							case "National Park Quarters": {
								// Add in 2014 National Park Quarters

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("Great Smoky Mountains");
								newCoinIdentifiers.add("Shenandoah");
								newCoinIdentifiers.add("Arches");
								newCoinIdentifiers.add("Great Sand Dunes");
								newCoinIdentifiers.add("Everglades");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);
								break;
							}
							case "First Spouse Gold Coins": {
								// Add in 2013 First Spouse Gold Coins

								ArrayList<String> newCoinIdentifiers = new ArrayList<>();
								newCoinIdentifiers.add("Ida McKinley");
								newCoinIdentifiers.add("Edith Roosevelt");
								newCoinIdentifiers.add("Helen Taft");
								newCoinIdentifiers.add("Ellen Wilson");
								newCoinIdentifiers.add("Edith Wilson");

								// Add these coins, mimicking which coinMints the user already has defined
								total += addFromArrayList(db, name, newCoinIdentifiers);

								break;
							}
						}
						
						// For each collection, add in 2013 coins if necessary
						
						if(coinType.equals("Pennies")           || coinType.equals("Nickels")           || 
						   coinType.equals("Dimes")             || coinType.equals("Half-Dollars")      ||
						   coinType.equals("Sacagawea Dollars") || coinType.equals("Sacagawea/Native American Dollars") ||
						   coinType.equals("American Eagle Silver Dollars")){
						
							// First, we need to determine whether we should add 2014 to this album
							Cursor lastYearCursor = db.query("[" + name + "]", new String[] {"coinIdentifier"}, null, null, null, null, "_id DESC", "1");
							boolean shouldAdd2014 = false;
							if(lastYearCursor.moveToFirst()){
								String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex("coinIdentifier"));
								if(lastYear.equals("2013")){
								    // If the collection included 2013 coins, it's likely they want 2014 in there too
									// Also, in earlier versions it was possible to make a collection end after 2012...
									// If they already have 2013 in their collection, don't add
									shouldAdd2014 = true;
								}
							}
							lastYearCursor.close();
							
							if(shouldAdd2014){
								// For each collection, we need to know which mint marks are present
								// Get the distinct columns from the coinMint field
								Cursor coinMintCursor = db.query(true, "[" + name + "]", new String[] {"coinMint"}, null, null, null, null, "_id", null);
								
							    // We need to determine which mint marks to add
								boolean hasAddedP = false;
							    if(coinMintCursor.moveToFirst()){
							    	do{
								        String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));
								        
							        	ContentValues new2014CoinValues = new ContentValues();
										switch (coinMint) {
											case "":
												if (!hasAddedP) {
													new2014CoinValues.put("coinIdentifier", "2014");
													new2014CoinValues.put("coinMint", "");
													// Fall through to insert coin info
												} else {
													// Do nothing... This collection has mint marks displayed, so the only
													// values we want to add are " P" and " D"
													continue;
												}
												break;
											case " P":
												new2014CoinValues.put("coinIdentifier", "2014");
												new2014CoinValues.put("coinMint", " P");
												hasAddedP = true;
												// Fall through to insert coin info

												// We may have mistakenly added the "" mint mark, since earlier
												// Philly minted coins didn't have the mint mark.  In this case,
												// delete it
												if (1 == db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"2014", ""})) {
													total--;
												}
												break;
											case " D":
												new2014CoinValues.put("coinIdentifier", "2014");
												new2014CoinValues.put("coinMint", " D");
												// Fall through to insert coin info
												break;
											default:

												// Don't want to add in any " S"s
												continue;
										}
								        
							        	if(db.insert("[" + name + "]", null, new2014CoinValues) != -1){
							        	    total++;
							        	}
								        
							    	}while(coinMintCursor.moveToNext());
							    }
							    
								// All done with the coin mint information
								coinMintCursor.close();
							}
						}
						
						// Last thing is to update the total for each collection
						if(originalTotal != total){
						    ContentValues values = new ContentValues();
						    values.put("total", total);
						    db.update("collection_info", values, "name=? AND coinType=?", new String[] { name, coinType });
						}
						
						// Move to the next collection
					} while(resultCursor.moveToNext());
			    }
			    resultCursor.close();
				
			}
			
			// Updates in version 2.0
            if(oldVersion >= 2 && oldVersion <= 5){
								
				// We need to add in columns to support the new advanced view
				db.execSQL("ALTER TABLE collection_info ADD COLUMN display INTEGER DEFAULT " + Integer.toString(MainApplication.SIMPLE_DISPLAY));

				// Get all of the created tables
				Cursor resultCursor = db.query("collection_info", new String[] {"name"}, null, null, null, null, "_id");
				if (resultCursor.moveToFirst()){
					do{
							
						String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
							
						db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN advGradeIndex INTEGER DEFAULT 0");
						db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN advQuantityIndex INTEGER DEFAULT 0");
						db.execSQL("ALTER TABLE [" + name + "] ADD COLUMN advNotes TEXT DEFAULT \"\"");

						// Move to the next collection
					} while(resultCursor.moveToNext());
				}
				resultCursor.close();
			}

            // Updates in version 2.1
            if(oldVersion >= 2 && oldVersion <= 6){
                updateToVersion7(db);
            }

            // Updates in version 2.2.1
            if(oldVersion >= 2 && oldVersion <= 7){
                updateToVersion8(db);
            }

            // Updates in version 2.3
            if(oldVersion >= 2 && oldVersion <= 8){
                updateToVersion9(db);
            }

        }

        public void updateToVersion7(SQLiteDatabase db){

            // TODO Update copy and pasted code
            // Get all of the created tables
            Cursor resultCursor = db.query("collection_info", new String[] {"name", "coinType",
                    "total"}, null, null, null, null, "_id");
            if (resultCursor.moveToFirst()){
                do{

                    String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                    String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
                    int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));

                    // If we add coins, keep track so we can update the total only if necessary
                    int originalTotal = total;

					switch (coinType) {
						case "Presidential Dollars": {
							// Add in 2015 Presidential Dollars

							ArrayList<String> newCoinIdentifiers = new ArrayList<>();
							newCoinIdentifiers.add("Harry Truman");
							newCoinIdentifiers.add("Dwight D. Eisenhower");
							newCoinIdentifiers.add("John F. Kennedy");
							newCoinIdentifiers.add("Lyndon B. Johnson");

							// Add these coins, mimicking which coinMints the user already has defined
							total += addFromArrayList(db, name, newCoinIdentifiers);
							break;
						}
						case "National Park Quarters": {
							// Add in 2015 National Park Quarters

							ArrayList<String> newCoinIdentifiers = new ArrayList<>();
							newCoinIdentifiers.add("Homestead");
							newCoinIdentifiers.add("Kisatchie");
							newCoinIdentifiers.add("Blue Ridge");
							newCoinIdentifiers.add("Bombay Hook");
							newCoinIdentifiers.add("Saratoga");

							// Add these coins, mimicking which coinMints the user already has defined
							total += addFromArrayList(db, name, newCoinIdentifiers);
							break;
						}
						case "First Spouse Gold Coins": {
							// Add in 2014 First Spouse Gold Coins

							ArrayList<String> newCoinIdentifiers = new ArrayList<>();
							newCoinIdentifiers.add("Florence Harding");
							newCoinIdentifiers.add("Grace Coolidge");
							newCoinIdentifiers.add("Lou Hoover");
							newCoinIdentifiers.add("Eleanor Roosevelt");

							// Add these coins, mimicking which coinMints the user already has defined
							total += addFromArrayList(db, name, newCoinIdentifiers);
							break;
						}
					}

                    // For each collection, add in 2015 coins if necessary

                    if(coinType.equals("Pennies")           || coinType.equals("Nickels")           ||
                            coinType.equals("Dimes")             || coinType.equals("Half-Dollars")      ||
                            coinType.equals("Sacagawea Dollars") || coinType.equals("Sacagawea/Native American Dollars") ||
                            coinType.equals("American Eagle Silver Dollars")){

                        // First, we need to determine whether we should add 2015 to this album
                        Cursor lastYearCursor = db.query("[" + name + "]", new String[] {"coinIdentifier"}, null, null, null, null, "_id DESC", "1");
                        boolean shouldAdd2015 = false;
                        if(lastYearCursor.moveToFirst()){
                            String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex("coinIdentifier"));
                            if(lastYear.equals("2014")){
                                // If the collection included 2014 coins, it's likely they want 2015 in there too
                                // Also, in earlier versions it was possible to make a collection end after 2012...
                                // If they already have 2014 in their collection, don't add
                                shouldAdd2015 = true;
                            }
                        }
                        lastYearCursor.close();

                        if(shouldAdd2015){
                            // For each collection, we need to know which mint marks are present
                            // Get the distinct columns from the coinMint field
                            Cursor coinMintCursor = db.query(true, "[" + name + "]", new String[] {"coinMint"}, null, null, null, null, "_id", null);

                            // We need to determine which mint marks to add
                            boolean hasAddedP = false;
                            if(coinMintCursor.moveToFirst()){
                                do{
                                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));

                                    ContentValues new2015CoinValues = new ContentValues();
									switch (coinMint) {
										case "":
											if (!hasAddedP) {
												new2015CoinValues.put("coinIdentifier", "2015");
												new2015CoinValues.put("coinMint", "");
												// Fall through to insert coin info
											} else {
												// Do nothing... This collection has mint marks displayed, so the only
												// values we want to add are " P" and " D"
												continue;
											}
											break;
										case " P":
											new2015CoinValues.put("coinIdentifier", "2015");
											new2015CoinValues.put("coinMint", " P");
											hasAddedP = true;
											// Fall through to insert coin info

											// We may have mistakenly added the "" mint mark, since earlier
											// Philly minted coins didn't have the mint mark.  In this case,
											// delete it
											if (1 == db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"2015", ""})) {
												total--;
											}
											break;
										case " D":
											new2015CoinValues.put("coinIdentifier", "2015");
											new2015CoinValues.put("coinMint", " D");
											// Fall through to insert coin info
											break;
										default:

											// Don't want to add in any " S"s
											continue;
									}

                                    if(db.insert("[" + name + "]", null, new2015CoinValues) != -1){
                                        total++;
                                    }

                                }while(coinMintCursor.moveToNext());
                            }

                            // All done with the coin mint information
                            coinMintCursor.close();
                        }
                    }

                    // Last thing is to update the total for each collection
                    if(originalTotal != total){
                        ContentValues values = new ContentValues();
                        values.put("total", total);
                        db.update("collection_info", values, "name=? AND coinType=?", new String[] { name, coinType });
                    }

                    // Move to the next collection
                } while(resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        public void updateToVersion8(SQLiteDatabase db){

            // Add another column for the display order
            // We have to do this in a try/catch block, because there is one case where the
            // table might already have the column - when importing a backup from a previous
            // version of the app.
            try {
                db.execSQL("ALTER TABLE collection_info ADD COLUMN displayOrder INTEGER");
            } catch(SQLException e) {
                Log.d(MainApplication.APP_NAME, "collection_info already has column displayOrder");
            }

            // TODO Update copy and pasted code
            // Get all of the created tables
            Cursor resultCursor = db.query("collection_info", new String[] {"name", "coinType",
                    "total"}, null, null, null, null, "_id");

            int i = 0;  // Used to set the display order

            if (resultCursor.moveToFirst()){
                do{

                    String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                    String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
                    int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));

                    // If we add coins, keep track so we can update the total only if necessary
                    int originalTotal = total;

					switch (coinType) {
						case "Presidential Dollars": {
							// Add in 2016 Presidential Dollars

							ArrayList<String> newCoinIdentifiers = new ArrayList<>();
							newCoinIdentifiers.add("Richard M. Nixon");
							newCoinIdentifiers.add("Gerald R. Ford");

							// Add these coins, mimicking which coinMints the user already has defined
							total += addFromArrayList(db, name, newCoinIdentifiers);
							break;
						}
						case "National Park Quarters": {
							// Add in 2016 National Park Quarters

							ArrayList<String> newCoinIdentifiers = new ArrayList<>();
							newCoinIdentifiers.add("Shawnee");
							newCoinIdentifiers.add("Cumberland Gap");
							newCoinIdentifiers.add("Harper's Ferry");
							newCoinIdentifiers.add("Theodore Roosevelt");
							newCoinIdentifiers.add("Fort Moultrie");

							// Add these coins, mimicking which coinMints the user already has defined
							total += addFromArrayList(db, name, newCoinIdentifiers);
							break;
						}
						case "First Spouse Gold Coins": {
							// Add in 2015 First Spouse Gold Coins

							ArrayList<String> newCoinIdentifiers = new ArrayList<>();
							newCoinIdentifiers.add("Bess Truman");
							newCoinIdentifiers.add("Mamie Eisenhower");
							newCoinIdentifiers.add("Jacqueline Kennedy");
							newCoinIdentifiers.add("Lady Bird Johnson");

							// Add these coins, mimicking which coinMints the user already has defined
							total += addFromArrayList(db, name, newCoinIdentifiers);
							break;
						}
					}

                    // For each collection, add in 2016 coins if necessary

                    if(coinType.equals("Pennies")           || coinType.equals("Nickels")           ||
                            coinType.equals("Dimes")             || coinType.equals("Half-Dollars")      ||
                            coinType.equals("Sacagawea Dollars") || coinType.equals("Sacagawea/Native American Dollars") ||
                            coinType.equals("American Eagle Silver Dollars")){

                        // First, we need to determine whether we should add 2016 to this album
                        Cursor lastYearCursor = db.query("[" + name + "]", new String[] {"coinIdentifier"}, null, null, null, null, "_id DESC", "1");
                        boolean shouldAdd2016 = false;
                        if(lastYearCursor.moveToFirst()){
                            String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex("coinIdentifier"));
                            if(lastYear.equals("2015")){
                                // If the collection included 2015 coins, it's likely they want 2016 in there too
                                // Also, in earlier versions it was possible to make a collection end after 2012...
                                // If they already have 2016 in their collection, don't add
                                shouldAdd2016 = true;
                            }
                        }
                        lastYearCursor.close();

                        if(shouldAdd2016){
                            // For each collection, we need to know which mint marks are present
                            // Get the distinct columns from the coinMint field
                            Cursor coinMintCursor = db.query(true, "[" + name + "]", new String[] {"coinMint"}, null, null, null, null, "_id", null);

                            // We need to determine which mint marks to add
                            boolean hasAddedP = false;
                            if(coinMintCursor.moveToFirst()){
                                do{
                                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));

                                    ContentValues new2016CoinValues = new ContentValues();
									switch (coinMint) {
										case "":
											if (!hasAddedP) {
												new2016CoinValues.put("coinIdentifier", "2016");
												new2016CoinValues.put("coinMint", "");
												// Fall through to insert coin info
											} else {
												// Do nothing... This collection has mint marks displayed, so the only
												// values we want to add are " P" and " D"
												continue;
											}
											break;
										case " P":
											new2016CoinValues.put("coinIdentifier", "2016");
											new2016CoinValues.put("coinMint", " P");
											hasAddedP = true;
											// Fall through to insert coin info

											// We may have mistakenly added the "" mint mark, since earlier
											// Philly minted coins didn't have the mint mark.  In this case,
											// delete it
											if (1 == db.delete("[" + name + "]", "coinIdentifier=? AND coinMint=?", new String[]{"2016", ""})) {
												total--;
											}
											break;
										case " D":
											new2016CoinValues.put("coinIdentifier", "2016");
											new2016CoinValues.put("coinMint", " D");
											// Fall through to insert coin info
											break;
										default:

											// Don't want to add in any " S"s
											continue;
									}

                                    if(db.insert("[" + name + "]", null, new2016CoinValues) != -1){
                                        total++;
                                    }

                                }while(coinMintCursor.moveToNext());
                            }

                            // All done with the coin mint information
                            coinMintCursor.close();
                        }
                    }

                    ContentValues values = new ContentValues();

                    // Set the total to be updated if needed
                    if(originalTotal != total){
                        values.put("total", total);
                    }

                    // Finally, since we added the displayOrder column, populate that.
                    // In the import case this may get done twice (in the case of going from
                    // an imported 7 DB to the latest version.
                    values.put("displayOrder", i);

                    db.update("collection_info", values, "name=? AND coinType=?", new String[]{name, coinType});
                    i++;

                    // Move to the next collection
                } while(resultCursor.moveToNext());
            }
            resultCursor.close();

		}

        public void updateToVersion9(SQLiteDatabase db){

            // TODO Update copy and pasted code
            // Get all of the created tables
            Cursor resultCursor = db.query("collection_info", new String[] {"name", "coinType",
                    "total"}, null, null, null, null, "_id");
            if (resultCursor.moveToFirst()){
                do{

                    String name = resultCursor.getString(resultCursor.getColumnIndex("name"));
                    String coinType = resultCursor.getString(resultCursor.getColumnIndex("coinType"));
                    int total = resultCursor.getInt(resultCursor.getColumnIndex("total"));

                    // If we add coins, keep track so we can update the total only if necessary
                    int originalTotal = total;

                    switch (coinType) {
                        case "Presidential Dollars": {
                            // Add in missing 2016 Presidential Dollars

                            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
                            newCoinIdentifiers.add("Ronald Reagan");

                            // Add these coins, mimicking which coinMints the user already has defined
                            total += addFromArrayList(db, name, newCoinIdentifiers);
                            break;
                        }
                        case "National Park Quarters": {
                            // Add in 2017 National Park Quarters

                            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
                            newCoinIdentifiers.add("Effigy Mounds");
                            newCoinIdentifiers.add("Frederick Douglass");
                            newCoinIdentifiers.add("Ozark Riverways");
                            newCoinIdentifiers.add("Ellis Island");
                            newCoinIdentifiers.add("George Rogers Clark");

                            // Add these coins, mimicking which coinMints the user already has defined
                            total += addFromArrayList(db, name, newCoinIdentifiers);
                            break;
                        }
                        case "First Spouse Gold Coins": {
                            // Add in remaining First Spouse Gold Coins

                            ArrayList<String> newCoinIdentifiers = new ArrayList<>();
                            newCoinIdentifiers.add("Patricia Nixon");
                            newCoinIdentifiers.add("Betty Ford");
                            newCoinIdentifiers.add("Nancy Reagan");

                            // Add these coins, mimicking which coinMints the user already has defined
                            total += addFromArrayList(db, name, newCoinIdentifiers);
                            break;
                        }
                    }

                    // For each collection, add in 2017 coins if necessary

                    if(coinType.equals("Pennies")           || coinType.equals("Nickels")           ||
                            coinType.equals("Dimes")             || coinType.equals("Half-Dollars")      ||
                            coinType.equals("Sacagawea Dollars") || coinType.equals("Sacagawea/Native American Dollars") ||
                            coinType.equals("American Eagle Silver Dollars")){

                        // First, we need to determine whether we should add 2017 to this album
                        Cursor lastYearCursor = db.query("[" + name + "]", new String[] {"coinIdentifier"}, null, null, null, null, "_id DESC", "1");
                        boolean shouldAdd2017 = false;
                        if(lastYearCursor.moveToFirst()){
                            String lastYear = lastYearCursor.getString(lastYearCursor.getColumnIndex("coinIdentifier"));
                            if(lastYear.equals("2016")){
                                // If the collection included 2016 coins, it's likely they want 2017 in there too
                                // Also, in earlier versions it was possible to make a collection end after 2012...
                                // If they already have 2017 in their collection, don't add
                                shouldAdd2017 = true;
                            }
                        }
                        lastYearCursor.close();

                        if(shouldAdd2017){
                            // For each collection, we need to know which mint marks are present
                            // Get the distinct columns from the coinMint field
                            Cursor coinMintCursor = db.query(true, "[" + name + "]", new String[] {"coinMint"}, null, null, null, null, "_id", null);

                            // We need to determine which mint marks to add.  The way we do this is
                            // a bit complicated, but is as follows:
                            // - If we see P's, we should add another P
                            // - If we see D's, we should add another D
                            // - If we see a blank mint mark, there are three cases that could be
                            //   be occurring:
                            //       - The collection has no mint marks displayed
                            //       - It's a collection type where coins from the P mint don't
                            //         have a mint mark (Ex: Lincoln Cents)
                            //       - It's a collection type where coins from the P mint mark
                            //         used to not have a mint mark but now do.
                            //
                            //   We only want to include a blank mark in the first two cases
                            //   above, so, if we see a blank mint mark and not a P mint mark.
                            //
                            //   A final note - we want to add the P mint mark before the D mint
                            //   mark (our convention.)

                            boolean shouldAddP = false;
                            boolean shouldAddD = false;
                            boolean foundBlank = false;

                            if(coinMintCursor.moveToFirst()){
                                do{
                                    String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));

                                    switch (coinMint) {
                                        case "":
                                            foundBlank = true;
                                            break;
                                        case " P":
                                            shouldAddP = true;
                                            break;
                                        case " D":
                                            shouldAddD = true;
                                            break;
                                        default:
                                            // Don't want to add in any " S"s
                                            break;
                                    }
                                } while(coinMintCursor.moveToNext());
                            }

                            // All done with the coin mint information
                            coinMintCursor.close();

                            if (shouldAddP) {
                                ContentValues new2017PCoinValues = new ContentValues();
                                new2017PCoinValues.put("coinIdentifier", "2017");
                                new2017PCoinValues.put("coinMint", " P");
                                if(db.insert("[" + name + "]", null, new2017PCoinValues) != -1){
                                    total++;
                                }
                            } else {

                                if (foundBlank) {
                                    ContentValues new2017BlankCoinValues = new ContentValues();
                                    new2017BlankCoinValues.put("coinIdentifier", "2017");
                                    new2017BlankCoinValues.put("coinMint", "");
                                    if (db.insert("[" + name + "]", null, new2017BlankCoinValues) != -1) {
                                        total++;
                                    }
                                }
                            }

                            if (shouldAddD) {
                                ContentValues new2017DCoinValues = new ContentValues();
                                new2017DCoinValues.put("coinIdentifier", "2017");
                                new2017DCoinValues.put("coinMint", " D");
                                if(db.insert("[" + name + "]", null, new2017DCoinValues) != -1){
                                    total++;
                                }
                            }
                        }
                    }

                    // Last thing is to update the total for each collection
                    if(originalTotal != total){
                        ContentValues values = new ContentValues();
                        values.put("total", total);
                        db.update("collection_info", values, "name=? AND coinType=?", new String[] { name, coinType });
                    }

                    // Move to the next collection
                } while(resultCursor.moveToNext());
            }
            resultCursor.close();
        }

        /**
		 * @param db The SQLiteDatabase object to use
		 * @param name the collection name
         * @param values the ArrayList containing
		 * @return number of rows added
		 */
		
		private int addFromArrayList(SQLiteDatabase db, String name, ArrayList<String> values) {
			
			int total = 0;
			// Get the distinct columns from the coinMint field
			Cursor coinMintCursor = db.query(true, "[" + name + "]", new String[] {"coinMint"}, null, null, null, null, "_id", null);
			
			// Now add the new coins
			for(int j = 0; j < values.size(); j++){
				ContentValues initialValues = new ContentValues();
				initialValues.put("coinIdentifier", values.get(j));
				initialValues.put("inCollection", 0);
			    // For each mint mark in the collection, add in a new Coin
			    if(coinMintCursor.moveToFirst()){
			    	do{
				        String coinMint = coinMintCursor.getString(coinMintCursor.getColumnIndex("coinMint"));
				        
				        initialValues.remove("coinMint");
				        initialValues.put("coinMint", coinMint);
				        if(db.insert("[" + name + "]", null, initialValues) != -1){
				            total++;
				        }
				        
			    	}while(coinMintCursor.moveToNext());
			    }
			}
		    
			// All done with the coin mint information
			coinMintCursor.close();
			
			return total;
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public DatabaseAdapter(Context ctx) {
		this.mContext = ctx;
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public DatabaseAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	// Clean up a bit if we no longer need this DatabaseAdapter
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Get the total number of coins in the collection
	 * 
	 * @param name String that identifiers which table to query
	 * @return int with the total number of coins in the collection
	 */
	public int fetchTotalCollected(String name) {
		String select_sqlStatement = "SELECT COUNT(_id) FROM [" + name + "] WHERE inCollection=1 LIMIT 1";
		SQLiteStatement compiledStatement;

		compiledStatement = mDb.compileStatement(select_sqlStatement);
		// TODO May generate a SQLITE_SCHEMA error (17) after just deleting a table, doesn't appear to break things though

		int result = (int) compiledStatement.simpleQueryForLong();

		compiledStatement.clearBindings();
		compiledStatement.close();
		return result;
	}

	/**
	 * Returns whether a coinIdentifier and coinMint has been marked as collected in a given
     * collection.
	 *
	 * @param name The collection of interest
	 * @param coinIdentifier The coinIdentifier of the coin we are to retrieve data for
     * @param coinMint The coinMint of the coin we are to retrieve data for
	 * @return 0 if item is in the collection, 1 otherwise
	 * @throws SQLException if coin could not be found (Shouldn't happen???)
	 */
	// TODO Rename
    // TODO Retrieving the coin information individually (and onScroll) is inefficient... We should
    // instead have one query that returns all of the info.
	private int fetchInfo(String name, String coinIdentifier, String coinMint) throws SQLException {
		String select_sqlStatement = "SELECT inCollection FROM [" + name + "] WHERE coinIdentifier=? AND coinMint=? LIMIT 1";
		SQLiteStatement compiledStatement = mDb.compileStatement(select_sqlStatement);

		compiledStatement.bindString(1, coinIdentifier);
		compiledStatement.bindString(2, coinMint);
		int result = (int) compiledStatement.simpleQueryForLong();

		compiledStatement.clearBindings();
		compiledStatement.close();
		return result;
	}

	/**
	 * Updates a coins presence in the database.
	 * 
	 * @param name The name of the collection of interest
     * @param coinIdentifier The coinIdentifier of the coin we are to retrieve data for
     * @param coinMint The coinMint of the coin we are to retrieve data for
	 * @return true if the value was successfully updated, false otherwise
	 */
	
	public boolean updateInfo(String name, String coinIdentifier, String coinMint) {
		int result = fetchInfo(name, coinIdentifier, coinMint);
		int newValue = (result + 1) % 2;
		ContentValues args = new ContentValues();
		args.put("inCollection", newValue);

		// TODO Should we do something if update fails?
		return mDb.update("[" + name + "]", args, "coinIdentifier=? AND coinMint =?", new String[] {coinIdentifier, coinMint}) > 0;
	}
	
	/**
	 * Returns the display configured for the table (advanced view, simple view, etc.)
	 * 
	 * @param tableName - Used to know which table to query
	 * @return which display we should show.  See MainApplication for the types
	 * @throws SQLException if an SQL-related error occurs
	 */
	public int fetchTableDisplay(String tableName) throws SQLException {
		
		// The database will only be set up this way in this case
		String select_sqlStatement = "SELECT display FROM collection_info WHERE name=? LIMIT 1";
		SQLiteStatement compiledStatement = mDb.compileStatement(select_sqlStatement);

		compiledStatement.bindString(1, tableName);
		int result = (int) compiledStatement.simpleQueryForLong();

		compiledStatement.clearBindings();
		compiledStatement.close();
		return result;
	}
	
	/**
	 * Updates the display type associated with a given collection
	 * 
	 * @param tableName - Used to know which table to update
	 * @param displayType - New displaytype to store for this table
	 * @return 0 on update success, 1 otherwise
     * @throws SQLException if an SQL-related error occurs
	 */
	public boolean updateTableDisplay(String tableName, int displayType) throws SQLException {

		ContentValues args = new ContentValues();
		args.put("display", displayType);

        // TODO Should we do something if update fails?
        return mDb.update("collection_info", args, "name=?", new String[] { tableName }) > 0;
	}

    /**
     * Updates the order in which a collection should appear in the list of collections
     *
     * @param tableName - Used to know which table to update
     * @param displayOrder - New displayOrder to store for this table
     * @return 0 if the update was successful, 1 otherwise
     * @throws SQLException if an SQL-related error occurs
     */
    public boolean updateDisplayOrder(String tableName, int displayOrder) throws SQLException {

        ContentValues args = new ContentValues();
        args.put("displayOrder", displayOrder);
        // TODO Should we do something if update fails?
        return mDb.update("collection_info", args, "name=?", new String[] { tableName }) > 0;

    }

    /**
     * Updates the info for the coin in table 'name' where the coin is identified with
     * coinIdentifier and coinMint. This includes the advanced info (coin grade, quantity, and
     * notes) in addition to whether it is inc the collection.
     * @param name The collection name
     * @param coinIdentifier
     * @param coinMint
     * @param grade
     * @param quantity
     * @param notes
     * @param inCollection
     * @return 1 on success, 0 otherwise
     */
	public int updateAdvInfo(String name, String coinIdentifier, String coinMint, int grade,
			                 int quantity, String notes, int inCollection) {

		ContentValues args = new ContentValues();
		args.put("inCollection", inCollection);
	    args.put("advGradeIndex", grade);
	    args.put("advQuantityIndex", quantity);
	    args.put("advNotes", notes);
	    return mDb.update("[" + name + "]", args, "coinIdentifier=? AND coinMint =?", new String[] {coinIdentifier, coinMint});
	}

    /**
     * Helper function to issue the SQL needed when creating a new database table for a collection
     * @param name The collection name
     */
	private void createNewTable(String name){
        // v2.2.1 - Until this point all fields had '_id' created with 'autoincrement'
        // which is unnecessary for our purposes.  Removing to improve performance.
		String DATABASE_CREATE = "CREATE TABLE [" + name
		+ "] (_id integer primary key,"
		+ " coinIdentifier text not null,"
		+ " coinMint text,"
		+ " inCollection integer,"
		+ " advGradeIndex integer default 0,"
        + " advQuantityIndex integer default 0,"
        + " advNotes text default \"\");";

		mDb.execSQL(DATABASE_CREATE);
	}

    /**
     * Handles adding everything needed for a collection to store it's data in the database.
     * This is used for creating new collections, and will initialize everything to a blank state.
     *
     * @param name The collection name
     * @param coinType The collection type
     * @param coinIdentifiers A list of the identifiers for the coins in this collection (Ex: 2009)
     * @param coinMints A list of the mints for the coins in this collection
     * @param displayOrder The position in the list of collections in which this should be displayed
     *                     TODO maybe make this not an argument, and determine this internally?
     * @return 1 TODO
     */
    // TODO Rename, since we aren't just creating a new table
	public int createNewTable(String name, String coinType, ArrayList<String> coinIdentifiers,
                              ArrayList<String> coinMints, int displayOrder) {

		// Actually make the table
		createNewTable(name);

		// We have the list of identifiers, now set them correctly
		for(int j = 0; j < coinIdentifiers.size(); j++){
			ContentValues initialValues = new ContentValues();
			initialValues.put("coinIdentifier", coinIdentifiers.get(j));
			initialValues.put("coinMint", coinMints.get(j));
			initialValues.put("inCollection", 0);
			// Advanced info gets added automatically, if the columns are there

			long value = mDb.insert("[" + name + "]", null, initialValues);
			// TODO Do something if insert fails?
		}

		// We also need to add the table to the list of tables
		addEntryToCollectionInfoTable(name, coinType, coinIdentifiers.size(), MainApplication.SIMPLE_DISPLAY, displayOrder);
		
		return 1;
	}

    /**
     * Helper function to add a collection into the global list of collections
     * @param name
     * @param coinType
     * @param total
     * @param display
     * @param displayOrder
     */
	private void addEntryToCollectionInfoTable(String name, String coinType, int total, int display, int displayOrder){
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("coinType", coinType);
		values.put("total", total);
        values.put("displayOrder", displayOrder);
        values.put("display", display);
		
		long value = mDb.insert("collection_info", null, values);
		// TODO Do something if insert fails?
		// TODO It'd be great if there was a way to clear the prepared SQL Statement cache so that
        // we don't get SQL 17 Errors
	}

    /**
     * Handles adding everything needed for a collection to store it's data in the database.
     * This also allows the data to be pre-populated in the database. This is used for importing
     * collections
     * @param name The collection name
     * @param coinType The collection type
     * @param total The total number of coins in the collection (TODO I think)
     * @param display The display type of this collection
     * @param displayOrder The order that this collection should appear in the list of collections
     * @param rawData The data that should be put into the backing database once it is created
     * @return 1 TODO
     */
    // TODO Rename, since we aren't just creating a new table
    public int createNewTable(String name, String coinType, int total, int display, int displayOrder, String[][] rawData) {

		// Actually make the table
		createNewTable(name);

		// We have the list of identifiers, now set them correctly
		for (String[] rawRowData : rawData) {

			// coinIdentifier, coinMint, inCollection, advGradeIndex, advQuantityIndex, advNotes
			ContentValues initialValues = new ContentValues();
			initialValues.put("coinIdentifier", rawRowData[0]);
			initialValues.put("coinMint", rawRowData[1]);
			initialValues.put("inCollection", Integer.valueOf(rawRowData[2]));
            initialValues.put("advGradeIndex", Integer.valueOf(rawRowData[3]));
			initialValues.put("advQuantityIndex", Integer.valueOf(rawRowData[4]));
			initialValues.put("advNotes", rawRowData[5]);

			long value = mDb.insert("[" + name + "]", null, initialValues);
			// TODO Do something if insert fails?
		}

		// We also need to add the table to the list of tables
		addEntryToCollectionInfoTable(name, coinType, total, display, displayOrder);
		return 1;
	}

	/**
	 * Handles removing a collection from the database
	 * 
	 * @param name The collection name
	 */
    // TODO Rename, since it does more than just drop a table
	public void dropTable(String name){
		String DATABASE_DROP = "DROP TABLE [" + name + "];";
		mDb.execSQL(DATABASE_DROP);

		//long value = mDb.delete("collection_info", "name=\"" + name + "\"", null);
		int value = mDb.delete("collection_info", "name=?", new String[] { name });
		// TODO Do something if insert fails?
		// TODO It be great if there was a way to clear the prepared SQL Statement cache so that
        // we don't get SQL 17 Errors
        // ^^^ Not sure if this is still an issue
	}

    /**
     * Deletes the table of metadata about all the current collections
     */
	public void dropCollectionInfoTable(){
		
		String DATABASE_DROP = "DROP TABLE [collection_info];";
		mDb.execSQL(DATABASE_DROP);
	}

    /**
     * Creates the table of metadata for all the current collections
     */
	public void createCollectionInfoTable(){
		
		// I would put the functionality here and call it from within the mDbHelper,
		// but I couldn't figure out how to get this working.  :(
	    mDbHelper.createCollectionInfoTable(mDb);
	}

	/**
	 * Return a Cursor that gives the names of all of the defined collections
	 * 
	 * @return Cursor
	 */
	public Cursor getAllCollectionNames() {

		return mDb.query("collection_info", new String[] {"name"}, null, null, null, null, "displayOrder");
	}

	/**
	 * Return a Cursor that gives information about each of the collections
	 * 
	 * @return Cursor
	 */
	public Cursor getAllTables() {

		return mDb.query("collection_info", new String[] {"name", "coinType",
		"total"}, null, null, null, null, "displayOrder");
	}

	/**
	 * Get the list of identifiers for each collection
     *
	 * @param name The name of the collection
	 * @return Cursor over all coins in the collection
	 */
	public Cursor getAllIdentifiers(String name) {

		return mDb.query("[" + name + "]", new String[] {"coinIdentifier", "coinMint"},
				null, null, null, null, "_id");
	}  
	
	/**
	 * Get whether each coin is in the collection
	 * 
	 * @return Cursor over all coins in the collection
	 */
	public Cursor getInCollectionInfo(String tableName) {
		return mDb.query("[" + tableName + "]", new String[] {"inCollection"},
					null, null, null, null, "_id");
	}
	
	
	/**
	 * Get the advanced info associated with each coin in the collection
     *
	 * @param name The collection name
	 * @return Cursor over all coins in the collection
	 */
	public Cursor getAdvInfo(String name) {

		return mDb.query("[" + name + "]", new String[] {"advGradeIndex", "advQuantityIndex", "advNotes"},
				null, null, null, null, "_id");
	}
	
	/**
	 * Get all collection info (for exporting)
	 *
     * @param name The collection name
	 * @return Cursor over all coins in the collection
	 */
	public Cursor getAllCollectionInfo(String name) {

        return mDb.query("[" + name + "]", new String[] {"coinIdentifier", "coinMint", "inCollection", "advGradeIndex", "advQuantityIndex", "advNotes"},
				null, null, null, null, "_id");
	}

    /**
     * Expose the dbHelper's onUpgrade method so we can call it manually when importing collections
     *
     * @param oldVersion the db version to upgrade from
	 */
    public void upgradeCollections(int oldVersion) {
        mDbHelper.onUpgrade(mDb, oldVersion, DatabaseAdapter.DATABASE_VERSION);
    }
}
