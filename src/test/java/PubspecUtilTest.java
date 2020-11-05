import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.exceptionCases.AbstractExceptionCase;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.ixigua.completion.pubspec.PubspecUtil;
import org.jetbrains.annotations.NotNull;

public class PubspecUtilTest extends BasePlatformTestCase {
    @NotNull
    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testInsertAssets_rootNotMapping() {
        String content = "";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        assertException(new AbstractExceptionCase<IllegalStateException>() {
            @Override
            public Class<IllegalStateException> getExpectedExceptionClass() {
                return IllegalStateException.class;
            }

            @Override
            public void tryClosure() throws IllegalStateException {
                PubspecUtil.insertAssets(content, assets, lineSep);
            }
        },"pubspecContent MUST be mapping");
    }

    public void testInsertAssets_flutterSectionIsMissing() {
        String content = "x: y";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        assertException(new AbstractExceptionCase<IllegalStateException>() {
            @Override
            public Class<IllegalStateException> getExpectedExceptionClass() {
                return IllegalStateException.class;
            }

            @Override
            public void tryClosure() throws IllegalStateException {
                PubspecUtil.insertAssets(content, assets, lineSep);
            }
        },"flutter section is missing");
    }

    public void testInsertAssets_flutterIsNotMapping() {
        String content = "flutter: y";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        assertException(new AbstractExceptionCase<IllegalStateException>() {
            @Override
            public Class<IllegalStateException> getExpectedExceptionClass() {
                return IllegalStateException.class;
            }

            @Override
            public void tryClosure() throws IllegalStateException {
                PubspecUtil.insertAssets(content, assets, lineSep);
            }
        },"flutter section content is missing");
    }

    public void testInsertAssets_assetsSectionIsMissing() {
        String content = "flutter:\n" +
                "  x: y";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        assertException(new AbstractExceptionCase<IllegalStateException>() {
            @Override
            public Class<IllegalStateException> getExpectedExceptionClass() {
                return IllegalStateException.class;
            }

            @Override
            public void tryClosure() throws IllegalStateException {
                PubspecUtil.insertAssets(content, assets, lineSep);
            }
        },"assets section is missing");
    }

    public void testInsertAssets_assetsContentIsMissing() {
        String content = "flutter:\n" +
                "  assets:";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        Pair<Integer, String> edited = PubspecUtil.insertAssets(content, assets, lineSep);
        String expected =   "flutter:\n" +
                            "  assets:\n" +
                            "    - 1\n" +
                            "    - 2";
        assertEquals(expected, edited.second);
        assertEquals(expected.length(), edited.first.intValue());
    }

    public void testInsertAssets_assetsContentIsScalar() {
        String content = "flutter:\n" +
                "  assets: xxxx";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        Pair<Integer, String> edited = PubspecUtil.insertAssets(content, assets, lineSep);

        // if the original assets content is not sequence, the edited is not sequence too, this is as expected
        String expected =   "flutter:\n" +
                "  assets: xxxx\n" +
                "    - 1\n" +
                "    - 2";
        assertEquals(expected, edited.second);
        assertEquals(expected.length(), edited.first.intValue());
    }

    public void testInsertAssets_assetsContentIsMapping() {
        String content = "flutter:\n" +
                "  assets:\n" +
                "    x: y";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        Pair<Integer, String> edited = PubspecUtil.insertAssets(content, assets, lineSep);

        // if the original assets content is not sequence, the edited is not sequence too, this is as expected
        String expected =   "flutter:\n" +
                "  assets:\n" +
                "    x: y\n" +
                "    - 1\n" +
                "    - 2";
        assertEquals(expected, edited.second);
        assertEquals(expected.length(), edited.first.intValue());
    }

    public void testInsertAssets_assetsContentHasSomeValueDuplicatedWithInsertedAssets() {
        String content = "flutter:\n" +
                "  assets:\n" +
                "    - 1";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        Pair<Integer, String> edited = PubspecUtil.insertAssets(content, assets, lineSep);

        // if the original assets content is not sequence, the edited is not sequence too, this is as expected
        String expected =   "flutter:\n" +
                "  assets:\n" +
                "    - 1\n" +
                "    - 2";
        assertEquals(expected, edited.second);
        assertEquals(expected.length(), edited.first.intValue());
    }

    public void testInsertAssets_assetsContentIsEmptySequence() {
        String content = "flutter:\n" +
                "  assets:\n" +
                "    -";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        Pair<Integer, String> edited = PubspecUtil.insertAssets(content, assets, lineSep);

        // if the original assets content is not sequence, the edited is not sequence too, this is as expected
        String expected =   "flutter:\n" +
                "  assets:\n" +
                "    -\n" +
                "    - 1\n" +
                "    - 2";
        assertEquals(expected, edited.second);
        assertEquals(expected.length(), edited.first.intValue());
    }

    public void testInsertAssets_assetsContentHasNoValueDuplicatedWithInsertedAssets() {
        String content = "flutter:\n" +
                "  assets:\n" +
                "    - x";
        String[] assets = {"1", "2"};
        String lineSep = System.lineSeparator();
        Pair<Integer, String> edited = PubspecUtil.insertAssets(content, assets, lineSep);

        // if the original assets content is not sequence, the edited is not sequence too, this is as expected
        String expected =   "flutter:\n" +
                "  assets:\n" +
                "    - x\n" +
                "    - 1\n" +
                "    - 2";
        assertEquals(expected, edited.second);
        assertEquals(expected.length(), edited.first.intValue());
    }

//    @NotNull
//    public static Pair<Integer, String> insertAssets(@NotNull String pubspecContent, @NotNull String[] assets, @Nullable String lineSeparator) {
//        int offset;
//        Set<String> originalAssetsSet = new HashSet<>();
//        Node assetsSeq = assetsNode.get().getValueNode();
//        if (!(assetsSeq instanceof SequenceNode)) {
//            offset = assetsSeq.getEndMark().getIndex();
//        } else {
//            ((SequenceNode) assetsSeq).getValue().forEach(asset -> {
//                if (!(asset instanceof ScalarNode)) {
//                    return;
//                }
//                originalAssetsSet.add(((ScalarNode) asset).getValue());
//            });
//            Node lastAssetNode = ((SequenceNode) assetsSeq).getValue().get(((SequenceNode) assetsSeq).getValue().size() - 1);
//
//            offset = lastAssetNode.getEndMark().getIndex();
//        }
//        reader.close();
//        // why not to use SnakeYAML's Emitter API to edit pubspec:
//        // because Emitter will lost all comments of the original pubspec.
//        StringBuilder sb = new StringBuilder(pubspecContent);
//        for (String asset :
//                assets) {
//            if (originalAssetsSet.contains(asset)) {
//                continue;
//            }
//            sb.insert(offset, lineSeparator);
//            offset += 1;
//            sb.insert(offset, "    - " + asset);
//            offset += 6 + asset.length();
//        }
//        return new Pair<>(offset, sb.toString());
//    }
}
