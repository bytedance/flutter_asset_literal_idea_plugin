import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CompleteTest extends BasePlatformTestCase {

    @NotNull
    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        copyFilesToProject();
    }

    private void copyFilesToProject() {
        //      We can copy the test resource file to the editor through the copyDirectoryToProject method

//      Here we add the assets folder, pubspec.yaml, and other resource files to the editor. you can see the directory
//      structure of these files under the src/test/testData folder
        myFixture.copyDirectoryToProject("flutter_asset_literal_test", "flutter_asset_literal_test");
        myFixture.copyDirectoryToProject("xg_appearance", "xg_appearance");
    }

    public void testCompletionNormalAsset() {
//      BasePlatformTestCase will create an in-memory editor, we can simulate the user's editing behavior through myFixture

//      We can use the configureByFiles method to let the editor open a file, just like we are editing this file,
//      the operations we will simulate in the future will happen in this file.
//      and you may notice this string "<caret>" in the CompleteTestData.dart file，it is used to simulate the user's
//      current cursor position.
        myFixture.configureByFiles("flutter_asset_literal_test/lib/CompleteTestData.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList(
                "assets/flr/loading.flr",
                "assets/json/abc.json",
                "assets/images/bmp/XING_B24.bmp",
                "assets/images/jpg/long.jpg",
                "assets/images/jpg/wide.jpg",
                "assets/images/svg/icon_camera.svg",
                "assets/images/tiff/CCITT_4.TIF",
                "assets/images/big_png/MainAlignment_start.png",
                "assets/images/webp/back.webp",
                "assets/images/png/play.png",
                "assets/images/png/danmaku_off.png",
                "assets/images/png/lock.png",
                "assets/images/png/play_in_list.png",
                "assets/images/png/unlock.png",
                "assets/images/png/zoom_in.png",
                "assets/images/png/pause.png",
                "assets/images/gif/flutter_asset_completion.gif",
                "asset_in_Lib.png",
                "othor_assets/some.xml"
        )));
        assertEquals(19, strings.size());
    }

    public void testCompletionInLib() {
        myFixture.configureByFiles("flutter_asset_literal_test/lib/CompleteTestDataInLib.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        assertTrue(strings.contains("asset_in_Lib.png"));
        assertEquals(1, strings.size());
    }

    public void testCompletionInDependency() {
        myFixture.configureByFiles("flutter_asset_literal_test/lib/CompleteTestDataInDependency.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList(
                "packages/xg_appearance/images/arrow_right.png",
                "packages/xg_appearance/images/arrow_right_dark.png",
                "packages/xg_appearance/images/check_selected.png",
                "packages/xg_appearance/images/check_unselected.png",
                "packages/xg_appearance/images/display_count_img.png"
        )));
        assertEquals(5, strings.size());
    }

    public void testCompletionForPreInstalledFont() {
        myFixture.configureByFiles("flutter_asset_literal_test/lib/CompleteTestDataForPreInstalledFont.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList(
                "sans-serif",
                "sans-serif-condensed",
                "sans-serif-light",
                "sans-serif-thin",
                "San Francisco",
                "Bangla Sangam MN",
                "Devanagari Sangam MN",
                "Droid Sans",
                "Gill Sans",
                "Gujarati Sangam MN",
                "Hiragino Sans",
                "Kannada Sangam MN",
                "KhmerSangamMN",
                "LaoSangamMN",
                "Malayalam Sangam MN",
                "Oriya Sangam MN",
                "Sinhala Sangam MN",
                "Tamil Sangam MN",
                "Telugu Sangam MN"
        )));
        assertEquals(19, strings.size());
    }

    public void testCompletionForPubspecFont() {
        myFixture.configureByFiles("flutter_asset_literal_test/lib/CompleteTestDataForPubspecFont.dart");
        
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        // DINAlternateBold is declared in pubspec.yaml
        assertTrue(strings.containsAll(Arrays.asList(
                "packages/xg_appearance/DINAlternateBold",
                "DINAlternateBold",
                "DIN Alternate",
                "DIN Condensed",
                "Zapf Dingbats",
                "packages/xg_appearance/DINAlternateNumber",
                "packages/xg_appearance/DIN_Alternate",
                "assets/flr/loading.flr"
        )));
        assertEquals(8, strings.size());
    }

    public void testCompletionInDependentLib() {
        myFixture.configureByFiles("flutter_asset_literal_test/lib/CompleteTestDataInDependentLib.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        assertTrue(strings.contains("packages/xg_appearance/smoke.png"));
        assertEquals(1, strings.size());
    }
}
