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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Generator for the V1 JSON fixture files used by CollectionUpgradeV1FixtureTests.
 *
 * <p>The fixtures hold the coin lists that the first public version of this codebase
 * (commit {@value #V1_COMMIT}, app 2.2.1, database version 8) created for each of its 25
 * collection types. Rather than re-typing those lists, this generator reads that version's
 * {@code CoinPageCreator.java} and resource arrays out of git history, compiles its
 * {@code makeTable()} / {@code make*Table()} methods unchanged against a small stand-in for
 * the activity state, and runs them under a handful of creation-option sets that the V1
 * creation screen allowed.
 *
 * <p>The fixtures are frozen: they describe what real V1 users have in their databases, so
 * they must never change as the current code changes. This generator exists so that anyone
 * can reproduce them from the V1 source and confirm they are authentic. To run it, remove
 * the {@code @Ignore} locally (it needs {@code git} on the PATH and a clone that contains
 * {@value #V1_COMMIT}) and run:
 * <pre>
 * ./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.GenerateV1Fixtures"
 * </pre>
 * The output should be byte-identical to the committed files.
 *
 * <p>Output directory: app/src/test/data/v1-upgrades/
 *
 * <p>Generated files (V1 options only apply where the V1 creation screen showed them):
 * <ul>
 *   <li>v1-default.json — every type with the creation screen's defaults</li>
 *   <li>v1-all-options.json — mint marks on with every offered mint, territories and
 *       burnished coins on</li>
 *   <li>v1-split-A.json — mint marks on with D and O only, territories off, burnished on</li>
 *   <li>v1-split-B.json — mint marks on with P, S and CC only, territories on,
 *       burnished off</li>
 *   <li>v1-date-range.json — no mint marks, date range narrowed by a year at each end</li>
 * </ul>
 */
@Ignore("Run manually to regenerate the V1 fixture files")
public class GenerateV1Fixtures {

    /** The first commit in the repository, which is version 2.2.1 of the app */
    static final String V1_COMMIT = "54e6538";

    /** Database version used by the V1 code */
    static final int V1_DATABASE_VERSION = 8;

    // Under app/src/test/data
    private static final String OUTPUT_DIR = "v1-upgrades";

    private static final String V1_SOURCE_PATH =
            "app/src/main/java/com/spencerpages/CoinPageCreator.java";
    private static final String V1_RES_PATH = "app/src/main/res/values/";
    private static final String[] V1_RES_FILES = {
            "strings.xml", "penny_lists.xml", "nickel_lists.xml", "quarter_lists.xml",
            "dollar_lists.xml", "misc_lists.xml"};

    private static final String GENERATED_CLASS = "V1CoinPageCreator";

    /** A set of V1 creation-screen options (the CoinPageCreator instance variables) */
    static final class V1Options {
        boolean showMintMark;
        boolean editDateRange;
        boolean showTerritories;
        boolean showBurnished;
        boolean showP;
        boolean showD;
        boolean showS;
        boolean showO;
        boolean showCC;
        int startYear;
        int stopYear;
    }

    /** Which V1 creation-screen options were visible for a coin type */
    static final class V1Screen {
        final String coinType;
        final int firstYear;
        final int lastYear;

        V1Screen(String coinType, int firstYear, int lastYear) {
            this.coinType = coinType;
            this.firstYear = firstYear;
            this.lastYear = lastYear;
        }

        // Mirrors V1 CoinPageCreator.resetView(String) and updateViewFromState()
        boolean offersMintMarks() {
            return !coinType.equals("First Spouse Gold Coins")
                    && !coinType.equals("American Eagle Silver Dollars");
        }

        boolean offersDateRange() {
            switch (coinType) {
                case "State Quarters":
                case "National Park Quarters":
                case "Presidential Dollars":
                case "First Spouse Gold Coins":
                case "American Eagle Silver Dollars":
                    return false;
                default:
                    return true;
            }
        }

        boolean offersO() {
            return coinType.equals("Barber Dimes") || coinType.equals("Morgan Dollars")
                    || coinType.equals("Barber Quarters") || coinType.equals("Barber Half Dollars");
        }

        boolean offersCC() {
            return coinType.equals("Morgan Dollars");
        }

        boolean offersTerritories() {
            return coinType.equals("State Quarters");
        }

        boolean offersBurnished() {
            return coinType.equals("American Eagle Silver Dollars");
        }

        /** The options after V1 CoinPageCreator.resetView(String) */
        V1Options defaults() {
            V1Options options = new V1Options();
            options.showTerritories = true;
            options.startYear = firstYear;
            options.stopYear = lastYear;
            return options;
        }
    }

    @FunctionalInterface
    interface Scenario {
        V1Options apply(V1Screen screen);
    }

    @Test
    public void generateAllFixtures() throws Exception {
        Map<String, String[]> stringArrays = new LinkedHashMap<>();
        Map<String, int[]> intArrays = new LinkedHashMap<>();
        for (String resFile : V1_RES_FILES) {
            parseResourceArrays(gitShow(V1_RES_PATH + resFile), stringArrays, intArrays);
        }
        String[] coinTypes = stringArrays.get("types_of_coins");
        int[] firstYears = intArrays.get("year_of_first_production");
        int[] lastYears = intArrays.get("year_of_most_recent_production");
        assertNotNull(coinTypes);
        assertNotNull(firstYears);
        assertNotNull(lastYears);
        assertEquals(coinTypes.length, firstYears.length);
        assertEquals(coinTypes.length, lastYears.length);

        List<V1Screen> screens = new ArrayList<>();
        for (int i = 0; i < coinTypes.length; i++) {
            screens.add(new V1Screen(coinTypes[i], firstYears[i], lastYears[i]));
        }

        Class<?> creatorClass = compileV1Creator(stringArrays);

        BaseTestCase.getTestDataSourceFile(OUTPUT_DIR).mkdirs();
        writeFixture(creatorClass, screens, "v1-default.json", V1Screen::defaults);
        writeFixture(creatorClass, screens, "v1-all-options.json", screen -> {
            V1Options options = screen.defaults();
            options.showMintMark = screen.offersMintMarks();
            options.showP = options.showMintMark;
            options.showD = options.showMintMark;
            options.showS = options.showMintMark;
            options.showO = options.showMintMark && screen.offersO();
            options.showCC = options.showMintMark && screen.offersCC();
            options.showBurnished = screen.offersBurnished();
            return options;
        });
        writeFixture(creatorClass, screens, "v1-split-A.json", screen -> {
            V1Options options = screen.defaults();
            options.showMintMark = screen.offersMintMarks();
            options.showD = options.showMintMark;
            options.showO = options.showMintMark && screen.offersO();
            options.showTerritories = !screen.offersTerritories();
            options.showBurnished = screen.offersBurnished();
            return options;
        });
        writeFixture(creatorClass, screens, "v1-split-B.json", screen -> {
            V1Options options = screen.defaults();
            options.showMintMark = screen.offersMintMarks();
            options.showP = options.showMintMark;
            options.showS = options.showMintMark;
            options.showCC = options.showMintMark && screen.offersCC();
            return options;
        });
        writeFixture(creatorClass, screens, "v1-date-range.json", screen -> {
            V1Options options = screen.defaults();
            if (screen.offersDateRange()) {
                options.editDateRange = true;
                options.startYear = screen.firstYear + 1;
                options.stopYear = screen.lastYear - 1;
            }
            return options;
        });
    }

    /**
     * Runs the V1 code for every coin type under one scenario and writes the fixture
     */
    private void writeFixture(Class<?> creatorClass, List<V1Screen> screens, String filename,
                              Scenario scenario) throws Exception {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"source\": ").append(quote("Generated by GenerateV1Fixtures from commit "
                + V1_COMMIT + " (app 2.2.1). Frozen - never regenerate against newer code.")).append(",\n");
        json.append("  \"databaseVersion\": ").append(V1_DATABASE_VERSION).append(",\n");
        json.append("  \"collections\": [\n");
        for (int i = 0; i < screens.size(); i++) {
            V1Screen screen = screens.get(i);
            V1Options options = scenario.apply(screen);
            validateOptions(screen, options);
            // V1 only required one mint to be checked, so some option sets give an empty
            // collection (e.g. Indian Head Cents with only D) - those are kept, as V1 made them
            List<String[]> coins = runV1Creator(creatorClass, screen.coinType, options);

            json.append("    {\n");
            json.append("      \"name\": ").append(quote(screen.coinType)).append(",\n");
            json.append("      \"coinType\": ").append(quote(screen.coinType)).append(",\n");
            json.append("      \"v1Options\": {")
                    .append("\"showMintMark\": ").append(options.showMintMark)
                    .append(", \"showP\": ").append(options.showP)
                    .append(", \"showD\": ").append(options.showD)
                    .append(", \"showS\": ").append(options.showS)
                    .append(", \"showO\": ").append(options.showO)
                    .append(", \"showCC\": ").append(options.showCC)
                    .append(", \"showTerritories\": ").append(options.showTerritories)
                    .append(", \"showBurnished\": ").append(options.showBurnished)
                    .append(", \"editDateRange\": ").append(options.editDateRange)
                    .append(", \"startYear\": ").append(options.startYear)
                    .append(", \"stopYear\": ").append(options.stopYear)
                    .append("},\n");
            json.append("      \"coins\": [\n");
            for (int j = 0; j < coins.size(); j++) {
                String[] coin = coins.get(j);
                json.append("        [").append(quote(coin[0])).append(", ").append(quote(coin[1])).append("]");
                json.append(j < coins.size() - 1 ? ",\n" : "\n");
            }
            json.append("      ]\n");
            json.append(i < screens.size() - 1 ? "    },\n" : "    }\n");
        }
        json.append("  ]\n");
        json.append("}\n");

        File outputFile = BaseTestCase.getTestDataSourceFile(OUTPUT_DIR + "/" + filename);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(outputFile.toPath()),
                StandardCharsets.UTF_8)) {
            writer.write(json.toString());
        }
    }

    /**
     * Applies the checks that the V1 "Create Collection" button made before creating a
     * collection, so that the fixtures only contain collections a V1 user could have made
     */
    private static void validateOptions(V1Screen screen, V1Options options) {
        if (options.showMintMark) {
            assertTrue(screen.coinType, screen.offersMintMarks());
            assertTrue(screen.coinType,
                    options.showP || options.showD || options.showS || options.showO || options.showCC);
        }
        if (options.editDateRange) {
            assertTrue(screen.coinType, screen.offersDateRange());
            assertTrue(screen.coinType, options.startYear >= screen.firstYear);
            assertTrue(screen.coinType, options.stopYear <= screen.lastYear);
            assertTrue(screen.coinType, options.startYear <= options.stopYear);
        } else {
            assertEquals(screen.coinType, screen.firstYear, options.startYear);
            assertEquals(screen.coinType, screen.lastYear, options.stopYear);
        }
    }

    /**
     * Sets the V1 CoinPageCreator's instance variables and calls its makeTable()
     *
     * @return the coins created, as {identifier, mint} pairs
     */
    private static List<String[]> runV1Creator(Class<?> creatorClass, String coinType,
                                               V1Options options) throws Exception {
        Object creator = creatorClass.getDeclaredConstructor().newInstance();
        setField(creator, "mShowMintMark", options.showMintMark);
        setField(creator, "mEditDateRange", options.editDateRange);
        setField(creator, "mShowTerritories", options.showTerritories);
        setField(creator, "mShowBurnished", options.showBurnished);
        setField(creator, "mShowP", options.showP);
        setField(creator, "mShowD", options.showD);
        setField(creator, "mShowS", options.showS);
        setField(creator, "mShowO", options.showO);
        setField(creator, "mShowCC", options.showCC);
        setField(creator, "mStartYear", options.startYear);
        setField(creator, "mStopYear", options.stopYear);
        creatorClass.getMethod("makeTable", String.class).invoke(creator, coinType);

        List<?> identifiers = (List<?>) getField(creator, "mIdentifierList");
        List<?> mints = (List<?>) getField(creator, "mMintList");
        assertEquals(coinType, identifiers.size(), mints.size());
        List<String[]> coins = new ArrayList<>();
        for (int i = 0; i < identifiers.size(); i++) {
            coins.add(new String[]{(String) identifiers.get(i), (String) mints.get(i)});
        }
        return coins;
    }

    private static void setField(Object obj, String name, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private static Object getField(Object obj, String name) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * Compiles V1's makeTable() and make*Table() methods, verbatim, inside a class that
     * supplies the instance variables and resource lookups they use
     */
    private static Class<?> compileV1Creator(Map<String, String[]> stringArrays) throws Exception {
        String v1Source = gitShow(V1_SOURCE_PATH);
        int start = v1Source.indexOf("    public void makeTable(String coinType){");
        int end = v1Source.indexOf("    @Override\n    public boolean onOptionsItemSelected");
        assertTrue("makeTable() not found in V1 source", start >= 0);
        assertTrue("End of the make*Table() methods not found in V1 source", end > start);
        String v1Methods = v1Source.substring(start, end);

        StringBuilder src = new StringBuilder();
        src.append("import java.util.ArrayList;\n");
        src.append("public class ").append(GENERATED_CLASS).append(" {\n");
        src.append("    static final class R {\n        static final class array {\n");
        List<String> arrayNames = new ArrayList<>(stringArrays.keySet());
        for (int i = 0; i < arrayNames.size(); i++) {
            src.append("            static final int ").append(arrayNames.get(i))
                    .append(" = ").append(i).append(";\n");
        }
        src.append("        }\n    }\n");
        src.append("    static final String[][] STRING_ARRAYS = {\n");
        for (String arrayName : arrayNames) {
            src.append("        {");
            for (String item : stringArrays.get(arrayName)) {
                src.append(quote(item)).append(", ");
            }
            src.append("},\n");
        }
        src.append("    };\n");
        src.append("    static final class Resources {\n");
        src.append("        String[] getStringArray(int id) { return STRING_ARRAYS[id]; }\n");
        src.append("    }\n");
        src.append("    static final class Context {\n");
        src.append("        Resources getResources() { return new Resources(); }\n");
        src.append("    }\n");
        src.append("    private final Context mContext = new Context();\n");
        src.append("    private boolean mShowMintMark;\n");
        src.append("    private boolean mEditDateRange;\n");
        src.append("    private boolean mShowTerritories;\n");
        src.append("    private boolean mShowBurnished;\n");
        src.append("    private boolean mShowP;\n");
        src.append("    private boolean mShowD;\n");
        src.append("    private boolean mShowS;\n");
        src.append("    private boolean mShowO;\n");
        src.append("    private boolean mShowCC;\n");
        src.append("    private int mStartYear;\n");
        src.append("    private int mStopYear;\n");
        src.append("    private final ArrayList<String> mIdentifierList = new ArrayList<>();\n");
        src.append("    private final ArrayList<String> mMintList = new ArrayList<>();\n");
        src.append(v1Methods);
        src.append("}\n");

        File workDir = Files.createTempDirectory("v1-creator").toFile();
        File sourceFile = new File(workDir, GENERATED_CLASS + ".java");
        Files.write(sourceFile.toPath(), src.toString().getBytes(StandardCharsets.UTF_8));

        // javax.tools isn't on the Android unit-test compile classpath, so run the javac that
        // ships with the JDK running the tests
        File javaBin = new File(System.getProperty("java.home"), "bin");
        File javac = new File(javaBin, "javac.exe").exists()
                ? new File(javaBin, "javac.exe") : new File(javaBin, "javac");
        assertTrue("A JDK is required to compile the V1 source: " + javac, javac.exists());
        String compileOutput = runProcess(javac.getPath(), "-encoding", "UTF-8", "-nowarn",
                "-d", workDir.getPath(), sourceFile.getPath());
        assertNotNull("V1 source failed to compile:\n" + compileOutput, compileOutput);

        URLClassLoader loader = new URLClassLoader(new URL[]{workDir.toURI().toURL()},
                GenerateV1Fixtures.class.getClassLoader());
        return loader.loadClass(GENERATED_CLASS);
    }

    /**
     * Reads the <string-array> and <integer-array> resources out of an Android values file
     */
    private static void parseResourceArrays(String xml, Map<String, String[]> stringArrays,
                                            Map<String, int[]> intArrays) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        NodeList stringArrayNodes = doc.getElementsByTagName("string-array");
        for (int i = 0; i < stringArrayNodes.getLength(); i++) {
            Element array = (Element) stringArrayNodes.item(i);
            NodeList items = array.getElementsByTagName("item");
            String[] values = new String[items.getLength()];
            for (int j = 0; j < items.getLength(); j++) {
                values[j] = decodeAndroidString(items.item(j).getTextContent());
            }
            stringArrays.put(array.getAttribute("name"), values);
        }

        NodeList intArrayNodes = doc.getElementsByTagName("integer-array");
        for (int i = 0; i < intArrayNodes.getLength(); i++) {
            Element array = (Element) intArrayNodes.item(i);
            NodeList items = array.getElementsByTagName("item");
            int[] values = new int[items.getLength()];
            for (int j = 0; j < items.getLength(); j++) {
                values[j] = Integer.parseInt(items.item(j).getTextContent().trim());
            }
            intArrays.put(array.getAttribute("name"), values);
        }
    }

    /**
     * Applies aapt's string-resource rules: whitespace runs collapse to one space outside
     * double quotes, unescaped double quotes are dropped, and backslash escapes resolve
     */
    private static String decodeAndroidString(String raw) {
        StringBuilder out = new StringBuilder();
        boolean inQuotes = false;
        boolean lastWasSpace = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\\' && i + 1 < raw.length()) {
                char next = raw.charAt(++i);
                switch (next) {
                    case 'n':
                        out.append('\n');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    default:
                        out.append(next);
                        break;
                }
                lastWasSpace = false;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && Character.isWhitespace(c)) {
                if (!lastWasSpace) {
                    out.append(' ');
                    lastWasSpace = true;
                }
            } else {
                out.append(c);
                lastWasSpace = false;
            }
        }
        return out.toString().trim();
    }

    /**
     * Returns the contents of a file at the V1 commit
     */
    private static String gitShow(String path) throws IOException, InterruptedException {
        // The CWD is app/, and git resolves <commit>:<path> from the repository root
        String contents = runProcess("git", "show", V1_COMMIT + ":" + path);
        assertNotNull("git show failed for " + path, contents);
        // Normalize line endings in case git converts them on this platform
        return contents.replace("\r\n", "\n");
    }

    /**
     * Runs a command and returns its output, or null if it exits with an error (after
     * printing the output)
     */
    static String runProcess(String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        byte[] output;
        try (InputStream in = process.getInputStream()) {
            output = in.readAllBytes();
        }
        String contents = new String(output, StandardCharsets.UTF_8);
        if (process.waitFor() != 0) {
            System.err.println(contents);
            return null;
        }
        return contents;
    }

    /**
     * Quotes a string as a JSON (and Java) string literal
     */
    static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                    break;
            }
        }
        return out.append('"').toString();
    }
}
