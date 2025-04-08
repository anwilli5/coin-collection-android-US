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

import android.app.Application;

import com.coincollection.CollectionInfo;
import com.coincollection.DatabaseAdapter;
import com.spencerpages.collections.AllNickels;
import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.AmericanInnovationDollars;
import com.spencerpages.collections.AmericanWomenQuarters;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BasicHalfDollars;
import com.spencerpages.collections.BasicQuarters;
import com.spencerpages.collections.BuffaloNickels;
import com.spencerpages.collections.Cartwheels;
import com.spencerpages.collections.CladQuarters;
import com.spencerpages.collections.BasicDimes;
import com.spencerpages.collections.EarlyDimes;
import com.spencerpages.collections.EarlyDollars;
import com.spencerpages.collections.EarlyHalfDollars;
import com.spencerpages.collections.EarlyQuarters;
import com.spencerpages.collections.EisenhowerDollar;
import com.spencerpages.collections.FirstSpouseGoldCoins;
import com.spencerpages.collections.FranklinHalfDollars;
import com.spencerpages.collections.HalfCents;
import com.spencerpages.collections.HalfDimes;
import com.spencerpages.collections.IndianHeadCents;
import com.spencerpages.collections.JeffersonNickels;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LargeCents;
import com.spencerpages.collections.LibertyHeadNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.MercuryDimes;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NationalParkQuarters;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.PresidentialDollars;
import com.spencerpages.collections.CoinSets;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.SilverDimes;
import com.spencerpages.collections.SilverHalfDollars;
import com.spencerpages.collections.SmallCents;
import com.spencerpages.collections.SmallDollars;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.Trimes;
import com.spencerpages.collections.TwentyCents;
import com.spencerpages.collections.TwoCents;
import com.spencerpages.collections.WalkingLibertyHalfDollars;
import com.spencerpages.collections.SilverQuarters;
import com.spencerpages.collections.WashingtonQuarters;
import com.spencerpages.collections.WestPoint;

public class MainApplication extends Application {

    // App name string, used when printing log messages
    public static final String APP_NAME = "CoinCollection";

    // String used to indicate the SharedPreference store to use.
    // SharedPreferences are used to store information like whether the help
    // dialogs have been seen before.
    public static final String PREFS = "mainPreferences";

    // List of all the supported collection types by the app.  New collections
    // should be added here, but don't reorder (reorder lists below)
    public static final CollectionInfo[] COLLECTION_TYPES = {
            new LincolnCents(),
            new JeffersonNickels(),
            new BasicDimes(),
            new BasicQuarters(),
            new StateQuarters(),
            new NationalParkQuarters(),
            new BasicHalfDollars(),
            new EisenhowerDollar(),
            new SusanBAnthonyDollars(),
            new NativeAmericanDollars(),
            new PresidentialDollars(),
            new IndianHeadCents(),
            new LibertyHeadNickels(),
            new BuffaloNickels(),
            new BarberDimes(),
            new MercuryDimes(),
            new BarberQuarters(),
            new StandingLibertyQuarters(),
            new BarberHalfDollars(),
            new WalkingLibertyHalfDollars(),
            new FranklinHalfDollars(),
            new MorganDollars(),
            new PeaceDollars(),
            new AmericanEagleSilverDollars(),
            new FirstSpouseGoldCoins(),
            new AmericanInnovationDollars(),
            new AmericanWomenQuarters(),
            new SmallCents(),
            new LargeCents(),
            new AllNickels(),
            new HalfDimes(),
            new SilverDimes(),
            new EarlyDimes(),
            new CladQuarters(),
            new SilverQuarters(),
            new EarlyQuarters(),
            new SmallDollars(),
            new SilverHalfDollars(),
            new Trimes(),
            new TwentyCents(),
            new TwoCents(),
            new WestPoint(),
            new EarlyDollars(),
            new EarlyHalfDollars(),
            new Cartwheels(),
            new HalfCents(),
            new CoinSets(),
            new KennedyHalfDollars(),
            new RooseveltDimes(),
            new WashingtonQuarters()
    };

    // Display order and groups in Collection Page Creator
    // Sorted roughly be denomination and alphabetical (can be re-ordered as needed)
    public static final Class<?>[] BASIC_COLLECTIONS = {
            LincolnCents.class,
            JeffersonNickels.class,
            BasicDimes.class,
            BasicQuarters.class,
            AmericanWomenQuarters.class,
            NationalParkQuarters.class,
            StateQuarters.class,
            BasicHalfDollars.class,
            AmericanInnovationDollars.class,
            FirstSpouseGoldCoins.class,
            NativeAmericanDollars.class,
            PresidentialDollars.class,
    };

    // Sorted roughly be denomination and alphabetical (can be re-ordered as needed)
    public static final Class<?>[] ADVANCED_COLLECTIONS = {
            SmallCents.class,
            LargeCents.class,
            AllNickels.class,
            HalfDimes.class,
            RooseveltDimes.class,
            SilverDimes.class,
            EarlyDimes.class,
            CladQuarters.class,
            WashingtonQuarters.class,
            SilverQuarters.class,
            EarlyQuarters.class,
            KennedyHalfDollars.class,
            SilverHalfDollars.class,
            EarlyHalfDollars.class,
            Cartwheels.class,
            EarlyDollars.class,
            SmallDollars.class,
    };

    public static final Class<?>[] MORE_COLLECTIONS = {
            CoinSets.class,
            WestPoint.class,
            HalfCents.class,
            IndianHeadCents.class,
            TwoCents.class,
            Trimes.class,
            BuffaloNickels.class,
            LibertyHeadNickels.class,
            BarberDimes.class,
            MercuryDimes.class,
            TwentyCents.class,
            BarberQuarters.class,
            StandingLibertyQuarters.class,
            BarberHalfDollars.class,
            WalkingLibertyHalfDollars.class,
            AmericanEagleSilverDollars.class,
            EisenhowerDollar.class,
            FranklinHalfDollars.class,
            MorganDollars.class,
            PeaceDollars.class,
            SusanBAnthonyDollars.class,
    };

    public static final String DATABASE_NAME = "CoinCollection";

    private final DatabaseAdapter mDbAdapter = new DatabaseAdapter(this);

    public DatabaseAdapter getDbAdapter() {
        return mDbAdapter;
    }

    /**
     * DATABASE_VERSION Tracks the current database version, and is essential for periodic
     * database updating.  It should be raised anytime we need to insert new
     * coins into a user's collections (Ex: yearly coin addition, bug fixes).
     *
     * Version 2 - Used in Versions 1 and 1.1 of the app
     * Version 3 - Used in Version 1.2 and 1.3 of the app
     * Version 4 - Used in Version 1.4, 1.4.1, and 1.5 of the app
     * Version 5 - Used in Version 1.6 of the app
     * Version 6 - Used in Version 2.0, 2.0.1 of the app
     * Version 7 - Used in Version 2.1, 2.1.1 of the app
     * Version 8 - Used in Version 2.2.1 of the app
     * Version 9 - Used in Version 2.3 of the app
     * Version 10 - Used in Version 2.3.1 of the app
     * Version 11 - Used in Version 2.3.2 of the app
     * Version 12 - Used in Version 2.3.3 of the app
     * Version 13 - Used in Version 2.3.4 of the app
     * Version 14 - Used in Version 2.3.5 of the app
     * Version 15 - Used in Version 3.0.0 of the app
     * Version 16 - Used in Version 3.1.0 of the app
     * Version 17 - Used in Version 3.3.0 of the app
     * Version 18 - Used in Version 3.4.0 of the app
     * Version 19 - Used in Version 3.5.0 of the app
     * Version 20 - Used in Version 3.6.0 of the app
     * Version 21-23 - Used in Version 3.7.0 of the app
     */
    public static final int DATABASE_VERSION = 23;

    /**
     * Get the collection index from collection type name
     *
     * @param collectionTypeName collection name
     * @return int index or -1 if not found
     */
    public static int getIndexFromCollectionNameStr(String collectionTypeName) {
        for (int i = 0; i < COLLECTION_TYPES.length; i++) {
            if (COLLECTION_TYPES[i].getCoinType().equals(collectionTypeName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the collection index from collection type name
     *
     * @param collectionClass collection class
     * @return int index or -1 if not found
     */
    public static int getIndexFromCollectionClass(Class<?> collectionClass) {
        for (int i = 0; i < COLLECTION_TYPES.length; i++) {
            if (collectionClass.isInstance(COLLECTION_TYPES[i])) {
                return i;
            }
        }
        return -1;
    }
}
