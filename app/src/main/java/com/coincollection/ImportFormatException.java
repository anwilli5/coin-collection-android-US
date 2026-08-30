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

/**
 * Thrown when an import file can't be parsed into valid collection data.
 * <p>
 * This is a checked exception on purpose: the import entry points must handle it
 * and turn it into a user-facing error string rather than letting a malformed
 * file crash the app or, worse, silently import mangled data.
 */
public class ImportFormatException extends Exception {

    public ImportFormatException(String message) {
        super(message);
    }

    public ImportFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
