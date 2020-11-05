package com.ixigua.completion.sync;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.ixigua.completion.pubspec.PubspecUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class SyncAssetsAction extends AnAction {

//    private static final Logger LOG = Logger.getInstance(SyncAssetsAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        SyncAssetsInfo syncAssetsInfo = getInfoFromEvent(e);
        boolean visible = syncAssetsInfo != null && syncAssetsInfo.isValid();
        e.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SyncAssetsInfo syncAssetsInfo = getInfoFromEvent(e);
        assert syncAssetsInfo != null;
        // If user select a folder, we get all its children, and write them into pubspec.
        Set<VirtualFile> allFiles = new HashSet<>();
        for (VirtualFile file :
                syncAssetsInfo.selectedFileOrFolder) {
            allFiles.addAll(expandAssetFile(file));
        }
        VirtualFile lib = syncAssetsInfo.projectDir.findChild("lib");
        String[] assets = assetNameFromFiles(allFiles, lib, syncAssetsInfo.projectDir);
        // Modify the pubspec.yaml file.
        try {
            int offset = PubspecUtil.insertAssets(syncAssetsInfo.pubspec, assets, FileDocumentManager.getInstance().getLineSeparator(syncAssetsInfo.pubspec, syncAssetsInfo.project));
            if (offset < 0) {
                return;
            }
            // In order to let users know that we have modified the pubspec, we need to open the pubspec file in editor.
            openPubspecInEditor(syncAssetsInfo.pubspec, syncAssetsInfo.project, offset);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Nullable
    private SyncAssetsInfo getInfoFromEvent(@NotNull AnActionEvent e) {
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles == null || selectedFiles.length == 0) {
            return null;
        }
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        return new SyncAssetsInfo(selectedFiles, project);
    }

    @NotNull
    public static ArrayList<VirtualFile> expandAssetFile(@NotNull VirtualFile file) {
        ArrayList<VirtualFile> ret = new ArrayList<>();
        if (!file.exists()) {
            return ret;
        }
        if (!file.isValid()) {
            return ret;
        }
        if (!file.isDirectory()) {
            String ext = file.getExtension();
            if (ext == null || !ext.toLowerCase().equals("dart")) {
                ret.add(file);
            }
            return ret;
        }
        VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor<Object>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.exists()) {
                    return true;
                }
                if (!file.isValid()) {
                    return true;
                }
                if (!file.isDirectory()) {
                    return true;
                }
                ret.add(file);
                return super.visitFile(file);
            }
        });
        return ret;
    }

    public static List<String> assetNameFromFile(@NotNull VirtualFile file, VirtualFile lib, @NotNull VirtualFile projectDir) {
        List<String> list = new ArrayList<>();
        if (lib != null && VfsUtilCore.isAncestor(lib, file, false)) {
            String name = file.getName();
            if ("lib".equals(name)) {
                for (VirtualFile fileInLib :
                        file.getChildren()) {
                    String ext = fileInLib.getExtension();
                    if (ext != null && ext.toLowerCase().equals("dart")) {
                        continue;
                    }
                    if (fileInLib.isDirectory()) {
                        continue;
                    }
                    list.add(fileInLib.getName());
                }
            } else if (lib.findChild(name) != null && !file.isDirectory()) {
                list.add(name);
            }
        } else {
            if (file.isDirectory()) {
                list.add(VfsUtilCore.getRelativePath(file, projectDir) + File.separator);
            } else {
                list.add(VfsUtilCore.getRelativePath(file, projectDir));
            }
        }
        return list;
    }

    private static String[] assetNameFromFiles(@NotNull Iterable<VirtualFile> files, VirtualFile lib, @NotNull VirtualFile projectDir) {
        List<String> list = new ArrayList<>();
        for (VirtualFile virtualFile : files) {
            list.addAll(assetNameFromFile(virtualFile, lib, projectDir));
        }
        return list.toArray(new String[0]);
    }

    private static void openPubspecInEditor(@NotNull VirtualFile pubspec, @NotNull Project project, int offset) {
        FileDocumentManager.getInstance().reloadFiles(pubspec);
        FileEditorManager.getInstance(project).openTextEditor(
                new OpenFileDescriptor(
                        project,
                        pubspec,
                        offset
                ),
                true // request focus to editor
        );
    }
}
