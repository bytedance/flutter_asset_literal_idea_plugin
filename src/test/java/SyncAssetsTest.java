import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.ixigua.completion.sync.SyncAssetsAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SyncAssetsTest extends BasePlatformTestCase {


    @NotNull
    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    private VirtualFile project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        copyFilesToProject();
    }

    private void copyFilesToProject() {
        //      We can copy the test resource file to the editor through the copyDirectoryToProject method

//      Here we add the assets' directory, pubspec.yaml, and other resource files to the editor. you can see the directory
//      structure of these files under the src/test/testData folder
        project = myFixture.copyDirectoryToProject("project_for_sync_assets", "project_for_sync_assets");
    }

    public void testExpandAssetFile_fileNotDirectory() {
        VirtualFile file = project.findFileByRelativePath("assets/flr/loading.flr");
        ArrayList<VirtualFile> ret = SyncAssetsAction.expandAssetFile(Objects.requireNonNull(file));
        assertSize(1, ret);
        assertEquals(ret.get(0), file);
    }

    public void testExpandAssetFile_fileNotDirectoryWithoutExt() {
        VirtualFile file = project.findFileByRelativePath("assets/flr/a");
        ArrayList<VirtualFile> ret = SyncAssetsAction.expandAssetFile(Objects.requireNonNull(file));
        assertSize(1, ret);
        assertEquals(ret.get(0), file);
    }

    public void testExpandAssetFile_fileNotDirectoryIsDart() {
        VirtualFile file = project.findFileByRelativePath("assets/flr/b.dart");
        ArrayList<VirtualFile> ret = SyncAssetsAction.expandAssetFile(Objects.requireNonNull(file));
        assertEmpty(ret);
    }

    public void testExpandAssetFile_directory() {
        VirtualFile file = project.findFileByRelativePath("assets/d1");
        ArrayList<VirtualFile> ret = SyncAssetsAction.expandAssetFile(Objects.requireNonNull(file));
        assertSize(4, ret);
        assertContainsElements(ret, project.findFileByRelativePath("assets/d1"),project.findFileByRelativePath("assets/d1/d11"),project.findFileByRelativePath("assets/d1/d12"),project.findFileByRelativePath("assets/d1/d11/d111"));
    }

    public void testExpandAssetFile_directoryWithoutSubDir() {
        VirtualFile file = project.findFileByRelativePath("assets/d2");
        ArrayList<VirtualFile> ret = SyncAssetsAction.expandAssetFile(Objects.requireNonNull(file));
        assertSize(1, ret);
        assertContainsElements(ret, file);
    }


    public void testAssetNameFromFile_fileInLib() {
        VirtualFile file = project.findFileByRelativePath("lib/3D.JPG");
        VirtualFile lib = project.findFileByRelativePath("lib");
        List<String> ret = SyncAssetsAction.assetNameFromFile(Objects.requireNonNull(file), lib);
        assertSize(1, ret);
        assertContainsElements(ret, "3D.JPG");
    }

    public void testAssetNameFromFile_dirInLib() {
        VirtualFile file = project.findFileByRelativePath("lib/d1");
        VirtualFile lib = project.findFileByRelativePath("lib");
        List<String> ret = SyncAssetsAction.assetNameFromFile(Objects.requireNonNull(file), lib);
        assertEmpty(ret);
    }

    public void testAssetNameFromFile_lib() {
        VirtualFile lib = project.findFileByRelativePath("lib");
        List<String> ret = SyncAssetsAction.assetNameFromFile(Objects.requireNonNull(lib), lib);
        assertSize(3, ret);
        assertContainsElements(ret, "3D.JPG","asset_in_Lib.png","danmaku_on.png");
    }

    public void testAssetNameFromFile_dirNotInLib() {
        VirtualFile file = project.findFileByRelativePath("assets");
        VirtualFile lib = project.findFileByRelativePath("lib");
        List<String> ret = SyncAssetsAction.assetNameFromFile(Objects.requireNonNull(file), lib);
        assertSize(1, ret);
        assertContainsElements(ret, "assets/");
    }

    public void testAssetNameFromFile_fileNotInLib() {
        VirtualFile file = project.findFileByRelativePath("assets/d1/a");
        VirtualFile lib = project.findFileByRelativePath("lib");
        List<String> ret = SyncAssetsAction.assetNameFromFile(Objects.requireNonNull(file), lib);
        assertSize(1, ret);
        assertContainsElements(ret, "assets/d1/a");
    }
}
