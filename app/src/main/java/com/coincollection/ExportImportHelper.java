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

import android.content.res.Resources;
import android.database.SQLException;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.spencerpages.MainApplication;
import com.spencerpages.R;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ExportImportHelper {

    // Internal state
    final Resources mRes;
    final DatabaseAdapter mDbAdapter;

    public final static String JSON_CHARSET = "UTF-8";

    // JSON keys
    public final static String JSON_DB_VERSION = "databaseVersion";
    public final static String JSON_COLLECTIONS = "collections";
    public final static String JSON_COIN_LIST = "coinList";

    // CSV keys
    public final static String CSV_SEPARATOR = "-----";

    public enum SectionType {
        DATABASE_VERSION(JSON_DB_VERSION),
        COLLECTIONS(JSON_COLLECTIONS),
        COIN_LIST(JSON_COIN_LIST),
        UNKNOWN("unknown");

        public final String label;

        SectionType(String label) {
            this.label = label;
        }

        public static SectionType fromLabel(String label) {
            for (SectionType sectionType : values()) {
                if (sectionType.label.equals(label)) {
                    return sectionType;
                }
            }
            return UNKNOWN;
        }
    }

    // Legacy export file/directory name
    public final static String LEGACY_EXPORT_FOLDER_NAME = "/coin-collection-app-files";
    public final static String LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME = "list-of-collections";
    public final static String LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT = ".csv";
    public final static String LEGACY_EXPORT_DB_VERSION_FILE = "database_version.txt";

    public ExportImportHelper(Resources res, DatabaseAdapter dbAdapter) {
        mRes = res;
        mDbAdapter = dbAdapter;
    }

    /**
     * This method imports collections from the external storage (legacy storage). New versions
     * of the app export to JSON and store in a user-accessible storage location
     *
     * @param importDirectory import directory
     * @return "" if successful, otherwise an error message to display
     */
    public String importCollectionsFromLegacyCSV(String importDirectory) {

        // See whether we can read from the external storage
        String state = Environment.getExternalStorageState();
        //noinspection StatementWithEmptyBody
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Should be able to read from it without issue
        } else if (Environment.MEDIA_SHARED.equals(state)) {
            // Shared with PC so can't write to it
            return mRes.getString(R.string.cannot_rd_ext_media_shared);
        } else {
            // Doesn't exist, so notify user
            return mRes.getString(R.string.cannot_rd_ext_media_state, state);
        }

        File dir = new File(importDirectory);
        if (!dir.isDirectory()) {
            // The directory doesn't exist, notify the user
            return mRes.getString(R.string.cannot_find_export_dir, importDirectory);
        }

        // Read the database version
        File inputFile = new File(dir, LEGACY_EXPORT_DB_VERSION_FILE);
        int importDatabaseVersion;
        ArrayList<CollectionListInfo> importedCollectionInfoList = new ArrayList<>();
        ArrayList<ArrayList<CoinSlot>> importedCollectionContents = new ArrayList<>();
        try {
            ArrayList<String[]> fileContents = getCsvFileContents(inputFile);
            if (!fileContents.isEmpty() && fileContents.get(0).length > 0) {
                importDatabaseVersion = Integer.parseInt(fileContents.get(0)[0]);
            } else {
                return mRes.getString(R.string.error_reading_file, inputFile.getAbsolutePath());
            }
        } catch (IOException | CsvValidationException ignored) {
            return mRes.getString(R.string.error_open_file_reading, inputFile.getAbsolutePath());
        }

        // Read the collection_info table
        inputFile = new File(dir, LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME + LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT);
        try {
            ArrayList<String[]> fileContents = getCsvFileContents(inputFile);
            for (String[] items : fileContents) {
                importedCollectionInfoList.add(new CollectionListInfo(items));
            }
        } catch (IOException | CsvValidationException ignored) {
            return mRes.getString(R.string.error_open_file_reading, inputFile.getAbsolutePath());
        }

        // We loaded in the collection "metadata" table, so now load in each collection
        ArrayList<String> collectionErrorMessages = new ArrayList<>();
        for (int i = 0; i < importedCollectionInfoList.size(); i++) {
            CollectionListInfo collectionData = importedCollectionInfoList.get(i);
            // If any '/''s exist in the collection name, change them to "_SL_" to match
            // the export logic (used to prevent slashes from being confused as path
            // delimiters when opening the file.)
            String collectionFileName = collectionData.getName().replaceAll("/", "_SL_");
            inputFile = new File(dir, collectionFileName + ".csv");

            if (!inputFile.isFile()) {
                collectionErrorMessages.add(mRes.getString(R.string.cannot_find_input_file, inputFile.getAbsolutePath()));
                continue;
            }

            // Read in the file
            ArrayList<CoinSlot> collectionContent = new ArrayList<>();
            try {
                ArrayList<String[]> fileContents = getCsvFileContents(inputFile);
                int coinIndex = 0;
                for (String[] items : fileContents) {
                    collectionContent.add(new CoinSlot(items, coinIndex++));
                }
            } catch (IOException | CsvValidationException ignored) {
                collectionErrorMessages.add(mRes.getString(R.string.error_open_file_reading, inputFile.getAbsolutePath()));
                continue;
            }
            importedCollectionContents.add(collectionContent);
        }

        if (!collectionErrorMessages.isEmpty()) {
            // An error occurred in one or more of the databases so show an error
            StringBuilder problems = new StringBuilder();
            for (String message : collectionErrorMessages) {
                problems.append("\n").append(message);
            }
            return mRes.getString(R.string.error_exporting_collections, problems.toString());
        }

        // All data has been parsed from CSV files so perform the DB steps to import
        return updateDatabaseFromImport(importDatabaseVersion, importedCollectionInfoList,
                importedCollectionContents);
    }

    /**
     * This method exports collections to the external storage (legacy storage). New versions
     * of the app export to JSON and store in a user-accessible storage location
     *
     * @param importDirectory import directory
     * @return R.string.success_export if successful, otherwise an error message to display
     */
    public String exportCollectionsToLegacyCSV(String importDirectory) {

        // See whether we can write to the external storage
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
                // Should be able to write to it without issue
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                // Can't write to it, so notify user
                return mRes.getString(R.string.cannot_wr_ext_media_ro);
            case Environment.MEDIA_SHARED:
                // Shared with PC so can't write to it
                return mRes.getString(R.string.cannot_wr_ext_media_shared);
            default:
                // Doesn't exist, so notify user
                return mRes.getString(R.string.cannot_wr_ext_media_state, state);
        }

        // At this point we know we can write to storage and the user is ok
        // if we blow away existing imported files
        File dir = new File(importDirectory);
        if (!dir.isDirectory() && !dir.mkdir()) {
            // The directory doesn't exist, notify the user
            return mRes.getString(R.string.failed_mk_dir, importDirectory);
        }

        // Get all collection lists from the database
        ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
        mDbAdapter.getAllTables(collectionListEntries);

        // Write out the collection_info table
        File outputFile = new File(dir, LEGACY_EXPORT_COLLECTION_LIST_FILE_NAME + LEGACY_EXPORT_COLLECTION_LIST_FILE_EXT);
        ArrayList<String[]> csvOutputLines = new ArrayList<>();

        // Iterate through the list of collections and write the files
        for (int i = 0; i < collectionListEntries.size(); i++) {
            CollectionListInfo item = collectionListEntries.get(i);
            csvOutputLines.add(item.getCsvExportProperties(mDbAdapter));
        }
        try {
            writeToLegacyCsv(outputFile, csvOutputLines);
        } catch (IOException e) {
            return mRes.getString(R.string.error_exporting, e.getMessage());
        }

        // Write out the database version
        outputFile = new File(dir, LEGACY_EXPORT_DB_VERSION_FILE);
        csvOutputLines = new ArrayList<>();
        String[] version = new String[]{String.valueOf(MainApplication.DATABASE_VERSION)};
        csvOutputLines.add(version);
        try {
            writeToLegacyCsv(outputFile, csvOutputLines);
        } catch (IOException e) {
            return mRes.getString(R.string.error_exporting, e.getMessage());
        }

        // Write out all of the other tables
        for (int i = 0; i < collectionListEntries.size(); i++) {
            CollectionListInfo item = collectionListEntries.get(i);
            String name = item.getName();

            // Handle '/''s in the file names (otherwise importing will fail, because the OS will
            // think the '/' characters are folder delimiters.)  This will be undone when we import.
            String cleanName = name.replaceAll("/", "_SL_");

            outputFile = new File(dir, cleanName + ".csv");
            csvOutputLines = new ArrayList<>();

            ArrayList<CoinSlot> coinList = mDbAdapter.getCoinList(name, true);
            for (CoinSlot coinSlot : coinList) {
                csvOutputLines.add(coinSlot.getLegacyCsvExportProperties());
            }
            try {
                writeToLegacyCsv(outputFile, csvOutputLines);
            } catch (IOException e) {
                return mRes.getString(R.string.error_exporting, e.getMessage());
            }
        }
        return mRes.getString(R.string.success_export, LEGACY_EXPORT_FOLDER_NAME);
    }

    /**
     * This method imports collections from a JSON file
     *
     * @param inputStream input stream to read from
     * @return "" if successful, otherwise an error message to display
     */
    public String importCollectionsFromJson(InputStream inputStream) {

        int importDatabaseVersion = 0;
        ArrayList<CollectionListInfo> importedCollectionInfoList = new ArrayList<>();
        ArrayList<ArrayList<CoinSlot>> importedCollectionContents = new ArrayList<>();

        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, JSON_CHARSET))) {
            // Parse the JSON file
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case JSON_DB_VERSION:
                        importDatabaseVersion = reader.nextInt();
                        break;
                    case JSON_COLLECTIONS:
                        reader.beginArray();
                        while (reader.hasNext()) {
                            ArrayList<CoinSlot> coinList = new ArrayList<>();
                            importedCollectionInfoList.add(new CollectionListInfo(reader, coinList));
                            importedCollectionContents.add(coinList);
                        }
                        reader.endArray();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            // All data has been parsed from CSV files so perform the DB steps to import
            return updateDatabaseFromImport(importDatabaseVersion, importedCollectionInfoList,
                    importedCollectionContents);
        } catch (IOException e) {
            return mRes.getString(R.string.error_importing, e.getMessage());
        }
    }

    /**
     * Update the database with the imported data
     *
     * @param importDatabaseVersion      imported database version
     * @param importedCollectionInfoList imported list of CollectionListInfo
     * @param importedCollectionContents imported list of coins
     * @return "" if successful, otherwise an error string
     */
    private String updateDatabaseFromImport(int importDatabaseVersion,
                                            ArrayList<CollectionListInfo> importedCollectionInfoList,
                                            ArrayList<ArrayList<CoinSlot>> importedCollectionContents) {

        // Drop existing tables
        ArrayList<CollectionListInfo> existingCollections = new ArrayList<>();
        mDbAdapter.getAllTables(existingCollections);
        for (int i = 0; i < existingCollections.size(); i++) {
            CollectionListInfo info = existingCollections.get(i);
            mDbAdapter.dropCollectionTable(info.getName());
        }
        mDbAdapter.dropCollectionInfoTable();

        // Take the data we've stored and replace what's in the database with it
        try {
            // Add new collections
            mDbAdapter.createCollectionInfoTable();
            for (int i = 0; i < importedCollectionInfoList.size(); i++) {
                CollectionListInfo collectionListInfo = importedCollectionInfoList.get(i);
                ArrayList<CoinSlot> collectionContent = importedCollectionContents.get(i);

                // Check for duplicate or illegal names
                int checkName = mDbAdapter.checkCollectionName(collectionListInfo.getName());
                if (checkName != -1) {
                    return mRes.getString(R.string.error_import);
                }
                mDbAdapter.createAndPopulateNewTable(collectionListInfo, i, collectionContent);
            }

            // Update any imported tables, if necessary
            if (importDatabaseVersion != MainApplication.DATABASE_VERSION) {
                mDbAdapter.upgradeDbForImport(importDatabaseVersion);
            }
        } catch (SQLException e) {
            // Report an import error message to display on the UI thread
            return mRes.getString(R.string.error_import);
        }

        // Success!
        return "";
    }

    /**
     * Exports the collection information to JSON
     *
     * @param outputStream output stream to write to
     * @param filePath     file path being written to
     * @return A message to be displayed to the user, whether successful or not
     */
    public String exportCollectionsToJson(OutputStream outputStream, String filePath) {

        // Get all collection lists from the database
        ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
        mDbAdapter.getAllTables(collectionListEntries);

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, JSON_CHARSET))) {
            writer.beginObject();
            writer.name(JSON_DB_VERSION).value(MainApplication.DATABASE_VERSION);
            writer.name(JSON_COLLECTIONS).beginArray();
            for (int i = 0; i < collectionListEntries.size(); i++) {
                // Add the collection and coin info
                CollectionListInfo collectionListInfo = collectionListEntries.get(i);
                ArrayList<CoinSlot> coinList = mDbAdapter.getCoinList(collectionListInfo.getName(), true);
                collectionListInfo.writeToJson(writer, mDbAdapter, coinList);
            }
            writer.endArray();
            writer.endObject();
            return mRes.getString(R.string.success_export, filePath);
        } catch (IOException e) {
            return mRes.getString(R.string.error_exporting, e.getMessage());
        }
    }

    /**
     * Extract the contents from a CSV file into a 2D list of strings
     *
     * @param inputFile file to read
     * @return 2D list of strings
     * @throws IOException if an error occurs
     */
    private ArrayList<String[]> getCsvFileContents(File inputFile) throws IOException, CsvValidationException {
        // Tell the CSVReader to use the NULL character as the escape
        // character to effectively allow no escape characters
        // (otherwise, '\' is the escape character, and it can be
        // typed by users!)
        CSVParser parser = new CSVParserBuilder().withEscapeChar('\0').build();
        CSVReader csvReader = new CSVReaderBuilder(new FileReader(inputFile))
                .withCSVParser(parser).build();

        ArrayList<String[]> lineList = new ArrayList<>();
        String[] lineValues;
        while (null != (lineValues = csvReader.readNext())) {
            lineList.add(lineValues);
        }
        csvReader.close();
        return lineList;
    }

    /**
     * Writes contents to a CSV file
     *
     * @param file     file to write to
     * @param contents contents to write to file
     * @throws IOException if an error occurs
     */
    private void writeToLegacyCsv(File file, ArrayList<String[]> contents) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
        for (String[] fileLine : contents) {
            csvWriter.writeNext(fileLine);
        }
        csvWriter.close();
    }

    /**
     * This method imports collections from a single CSV file
     *
     * @param inputStream input stream to read from
     * @return "" if successful, otherwise an error message to display
     */
    public String importCollectionsFromSingleCSV(InputStream inputStream) {

        int importDatabaseVersion = 0;
        ArrayList<CollectionListInfo> importedCollectionInfoList = new ArrayList<>();
        ArrayList<ArrayList<CoinSlot>> importedCollectionContents = new ArrayList<>();

        SectionType currSectionType = SectionType.UNKNOWN;
        String[] lineValues;
        ArrayList<CoinSlot> currCoinList = new ArrayList<>();
        int coinIndex = 0;

        // Tell the CSVReader to use the NULL character as the escape
        // character to effectively allow no escape characters
        // (otherwise, '\' is the escape character, and it can be
        // typed by users!)
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                    .withCSVParser(new CSVParserBuilder().withEscapeChar('\0').build()).build()) {

            while (null != (lineValues = csvReader.readNext())) {

                if (lineValues.length == 0) {
                    // Ignore empty lines
                    continue;
                } else if ((lineValues.length >= 2) && lineValues[0].equals(CSV_SEPARATOR)) {
                    // Look for CSV separators which we're using to put multiple files in a single CSV
                    // Make sure any cells following '-----', 'section' are blank, to avoid possible data row
                    boolean foundNonEmptyCell = false;
                    for (int i = 2; i < lineValues.length; i++) {
                        if (!lineValues[i].isEmpty()) {
                            foundNonEmptyCell = true;
                            break;
                        }
                    }
                    if (foundNonEmptyCell) {
                        continue;
                    }
                    currSectionType = SectionType.fromLabel(lineValues[1]);
                    coinIndex = 0;
                    if (currSectionType != SectionType.DATABASE_VERSION) {
                        // Skip the header line, except for the database version (no header for that section)
                        csvReader.readNext();
                    }
                    continue;
                }

                switch (currSectionType) {
                    case DATABASE_VERSION:
                        importDatabaseVersion = Integer.parseInt(lineValues[0]);
                        break;
                    case COLLECTIONS:
                        importedCollectionInfoList.add(new CollectionListInfo(lineValues));
                        currCoinList = new ArrayList<>();
                        importedCollectionContents.add(currCoinList);
                        break;
                    case COIN_LIST:
                        currCoinList.add(new CoinSlot(lineValues, coinIndex++));
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | CsvValidationException e) {
            return mRes.getString(R.string.error_importing, e.getMessage());
        }

        // All data has been parsed from the CSV file so perform the DB steps to import
        return updateDatabaseFromImport(importDatabaseVersion, importedCollectionInfoList,
                importedCollectionContents);
    }

    /**
     * Exports the collection information to a single CSV file
     *
     * @param outputStream output stream to write to
     * @param filePath     file path being written to
     * @return A message to be displayed to the user, whether successful or not
     */
    public String exportCollectionsToSingleCSV(OutputStream outputStream, String filePath) {

        // Get all collection lists from the database
        ArrayList<CollectionListInfo> collectionListEntries = new ArrayList<>();
        mDbAdapter.getAllTables(collectionListEntries);

        try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream))) {

            // Write database version
            csvWriter.writeNext(new String[]{CSV_SEPARATOR, SectionType.DATABASE_VERSION.label});
            // Note: No header row here - this was originally a bug but it seems fine without
            //       it, so making that the long-term behavior
            csvWriter.writeNext(new String[]{String.valueOf(MainApplication.DATABASE_VERSION)});

            // Write collections
            for (int i = 0; i < collectionListEntries.size(); i++) {
                // Get the collection and coin info
                CollectionListInfo collectionListInfo = collectionListEntries.get(i);
                ArrayList<CoinSlot> coinList = mDbAdapter.getCoinList(collectionListInfo.getName(), true);

                csvWriter.writeNext(new String[]{CSV_SEPARATOR, SectionType.COLLECTIONS.label});
                csvWriter.writeNext(CollectionListInfo.getCsvExportHeader());
                csvWriter.writeNext(collectionListInfo.getCsvExportProperties(mDbAdapter));

                csvWriter.writeNext(new String[]{CSV_SEPARATOR, SectionType.COIN_LIST.label});
                csvWriter.writeNext(CoinSlot.getCsvExportHeader());
                for (CoinSlot coinSlot : coinList) {
                    csvWriter.writeNext(coinSlot.getCsvExportProperties());
                }
            }
            return mRes.getString(R.string.success_export, filePath);
        } catch (IOException e) {
            return mRes.getString(R.string.error_exporting, e.getMessage());
        }
    }
}
