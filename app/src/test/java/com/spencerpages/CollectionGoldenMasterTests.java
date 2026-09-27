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

import static com.spencerpages.MainApplication.COLLECTION_TYPES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.res.Resources;

import androidx.test.core.app.ApplicationProvider;

import com.coincollection.CoinPageCreator;
import com.coincollection.CoinSlot;
import com.coincollection.CollectionInfo;
import com.coincollection.helper.ParcelableHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Golden-master snapshots of every collection type: the coins each one creates
 * under several parameter combinations, the images they display, and the
 * collection's image id table. Each type has a checked-in fixture under
 * app/src/test/data/golden-master/, and this test regenerates the snapshot in
 * memory and requires it to match exactly.
 *
 * <p>A change to any collection's output therefore shows up as a fixture diff.
 * When the change is intended (new coins, a data fix), regenerate with
 * {@link #UPDATE_COMMAND} and review the diff: every changed line is a change
 * users will see in newly created collections. Existing collections only get
 * it through an onCollectionDatabaseUpgrade() block.
 *
 * <p>Regenerating still refuses two changes that would corrupt user databases:
 * moving a type to another index in COLLECTION_TYPES, and changing or removing
 * an existing entry of a collection's image id table (both are append-only).
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class CollectionGoldenMasterTests extends BaseTestCase {

    // Gradle test runner CWD is the app/ directory
    private static final String FIXTURE_DIR = "src/test/data/golden-master";

    static final String UPDATE_COMMAND = "./gradlew testAndroidDebugUnitTest"
            + " --tests \"com.spencerpages.CollectionGoldenMasterTests\" -PupdateGoldenMasters";

    // Set by passing -PupdateGoldenMasters to Gradle (see app/build.gradle)
    private static final boolean UPDATE_FIXTURES = Boolean.getBoolean("updateGoldenMasters");

    private static final String INDEX_PREFIX = "index: ";
    private static final String IMAGE_IDS_HEADER = "imageIds (append-only):";
    private static final String NO_RESOURCE = "<none>";

    private final CollectionInfo mCollectionInfo;
    private final String mTypeName;

    public CollectionGoldenMasterTests(CollectionInfo collectionInfo, String typeName) {
        mCollectionInfo = collectionInfo;
        mTypeName = typeName;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
    public static List<Object[]> getCollectionTypes() {
        List<Object[]> types = new ArrayList<>();
        for (CollectionInfo collectionInfo : COLLECTION_TYPES) {
            types.add(new Object[]{collectionInfo, collectionInfo.getClass().getSimpleName()});
        }
        return types;
    }

    /**
     * Compare this collection type's output against its golden master, or
     * rewrite the golden master when updating
     */
    @Test
    public void test_matchesGoldenMaster() throws IOException {
        String actual = render();
        File fixture = new File(FIXTURE_DIR, mTypeName + ".txt");
        String expected = fixture.exists() ? readFixture(fixture) : null;

        // Enforced even when updating, so these can't be approved by accident
        if (expected != null) {
            assertEquals(mTypeName + " moved in MainApplication.COLLECTION_TYPES, which is"
                            + " append-only because indexes are stored in user databases",
                    findLine(expected, INDEX_PREFIX), findLine(actual, INDEX_PREFIX));
            List<String> expectedImageIds = getImageIdLines(expected);
            List<String> actualImageIds = getImageIdLines(actual);
            assertTrue(mTypeName + " changed or removed image ids, which are append-only"
                            + " because indexes are stored in user databases.\nWas: "
                            + expectedImageIds + "\nNow: " + actualImageIds,
                    actualImageIds.size() >= expectedImageIds.size()
                            && actualImageIds.subList(0, expectedImageIds.size()).equals(expectedImageIds));
        }

        if (UPDATE_FIXTURES) {
            if (!actual.equals(expected)) {
                new File(FIXTURE_DIR).mkdirs();
                Files.write(fixture.toPath(), actual.getBytes(StandardCharsets.UTF_8));
            }
            return;
        }
        assertNotNull("No golden master for " + mTypeName + ". Create it with:\n"
                + UPDATE_COMMAND, expected);
        assertEquals(mTypeName + " output changed. If intended, regenerate with:\n"
                + UPDATE_COMMAND + "\nand review the fixture diff.", expected, actual);
    }

    /**
     * Render the full snapshot for this collection type
     *
     * @return snapshot text
     */
    private String render() {
        Resources res = ApplicationProvider.getApplicationContext().getResources();
        StringBuilder out = new StringBuilder();
        out.append("# Golden master for ").append(mCollectionInfo.getClass().getName()).append('\n')
                .append("# Generated by CollectionGoldenMasterTests. Don't edit by hand; to update run\n")
                .append("#   ").append(UPDATE_COMMAND).append('\n')
                .append("# and review the diff. Resources are listed by name.\n")
                .append("# Coin columns: sortOrder, identifier, mint, imageId, image, image ignoring imageId\n")
                .append('\n')
                .append("type: ").append(mCollectionInfo.getCoinType()).append('\n')
                .append(INDEX_PREFIX).append(MainApplication.getIndexFromCollectionClass(mCollectionInfo.getClass())).append('\n')
                .append("startYear: ").append(mCollectionInfo.getStartYear()).append('\n')
                .append("stopYear: ").append(mCollectionInfo.getStopYear()).append('\n')
                .append("listImage: ").append(resourceName(res, mCollectionInfo.getCoinImageIdentifier())).append('\n')
                .append("attribution: ").append(resourceName(res, mCollectionInfo.getAttributionResId())).append('\n')
                .append('\n')
                .append(IMAGE_IDS_HEADER).append('\n');
        Object[][] imageIds = mCollectionInfo.getImageIds();
        for (int i = 0; i < imageIds.length; i++) {
            out.append(i);
            for (Object value : imageIds[i]) {
                out.append('\t').append(value instanceof Integer
                        ? resourceName(res, (Integer) value) : escape(String.valueOf(value)));
            }
            out.append('\n');
        }

        renderCoins(out, res, "default", getParameters(), false);
        ParcelableHashMap allEnabled = getParameters();
        setBooleans(allEnabled, true, false);
        renderCoins(out, res, "all enabled", allEnabled, false);
        ParcelableHashMap alternatingA = getParameters();
        setBooleans(alternatingA, false, true);
        renderCoins(out, res, "alternating A", alternatingA, false);
        ParcelableHashMap alternatingB = getParameters();
        setBooleans(alternatingB, true, true);
        renderCoins(out, res, "alternating B", alternatingB, false);
        ParcelableHashMap narrowYears = getParameters();
        setBooleans(narrowYears, true, false);
        renderCoins(out, res, "all enabled, narrowed years", narrowYears, true);
        return out.toString();
    }

    /**
     * @return a fresh copy of this collection type's default creation parameters
     */
    private ParcelableHashMap getParameters() {
        ParcelableHashMap parameters = new ParcelableHashMap();
        mCollectionInfo.getCreationParameters(parameters);
        return parameters;
    }

    /**
     * Set every Boolean parameter, visiting the keys in sorted order so the
     * result doesn't depend on HashMap iteration order
     *
     * @param parameters parameters to change
     * @param first      value for the first key
     * @param alternate  whether to alternate the value between keys
     */
    private static void setBooleans(ParcelableHashMap parameters, boolean first, boolean alternate) {
        boolean value = first;
        for (String key : new TreeMap<>(parameters).keySet()) {
            if (parameters.get(key) instanceof Boolean) {
                parameters.put(key, value);
                if (alternate) {
                    value = !value;
                }
            }
        }
    }

    /**
     * Render one parameter combination and the coins it creates
     *
     * @param out         output to append to
     * @param res         resources, for naming resource ids
     * @param title       section title
     * @param parameters  creation parameters
     * @param narrowYears whether to narrow the date range by a year at each end;
     *                    the section is skipped if the collection has no date
     *                    range wide enough to narrow
     */
    private void renderCoins(StringBuilder out, Resources res, String title,
                             ParcelableHashMap parameters, boolean narrowYears) {
        if (narrowYears) {
            Object startYear = parameters.get(CoinPageCreator.OPT_START_YEAR);
            Object stopYear = parameters.get(CoinPageCreator.OPT_STOP_YEAR);
            if (!(startYear instanceof Integer) || !(stopYear instanceof Integer)
                    || (Integer) stopYear - (Integer) startYear < 2) {
                return;
            }
            parameters.put(CoinPageCreator.OPT_START_YEAR, (Integer) startYear + 1);
            parameters.put(CoinPageCreator.OPT_STOP_YEAR, (Integer) stopYear - 1);
        }

        out.append('\n').append("== ").append(title).append(" ==").append('\n');
        for (Map.Entry<String, Object> entry : new TreeMap<>(parameters).entrySet()) {
            Object value = entry.getValue();
            out.append(entry.getKey()).append(" = ")
                    .append(entry.getKey().endsWith("StringId") && value instanceof Integer
                            ? resourceName(res, (Integer) value) : String.valueOf(value))
                    .append('\n');
        }

        ArrayList<CoinSlot> coinList = new ArrayList<>();
        mCollectionInfo.populateCollectionLists(parameters, coinList);
        out.append("coins: ").append(coinList.size()).append('\n');
        for (CoinSlot coin : coinList) {
            out.append(coin.getSortOrder())
                    .append('\t').append(escape(coin.getIdentifier()))
                    .append('\t').append(escape(coin.getMint()))
                    .append('\t').append(coin.getImageId())
                    .append('\t').append(resourceName(res, mCollectionInfo.getCoinSlotImage(coin, false)))
                    .append('\t').append(resourceName(res, mCollectionInfo.getCoinSlotImage(coin, true)))
                    .append('\n');
        }
    }

    /**
     * Name a resource id, since the ids themselves change between builds
     *
     * @param res resources
     * @param id  resource id
     * @return the resource's entry name, or a placeholder if it isn't one
     */
    private static String resourceName(Resources res, int id) {
        try {
            return res.getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
            return NO_RESOURCE;
        }
    }

    /**
     * Escape a value written into a tab-separated line. Mints can span two
     * lines (Ex: "P\nPeace Medal")
     *
     * @param field value to escape
     * @return the value with backslashes, tabs and line breaks escaped
     */
    private static String escape(String field) {
        return field.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String readFixture(File fixture) throws IOException {
        // Tolerate CRLF line endings from a Windows checkout
        return new String(Files.readAllBytes(fixture.toPath()), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
    }

    private static String findLine(String snapshot, String prefix) {
        for (String line : snapshot.split("\n")) {
            if (line.startsWith(prefix)) {
                return line;
            }
        }
        return null;
    }

    private static List<String> getImageIdLines(String snapshot) {
        List<String> lines = Arrays.asList(snapshot.split("\n"));
        int start = lines.indexOf(IMAGE_IDS_HEADER) + 1;
        int end = start;
        while (end < lines.size() && !lines.get(end).isEmpty()) {
            end++;
        }
        return new ArrayList<>(lines.subList(start, end));
    }
}
