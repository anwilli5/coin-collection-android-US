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

import com.spencerpages.collections.AmericanEagleSilverDollars;
import com.spencerpages.collections.BarberDimes;
import com.spencerpages.collections.BarberHalfDollars;
import com.spencerpages.collections.BarberQuarters;
import com.spencerpages.collections.BuffaloNickels;
import com.spencerpages.collections.CollectionInfo;
import com.spencerpages.collections.EisenhowerDollar;
import com.spencerpages.collections.FirstSpouseGoldCoins;
import com.spencerpages.collections.FranklinHalfDollars;
import com.spencerpages.collections.IndianHeadCents;
import com.spencerpages.collections.JeffersonNickels;
import com.spencerpages.collections.KennedyHalfDollars;
import com.spencerpages.collections.LibertyHeadNickels;
import com.spencerpages.collections.LincolnCents;
import com.spencerpages.collections.MercuryDimes;
import com.spencerpages.collections.MorganDollars;
import com.spencerpages.collections.NationalParkQuarters;
import com.spencerpages.collections.NativeAmericanDollars;
import com.spencerpages.collections.PeaceDollars;
import com.spencerpages.collections.PresidentialDollars;
import com.spencerpages.collections.RooseveltDimes;
import com.spencerpages.collections.StandingLibertyQuarters;
import com.spencerpages.collections.StateQuarters;
import com.spencerpages.collections.SusanBAnthonyDollars;
import com.spencerpages.collections.WalkingLibertyHalfDollars;
import com.spencerpages.collections.WashingtonQuarters;

import java.util.HashMap;

public class MainApplication extends Application {

    // App name string, used when printing log messages
    public static final String APP_NAME = "CoinCollection";

    // String used to indicate the SharedPreference store to use.
    // SharedPreferences are used to store information like whether the help
    // dialogs have been seen before.
    public static final String PREFS = "mainPreferences";

    // Global "enum" values
    public static final int SIMPLE_DISPLAY = 0;
    public static final int ADVANCED_DISPLAY = 1;

    // List of all the supported collection types by the app.  New collections
    // should be added here
    public static final CollectionInfo[] COLLECTION_TYPES =
            {
                    new LincolnCents(),
                    new JeffersonNickels(),
                    new RooseveltDimes(),
                    new WashingtonQuarters(),
                    new StateQuarters(),
                    new NationalParkQuarters(),
                    new KennedyHalfDollars(),
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
            };
}
