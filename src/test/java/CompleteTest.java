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

    public void testCompletionNormalAsset() {
//      BasePlatformTestCase will create an in-memory editor, we can simulate the user's editing behavior through myFixture
//      We can copy the test resource file to the editor through the copyDirectoryToProject method
//      Here we add the assets folder, pubspec.yaml, and other resource files to the editor. you can see the directory
//      structure of these files under the testData folder
        myFixture.copyDirectoryToProject("assets", "assets");
        myFixture.copyFileToProject("pubspec.yaml");
//      We can use the configureByFiles method to let the editor open a file, just like we are editing this file,
//      the operations we will simulate in the future will happen in this file.
//      and you may notice this string "<caret>" in the CompleteTestData.dart file，it is used to simulate the user's
//      current cursor position.
        myFixture.configureByFiles("CompleteTestData.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings.containsAll(Arrays.asList("assets/flr/loading.flr",
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
                "assets/images/gif/flutter_asset_completion.gif")));
        assertEquals(17, strings.size());
    }

    public void testCompletionInLib() {
        myFixture.copyDirectoryToProject("lib", "lib");
        myFixture.copyFileToProject("pubspec.yaml");
        myFixture.configureByFiles("CompleteTestDataInLib.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings.containsAll(Arrays.asList("asset_in_Lib.png")));
        assertEquals(1, strings.size());
    }

    public void testCompletionInDependency() {
        myFixture.copyFileToProject("pubspec.yaml");
        myFixture.configureByFiles("CompleteTestDataInDependency.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings.containsAll(Arrays.asList(
                "packages/fancy_images/image_1.png",
                "packages/fancy_images/image_2.png"
        )));
        assertEquals(2, strings.size());
    }

    public void testCompletionForPreInstalledFont() {
        myFixture.copyFileToProject("pubspec.yaml");
        myFixture.configureByFiles("CompleteTestDataForPreInstalledFont.dart");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
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
        myFixture.copyFileToProject("pubspec.yaml");
        myFixture.configureByFiles("CompleteTestDataForPubspecFont.dart");
        
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        // DINAlternateBold is declared in pubspec.yaml
        assertTrue(strings.containsAll(Arrays.asList(
                "DINAlternateBold",
                "DIN Alternate",
                "DIN Condensed",
                "Zapf Dingbats"
        )));
        assertEquals(4, strings.size());
    }
}
