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
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Generator for the upgrade-path fixtures used by UpgradePathTests.
 *
 * <p>For every database version that shipped since collections became CollectionInfo classes
 * (10 onward), the fixtures hold the collections that version of the app created: every
 * collection type under a set of creation options, with the coins and the collection_info
 * values that version stored. UpgradePathTests loads them into a database with that version's
 * schema and runs the current upgrade code on it.
 *
 * <p>Rather than re-typing old coin lists, this generator reads each version's collection
 * classes out of git history and runs them. It compiles their creation code unchanged, with
 * stand-ins for the classes it needs from the rest of the app:
 * <ul>
 *   <li>members that touch the database (the upgrade methods) are removed, since only
 *       creation is needed;</li>
 *   <li>{@code R}, {@code CollectionListInfo}, {@code CoinPageCreator} and
 *       {@code MainApplication} are replaced by classes holding only their constants, plus
 *       {@code CoinPageCreator}'s static methods that turn creation options into the stored
 *       flags;</li>
 *   <li>packages are renamed so the old classes can't collide with the current ones.</li>
 * </ul>
 *
 * <p>Option sets per collection type (see {@link #buildOptionSets}): the defaults, everything
 * on, everything off, and a pairwise set in which every pair of options appears in each
 * on/off combination the creation screen allowed. Turning on the custom date range narrows
 * the range by a year at each end.
 *
 * <p>The fixtures are frozen: they describe what users of each version have in their
 * databases, so they must never change as the current code changes. This generator exists so
 * that anyone can reproduce them from history and confirm they are authentic. To run it,
 * remove the {@code @Ignore} locally (it needs {@code git} on the PATH and a full clone) and
 * run:
 * <pre>
 * ./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.GenerateUpgradeFixtures"
 * </pre>
 * The output should be byte-identical to the committed files.
 *
 * <p>Output: app/src/test/data/upgrade-paths/v{databaseVersion}.json.gz
 */
@Ignore("Run manually to regenerate the upgrade-path fixture files")
@RunWith(RobolectricTestRunner.class)
public class GenerateUpgradeFixtures {

    /** A shipped version of the app, identified by its database version */
    static final class Snapshot {
        final int databaseVersion;
        final String commit;
        final String release;

        Snapshot(int databaseVersion, String commit, String release) {
            this.databaseVersion = databaseVersion;
            this.commit = commit;
            this.release = release;
        }
    }

    /**
     * Database versions 10-16 shipped before releases were tagged, so they use the last
     * commit before the next version bump. Later versions use the last release tag at that
     * version. Versions 21 and 22 were never released. The full commit hashes pin the
     * history exactly; DevSkim takes 40-character hex strings for secrets, hence the ignores.
     */
    static final Snapshot[] SNAPSHOTS = {
            new Snapshot(10, "b5367edaea1150d35391efc929695a2bcff663ae", null), // DevSkim: ignore DS173237
            new Snapshot(11, "1bfeb4f20570b749b23599e60cb0d8de8a7f371b", null), // DevSkim: ignore DS173237
            new Snapshot(12, "1e59d82c11bd758220077dec478be9fca2864e6a", null), // DevSkim: ignore DS173237
            new Snapshot(13, "8bc7124553cc20f32fac6a2e0dea33b466971b25", null), // DevSkim: ignore DS173237
            new Snapshot(14, "e99f64a2d802eb5f70c35e669584f85733430b70", null), // DevSkim: ignore DS173237
            new Snapshot(15, "8d9cdb45960c35176d236c58168d6b50ae447fe1", null), // DevSkim: ignore DS173237
            new Snapshot(16, "aa5cc0aff897f994cc0400bcb9dc5024435033cd", null), // DevSkim: ignore DS173237
            new Snapshot(17, "5680f18b44f064d138047daba06fe979de6827f4", "v3.3.1"), // DevSkim: ignore DS173237
            new Snapshot(18, "ed94143707fbee5148c2ec88df2dd34b005a093e", "v3.4.0"), // DevSkim: ignore DS173237
            new Snapshot(19, "9d90fb68f63e52982d931d16a41594c8cfd741e8", "v3.5.1"), // DevSkim: ignore DS173237
            new Snapshot(20, "54559bf9afe08a87721e7e6f31319eb43fa1581d", "v3.6.0"), // DevSkim: ignore DS173237
            new Snapshot(23, "39ae82de8f36971dc8cb331f19c995471f070244", "v3.7.4"), // DevSkim: ignore DS173237
            new Snapshot(24, "ddb837a76c3b25e25d0ca26acbdbbac2d859ef84", "v3.8.4"), // DevSkim: ignore DS173237
    };

    /** The first database version that stored the creation options in collection_info */
    static final int FIRST_VERSION_WITH_STORED_OPTIONS = 15;

    // Under app/src/test/data
    static final String OUTPUT_DIR = "upgrade-paths";

    private static final String SRC_ROOT = "app/src/main/java/";
    private static final String COLLECTIONS_DIR = SRC_ROOT + "com/spencerpages/collections/";
    private static final String CORE_DIR = SRC_ROOT + "com/coincollection/";

    /** Identifiers that only the database-upgrade code uses */
    private static final Pattern DATABASE_CODE = Pattern.compile(
            "\\b(SQLiteDatabase|DatabaseHelper|ContentValues|runSqlUpdate|runSqlDelete|runSqlInsert)\\b");

    private static final Pattern DATABASE_IMPORT = Pattern.compile(
            "(?m)^import\\s+(static\\s+)?(android\\.database\\.|android\\.content\\.ContentValues"
                    + "|com\\.coincollection\\.DatabaseHelper).*;\\s*$\\n?");

    /** A static field whose value is a primitive or String constant */
    private static final Pattern CONSTANT_FIELD = Pattern.compile(
            "^\\s*(public\\s+|protected\\s+|private\\s+)?(final\\s+static|static\\s+final)\\s+"
                    + "(long|int|boolean|String|Long|Integer|Boolean)\\s+\\w+\\s*=[^;]*;\\s*$", Pattern.DOTALL);

    private static final Pattern R_REFERENCE = Pattern.compile("\\bR\\.(\\w+)\\.(\\w+)\\b");

    @Test
    public void generateAllFixtures() throws Exception {
        BaseTestCase.getTestDataSourceFile(OUTPUT_DIR).mkdirs();
        for (Snapshot snapshot : SNAPSHOTS) {
            generateFixture(snapshot);
        }
    }

    // ---------------------------------------------------------------------------------
    // Compiling a snapshot

    /** A snapshot's compiled classes */
    private static final class CompiledSnapshot {
        final ClassLoader loader;
        final String packagePrefix;
        final List<String> collectionClassNames;
        final Map<Integer, String> resourceNames;

        CompiledSnapshot(ClassLoader loader, String packagePrefix, List<String> collectionClassNames,
                         Map<Integer, String> resourceNames) {
            this.loader = loader;
            this.packagePrefix = packagePrefix;
            this.collectionClassNames = collectionClassNames;
            this.resourceNames = resourceNames;
        }

        Class<?> load(String originalName) throws ClassNotFoundException {
            return loader.loadClass(renamePackages(originalName, packagePrefix));
        }
    }

    private static String renamePackages(String source, String packagePrefix) {
        return source.replace("com.spencerpages", packagePrefix + ".spencerpages")
                .replace("com.coincollection", packagePrefix + ".coincollection");
    }

    private static CompiledSnapshot compileSnapshot(Snapshot snapshot) throws Exception {
        String packagePrefix = "upgradefixtures.v" + snapshot.databaseVersion;
        Map<String, String> sources = new LinkedHashMap<>();   // original class name -> source
        List<String> collectionClassNames = new ArrayList<>();

        for (String path : gitListFiles(snapshot.commit, COLLECTIONS_DIR)) {
            if (!path.endsWith(".java")) {
                continue;
            }
            String className = "com.spencerpages.collections." + baseName(path);
            sources.put(className, withoutDatabaseCode(gitShow(snapshot.commit, path), baseName(path)));
            collectionClassNames.add(className);
        }
        sources.put("com.coincollection.CollectionInfo",
                withoutDatabaseCode(gitShow(snapshot.commit, CORE_DIR + "CollectionInfo.java"), "CollectionInfo"));
        String coinSlotPath = CORE_DIR + "CoinSlot.java";
        if (gitFileExists(snapshot.commit, coinSlotPath)) {
            sources.put("com.coincollection.CoinSlot", gitShow(snapshot.commit, coinSlotPath));
        }
        sources.put("com.coincollection.CollectionListInfo", constantsOnly(
                gitShow(snapshot.commit, CORE_DIR + "CollectionListInfo.java"),
                "com.coincollection", "CollectionListInfo", false));
        sources.put("com.coincollection.CoinPageCreator", constantsOnly(
                gitShow(snapshot.commit, CORE_DIR + "CoinPageCreator.java"),
                "com.coincollection", "CoinPageCreator", true));
        sources.put("com.spencerpages.MainApplication", constantsOnly(
                gitShow(snapshot.commit, SRC_ROOT + "com/spencerpages/MainApplication.java"),
                "com.spencerpages", "MainApplication", false));
        Map<Integer, String> resourceNames = new HashMap<>();
        sources.put("com.spencerpages.R", resourceStub(sources.values(), resourceNames));

        File workDir = Files.createTempDirectory("upgrade-fixtures-v" + snapshot.databaseVersion).toFile();
        File srcDir = new File(workDir, "src");
        File outDir = new File(workDir, "classes");
        assertTrue(outDir.mkdirs());
        List<String> javacArgs = new ArrayList<>();
        javacArgs.add(javacPath());
        javacArgs.add("-encoding");
        javacArgs.add("UTF-8");
        javacArgs.add("-nowarn");
        javacArgs.add("-proc:none");
        javacArgs.add("-cp");
        javacArgs.add(System.getProperty("java.class.path"));
        javacArgs.add("-d");
        javacArgs.add(outDir.getPath());
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            String renamedClass = renamePackages(entry.getKey(), packagePrefix);
            File sourceFile = new File(srcDir, renamedClass.replace('.', '/') + ".java");
            assertTrue(sourceFile.getParentFile().isDirectory() || sourceFile.getParentFile().mkdirs());
            Files.write(sourceFile.toPath(),
                    renamePackages(entry.getValue(), packagePrefix).getBytes(StandardCharsets.UTF_8));
            javacArgs.add(sourceFile.getPath());
        }
        String compileOutput = GenerateV1Fixtures.runProcess(javacArgs.toArray(new String[0]));
        assertNotNull("Database version " + snapshot.databaseVersion + " sources failed to compile"
                + " (see the output above); sources are in " + srcDir, compileOutput);

        URLClassLoader loader = new URLClassLoader(new URL[]{outDir.toURI().toURL()},
                GenerateUpgradeFixtures.class.getClassLoader());
        return new CompiledSnapshot(loader, packagePrefix, collectionClassNames, resourceNames);
    }

    /**
     * Removes the class members that use the database (the upgrade methods and their
     * helpers) and the imports only they need
     */
    static String withoutDatabaseCode(String source, String className) {
        ClassBody body = ClassBody.parse(source, className);
        StringBuilder out = new StringBuilder(source.substring(0, body.openBrace + 1));
        for (String member : body.members) {
            if (!DATABASE_CODE.matcher(withoutComments(member)).find()) {
                out.append(member);
            }
        }
        out.append("\n}\n");
        return DATABASE_IMPORT.matcher(out.toString()).replaceAll("");
    }

    /**
     * Builds a stand-in for a class that holds its primitive and String constants. For
     * CoinPageCreator it also keeps the static option maps, their initializers and the
     * methods that turn creation options into the flags stored in collection_info.
     */
    static String constantsOnly(String source, String packageName, String className,
                                boolean keepFlagEncoders) {
        ClassBody body = ClassBody.parse(source, className);
        StringBuilder out = new StringBuilder();
        out.append("package ").append(packageName).append(";\n\n");
        out.append("import com.spencerpages.R;\n");
        out.append("import java.util.HashMap;\n");
        out.append("import java.util.LinkedHashMap;\n\n");
        out.append("public class ").append(className).append(" {\n");
        for (String member : body.members) {
            String code = withoutComments(member).trim();
            boolean keep = CONSTANT_FIELD.matcher(code).matches();
            if (keepFlagEncoders) {
                keep |= code.matches("(?s)(private\\s+|public\\s+)?(final\\s+)?static\\s+(final\\s+)?"
                        + "(Linked)?HashMap<String,\\s*String>.*");
                keep |= code.startsWith("static {") || code.startsWith("static{");
                keep |= code.matches("(?s)public\\s+static\\s+(int|long)\\s+get(MintMark|Checkbox)FlagsFromParameters\\(.*");
            }
            if (keep) {
                out.append(member);
            }
        }
        out.append("\n}\n");
        return out.toString();
    }

    /**
     * Builds an R class with a distinct id for every resource the sources refer to
     *
     * @param resourceNames filled with each id's resource name (e.g. "include_p")
     */
    static String resourceStub(Iterable<String> sources, Map<Integer, String> resourceNames) {
        Map<String, Set<String>> resources = new TreeMap<>();
        for (String source : sources) {
            Matcher matcher = R_REFERENCE.matcher(withoutComments(source));
            while (matcher.find()) {
                resources.computeIfAbsent(matcher.group(1), k -> new LinkedHashSet<>()).add(matcher.group(2));
            }
        }
        StringBuilder out = new StringBuilder("package com.spencerpages;\n\npublic final class R {\n");
        int nextId = 0x7f000001;
        for (Map.Entry<String, Set<String>> type : resources.entrySet()) {
            out.append("    public static final class ").append(type.getKey()).append(" {\n");
            for (String name : type.getValue()) {
                resourceNames.put(nextId, name);
                out.append("        public static final int ").append(name).append(" = ")
                        .append(nextId++).append(";\n");
            }
            out.append("    }\n");
        }
        return out.append("}\n").toString();
    }

    /**
     * The top-level members of a class body, each as the source text from the end of the
     * previous member (so comments and annotations stay with the member they precede)
     */
    static final class ClassBody {
        int openBrace;
        final List<String> members = new ArrayList<>();

        static ClassBody parse(String source, String className) {
            Matcher declaration = Pattern.compile("\\b(class|interface)\\s+" + className + "\\b").matcher(source);
            assertTrue("No declaration of " + className, declaration.find());
            ClassBody body = new ClassBody();
            body.openBrace = source.indexOf('{', declaration.end());
            int depth = 0;
            int memberStart = body.openBrace + 1;
            int i = memberStart;
            while (i < source.length()) {
                char c = source.charAt(i);
                if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                    i = endOfLine(source, i);
                    continue;
                }
                if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '*') {
                    i = source.indexOf("*/", i + 2) + 2;
                    continue;
                }
                if (c == '"' || c == '\'') {
                    i = endOfLiteral(source, i);
                    continue;
                }
                if (c == '{' || c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (c == '}') {
                    if (depth == 0) {
                        break;   // the end of the class body
                    }
                    depth--;
                    if (depth == 0) {
                        // A method, initializer or nested class ends here, unless it was
                        // an array initializer, which a ';' follows
                        int next = i + 1;
                        while (next < source.length() && Character.isWhitespace(source.charAt(next))) {
                            next++;
                        }
                        int end = (next < source.length() && source.charAt(next) == ';') ? next + 1 : i + 1;
                        body.members.add(source.substring(memberStart, end));
                        memberStart = end;
                        i = end;
                        continue;
                    }
                } else if (c == ';' && depth == 0) {
                    body.members.add(source.substring(memberStart, i + 1));
                    memberStart = i + 1;
                }
                i++;
            }
            return body;
        }

        private static int endOfLine(String source, int i) {
            int end = source.indexOf('\n', i);
            return end < 0 ? source.length() : end;
        }

        private static int endOfLiteral(String source, int i) {
            char quote = source.charAt(i);
            int j = i + 1;
            while (j < source.length() && source.charAt(j) != quote) {
                j += (source.charAt(j) == '\\') ? 2 : 1;
            }
            return j + 1;
        }
    }

    /** Removes comments, leaving string literals intact */
    static String withoutComments(String source) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                i = ClassBody.endOfLine(source, i);
            } else if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '*') {
                int end = source.indexOf("*/", i + 2);
                i = end < 0 ? source.length() : end + 2;
                out.append(' ');
            } else if (c == '"' || c == '\'') {
                int end = ClassBody.endOfLiteral(source, i);
                out.append(source, i, end);
                i = end;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    // ---------------------------------------------------------------------------------
    // Running a snapshot's creation code

    /** The option keys a snapshot's creation screen used, read from its constants */
    static final class OptionKeys {
        final String showMintMarks;
        final String editDateRange;
        final String startYear;
        final String stopYear;
        final Set<String> mintMarks = new LinkedHashSet<>();

        OptionKeys(Class<?> creator) throws Exception {
            showMintMarks = (String) creator.getField("OPT_SHOW_MINT_MARKS").get(null);
            editDateRange = (String) creator.getField("OPT_EDIT_DATE_RANGE").get(null);
            startYear = (String) creator.getField("OPT_START_YEAR").get(null);
            stopYear = (String) creator.getField("OPT_STOP_YEAR").get(null);
            for (java.lang.reflect.Field field : creator.getFields()) {
                if (field.getName().matches("OPT_SHOW_MINT_MARK_\\d+")) {
                    mintMarks.add((String) field.get(null));
                }
            }
        }
    }

    private static void generateFixture(Snapshot snapshot) throws Exception {
        CompiledSnapshot compiled = compileSnapshot(snapshot);
        Class<?> creatorClass = compiled.load("com.coincollection.CoinPageCreator");
        OptionKeys keys = new OptionKeys(creatorClass);
        boolean storesOptions = snapshot.databaseVersion >= FIRST_VERSION_WITH_STORED_OPTIONS;

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"source\": ").append(GenerateV1Fixtures.quote("Generated by GenerateUpgradeFixtures from commit "
                + snapshot.commit + (snapshot.release != null ? " (" + snapshot.release + ")" : "")
                + ". Frozen - never regenerate against newer code.")).append(",\n");
        json.append("  \"commit\": ").append(GenerateV1Fixtures.quote(snapshot.commit)).append(",\n");
        json.append("  \"databaseVersion\": ").append(snapshot.databaseVersion).append(",\n");
        json.append("  \"collections\": [\n");

        boolean firstCollection = true;
        List<String> uncreatable = new ArrayList<>();
        for (String collectionClassName : compiled.collectionClassNames) {
            Class<?> collectionClass = compiled.load(collectionClassName);
            if (Modifier.isAbstract(collectionClass.getModifiers())) {
                continue;
            }
            Object collection = collectionClass.getDeclaredConstructor().newInstance();
            String coinType = (String) collectionClass.getMethod("getCoinType").invoke(collection);
            HashMap<String, Object> defaults = new HashMap<>();
            collectionClass.getMethod("getCreationParameters", HashMap.class).invoke(collection, defaults);

            List<HashMap<String, Object>> optionSets = buildOptionSets(defaults, keys);
            for (int i = 0; i < optionSets.size(); i++) {
                HashMap<String, Object> options = optionSets.get(i);
                List<Object[]> coins;
                try {
                    coins = runCreation(compiled, collectionClass, collection, options);
                } catch (InvocationTargetException e) {
                    // That version crashed creating this collection, so no user has one
                    uncreatable.add(coinType + " " + optionsJson(options) + ": " + e.getCause());
                    continue;
                }

                json.append(firstCollection ? "" : ",\n");
                firstCollection = false;
                json.append("    {\n");
                json.append("      \"name\": ").append(GenerateV1Fixtures.quote(coinType + " " + (i + 1))).append(",\n");
                json.append("      \"coinType\": ").append(GenerateV1Fixtures.quote(coinType)).append(",\n");
                json.append("      \"options\": ").append(optionsJson(options)).append(",\n");
                json.append("      \"labels\": ").append(labelsJson(options, compiled.resourceNames)).append(",\n");
                if (storesOptions) {
                    json.append("      \"stored\": ").append(storedJson(creatorClass, options, keys)).append(",\n");
                }
                json.append("      \"coins\": [\n");
                for (int j = 0; j < coins.size(); j++) {
                    Object[] coin = coins.get(j);
                    json.append("        [").append(GenerateV1Fixtures.quote((String) coin[0])).append(", ")
                            .append(GenerateV1Fixtures.quote((String) coin[1])).append(", ")
                            .append(coin[2]).append(", ").append(coin[3]).append("]")
                            .append(j < coins.size() - 1 ? ",\n" : "\n");
                }
                json.append("      ]\n");
                json.append("    }");
            }
        }
        json.append("\n  ],\n");
        json.append("  \"uncreatable\": [");
        for (int i = 0; i < uncreatable.size(); i++) {
            json.append(i == 0 ? "\n" : ",\n").append("    ").append(GenerateV1Fixtures.quote(uncreatable.get(i)));
        }
        json.append(uncreatable.isEmpty() ? "]\n}\n" : "\n  ]\n}\n");

        File outputFile = BaseTestCase.getTestDataSourceFile(
                OUTPUT_DIR + "/v" + snapshot.databaseVersion + ".json.gz");
        // GZIPOutputStream writes a zero timestamp, so the output is reproducible
        try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(outputFile.toPath()));
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write(json.toString());
        }
    }

    /**
     * Calls the snapshot's populateCollectionLists, which took parallel identifier and mint
     * lists before database version 14 and a CoinSlot list after
     *
     * @return the coins as {identifier, mint, imageId, sortOrder}
     */
    private static List<Object[]> runCreation(CompiledSnapshot compiled, Class<?> collectionClass,
                                              Object collection, HashMap<String, Object> options) throws Exception {
        HashMap<String, Object> parameters = new HashMap<>(options);
        List<Object[]> coins = new ArrayList<>();
        Method populate = findMethod(collectionClass, "populateCollectionLists");
        if (populate.getParameterCount() == 3) {
            ArrayList<String> identifiers = new ArrayList<>();
            ArrayList<String> mints = new ArrayList<>();
            populate.invoke(collection, parameters, identifiers, mints);
            assertEquals(identifiers.size(), mints.size());
            for (int i = 0; i < identifiers.size(); i++) {
                coins.add(new Object[]{identifiers.get(i), mints.get(i), -1, i});
            }
            return coins;
        }
        ArrayList<Object> coinSlots = new ArrayList<>();
        populate.invoke(collection, parameters, coinSlots);
        Class<?> coinSlotClass = compiled.load("com.coincollection.CoinSlot");
        Method getIdentifier = coinSlotClass.getMethod("getIdentifier");
        Method getMint = coinSlotClass.getMethod("getMint");
        Method getImageId = findMethodOrNull(coinSlotClass, "getImageId");
        Method getSortOrder = findMethodOrNull(coinSlotClass, "getSortOrder");
        for (int i = 0; i < coinSlots.size(); i++) {
            Object coinSlot = coinSlots.get(i);
            coins.add(new Object[]{
                    getIdentifier.invoke(coinSlot),
                    getMint.invoke(coinSlot),
                    getImageId != null ? getImageId.invoke(coinSlot) : -1,
                    getSortOrder != null ? getSortOrder.invoke(coinSlot) : i});
        }
        return coins;
    }

    /** The values that version's CoinPageCreator.getCollectionInfoFromParameters() stored */
    private static String storedJson(Class<?> creatorClass, HashMap<String, Object> options,
                                     OptionKeys keys) throws Exception {
        Object mintMarkFlags = creatorClass.getMethod("getMintMarkFlagsFromParameters", HashMap.class)
                .invoke(null, new HashMap<>(options));
        Object checkboxFlags = creatorClass.getMethod("getCheckboxFlagsFromParameters", HashMap.class)
                .invoke(null, new HashMap<>(options));
        Integer startYear = (Integer) options.get(keys.startYear);
        Integer stopYear = (Integer) options.get(keys.stopYear);
        return "{\"startYear\": " + (startYear != null ? startYear : 0)
                + ", \"endYear\": " + (stopYear != null ? stopYear : 0)
                + ", \"mintMarkFlags\": " + ((Number) mintMarkFlags).longValue()
                + ", \"checkboxFlags\": " + ((Number) checkboxFlags).longValue() + "}";
    }

    /**
     * Each option's label, by resource name. Options are numbered slots that a collection
     * type can reassign between versions, so the labels say which choices the version offered.
     */
    private static String labelsJson(Map<String, Object> options, Map<Integer, String> resourceNames) {
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> option : new TreeMap<>(options).entrySet()) {
            if (!option.getKey().endsWith("StringId")) {
                continue;
            }
            String name = resourceNames.get((Integer) option.getValue());
            assertNotNull("No resource name for " + option, name);
            out.append(first ? "" : ", ")
                    .append(GenerateV1Fixtures.quote(option.getKey().substring(0, option.getKey().length() - "StringId".length())))
                    .append(": ").append(GenerateV1Fixtures.quote(name));
            first = false;
        }
        return out.append("}").toString();
    }

    private static String optionsJson(Map<String, Object> options) {
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> option : new TreeMap<>(options).entrySet()) {
            if (option.getKey().endsWith("StringId")) {
                continue;   // labels, not choices (and only stand-in resource ids here)
            }
            out.append(first ? "" : ", ").append(GenerateV1Fixtures.quote(option.getKey())).append(": ");
            Object value = option.getValue();
            out.append(value instanceof String ? GenerateV1Fixtures.quote((String) value) : value);
            first = false;
        }
        return out.append("}").toString();
    }

    // ---------------------------------------------------------------------------------
    // Choosing option sets

    /**
     * The option sets to create each collection type with: the defaults, everything on,
     * everything off, then pairwise cover - rows are added until every pair of options has
     * appeared in every on/off combination that the creation screen allowed
     */
    static List<HashMap<String, Object>> buildOptionSets(HashMap<String, Object> defaults, OptionKeys keys) {
        List<String> toggles = new ArrayList<>();
        for (Map.Entry<String, Object> entry : new TreeMap<>(defaults).entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                toggles.add(entry.getKey());
            }
        }

        List<boolean[]> rows = new ArrayList<>();
        boolean[] defaultRow = new boolean[toggles.size()];
        boolean[] allOn = new boolean[toggles.size()];
        boolean[] allOff = new boolean[toggles.size()];
        for (int i = 0; i < toggles.size(); i++) {
            defaultRow[i] = (Boolean) defaults.get(toggles.get(i));
            allOn[i] = true;
        }
        addIfNew(rows, defaultRow);
        addIfValid(rows, allOn, toggles, keys);
        addIfValid(rows, allOff, toggles, keys);

        // Greedy pairwise cover. For each uncovered pair, start a row from it and set each
        // remaining option to the value that covers the most new pairs, then repair the
        // row if the creation screen wouldn't have accepted it.
        Set<String> uncovered = new LinkedHashSet<>();
        for (int a = 0; a < toggles.size(); a++) {
            for (int b = a + 1; b < toggles.size(); b++) {
                for (int va = 0; va < 2; va++) {
                    for (int vb = 0; vb < 2; vb++) {
                        uncovered.add(pairKey(a, va == 1, b, vb == 1));
                    }
                }
            }
        }
        for (boolean[] row : rows) {
            removeCovered(uncovered, row);
        }
        Set<String> impossible = new LinkedHashSet<>();
        while (uncovered.size() > impossible.size()) {
            String target = null;
            for (String pair : uncovered) {
                if (!impossible.contains(pair)) {
                    target = pair;
                    break;
                }
            }
            String[] parts = target.split(",");
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[2]);
            boolean[] row = new boolean[toggles.size()];
            boolean[] fixed = new boolean[toggles.size()];
            row[a] = Boolean.parseBoolean(parts[1]);
            row[b] = Boolean.parseBoolean(parts[3]);
            fixed[a] = true;
            fixed[b] = true;
            for (int k = 0; k < toggles.size(); k++) {
                if (fixed[k]) {
                    continue;
                }
                int gainOn = newPairs(uncovered, row, fixed, k, true);
                int gainOff = newPairs(uncovered, row, fixed, k, false);
                row[k] = gainOn > gainOff;
                fixed[k] = true;
            }
            if (!isValid(row, toggles, keys) && !repair(row, a, b, toggles, keys)) {
                impossible.add(target);
                continue;
            }
            int before = uncovered.size();
            removeCovered(uncovered, row);
            if (uncovered.size() == before) {
                impossible.add(target);
                continue;
            }
            addIfNew(rows, row);
        }

        List<HashMap<String, Object>> optionSets = new ArrayList<>();
        for (boolean[] row : rows) {
            HashMap<String, Object> options = new HashMap<>(defaults);
            for (int i = 0; i < toggles.size(); i++) {
                options.put(toggles.get(i), row[i]);
            }
            if (Boolean.TRUE.equals(options.get(keys.editDateRange))) {
                Integer start = (Integer) defaults.get(keys.startYear);
                Integer stop = (Integer) defaults.get(keys.stopYear);
                if (start != null && stop != null && stop - start >= 2) {
                    options.put(keys.startYear, start + 1);
                    options.put(keys.stopYear, stop - 1);
                }
            }
            optionSets.add(options);
        }
        return optionSets;
    }

    private static String pairKey(int a, boolean va, int b, boolean vb) {
        return a + "," + va + "," + b + "," + vb;
    }

    private static void removeCovered(Set<String> uncovered, boolean[] row) {
        for (int a = 0; a < row.length; a++) {
            for (int b = a + 1; b < row.length; b++) {
                uncovered.remove(pairKey(a, row[a], b, row[b]));
            }
        }
    }

    private static int newPairs(Set<String> uncovered, boolean[] row, boolean[] fixed, int k, boolean value) {
        int count = 0;
        for (int j = 0; j < row.length; j++) {
            if (j == k || !fixed[j]) {
                continue;
            }
            String pair = j < k ? pairKey(j, row[j], k, value) : pairKey(k, value, j, row[j]);
            if (uncovered.contains(pair)) {
                count++;
            }
        }
        return count;
    }

    /** The creation screen required at least one mint when mint marks were shown */
    private static boolean isValid(boolean[] row, List<String> toggles, OptionKeys keys) {
        int show = toggles.indexOf(keys.showMintMarks);
        if (show < 0 || !row[show]) {
            return true;
        }
        for (int i = 0; i < toggles.size(); i++) {
            if (row[i] && keys.mintMarks.contains(toggles.get(i))) {
                return true;
            }
        }
        return false;
    }

    /** Turns on the first mint the target pair leaves free, if there is one */
    private static boolean repair(boolean[] row, int a, int b, List<String> toggles, OptionKeys keys) {
        for (int i = 0; i < toggles.size(); i++) {
            if (i != a && i != b && keys.mintMarks.contains(toggles.get(i))) {
                row[i] = true;
                return true;
            }
        }
        return false;
    }

    private static void addIfValid(List<boolean[]> rows, boolean[] row, List<String> toggles, OptionKeys keys) {
        if (isValid(row, toggles, keys)) {
            addIfNew(rows, row);
        }
    }

    private static void addIfNew(List<boolean[]> rows, boolean[] row) {
        for (boolean[] existing : rows) {
            if (java.util.Arrays.equals(existing, row)) {
                return;
            }
        }
        rows.add(row.clone());
    }

    // ---------------------------------------------------------------------------------
    // Reflection and git helpers

    private static Method findMethod(Class<?> type, String name) {
        Method method = findMethodOrNull(type, name);
        assertNotNull(type.getName() + "." + name, method);
        return method;
    }

    private static Method findMethodOrNull(Class<?> type, String name) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    private static String baseName(String path) {
        String file = path.substring(path.lastIndexOf('/') + 1);
        return file.substring(0, file.length() - ".java".length());
    }

    private static String javacPath() {
        // javax.tools isn't on the Android unit-test compile classpath, so run the javac that
        // ships with the JDK running the tests
        File javaBin = new File(System.getProperty("java.home"), "bin");
        File javac = new File(javaBin, "javac.exe").exists()
                ? new File(javaBin, "javac.exe") : new File(javaBin, "javac");
        assertTrue("A JDK is required to compile old sources: " + javac, javac.exists());
        return javac.getPath();
    }

    private static List<String> gitListFiles(String commit, String dir) throws IOException, InterruptedException {
        String output = GenerateV1Fixtures.runProcess("git", "ls-tree", "--name-only", "--full-tree", commit, dir);
        assertNotNull("git ls-tree failed for " + commit + ":" + dir, output);
        List<String> files = new ArrayList<>();
        for (String line : output.split("\n")) {
            if (!line.trim().isEmpty()) {
                files.add(line.trim());
            }
        }
        java.util.Collections.sort(files);
        return files;
    }

    private static boolean gitFileExists(String commit, String path) throws IOException, InterruptedException {
        return GenerateV1Fixtures.runProcess("git", "cat-file", "-e", commit + ":" + path) != null;
    }

    private static String gitShow(String commit, String path) throws IOException, InterruptedException {
        String contents = GenerateV1Fixtures.runProcess("git", "show", commit + ":" + path);
        assertNotNull("git show failed for " + commit + ":" + path, contents);
        return contents.replace("\r\n", "\n");
    }
}
