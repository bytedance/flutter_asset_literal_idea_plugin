package com.ixigua.completion.sync;

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
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Stream;

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
        Stream<String> files = syncAssetsInfo.selectedFileOrFolder.stream().map(virtualFile -> VfsUtilCore.getRelativePath(virtualFile, syncAssetsInfo.projectDir));
        // If user select a folder, we get all its children, and write them into pubspec.
        Set<VirtualFile> allFiles = new HashSet<>();
        for (VirtualFile file :
                syncAssetsInfo.selectedFileOrFolder) {
            allFiles.addAll(expandAssetFile(file));
        }
        List<String> list = new ArrayList<>();
        VirtualFile lib = syncAssetsInfo.projectDir.findChild("lib");
        for (VirtualFile virtualFile : allFiles) {
            if (lib != null && VfsUtilCore.isAncestor(lib, virtualFile, false)) {
                String name = virtualFile.getName();
                if ("lib".equals(name)) {
                    for (VirtualFile fileInLib :
                            virtualFile.getChildren()) {
                        String ext = fileInLib.getExtension();
                        if (ext != null && ext.toLowerCase().equals("dart")) {
                            continue;
                        }
                        if (fileInLib.isDirectory()) {
                            continue;
                        }
                        list.add(fileInLib.getName());
                    }
                } else if (lib.findChild(name) != null && !virtualFile.isDirectory()) {
                    list.add(name);
                }
            } else {
                if (virtualFile.isDirectory()) {
                    list.add(VfsUtilCore.getRelativePath(virtualFile, syncAssetsInfo.projectDir) + File.separator);
                } else {
                    list.add(VfsUtilCore.getRelativePath(virtualFile, syncAssetsInfo.projectDir));
                }
            }
        }
        String[] assets = list.toArray(new String[0]);
        // Modify the pubspec.yaml file.
        try {
            int offset = PubspecUtil.insertAssets(syncAssetsInfo.pubspec, assets, FileDocumentManager.getInstance().getLineSeparator(syncAssetsInfo.pubspec, syncAssetsInfo.project));
            if (offset < 0) {
                return;
            }
            // In order to let users know that we have modified the pubspec, we need to open the pubspec file in editor.
            FileDocumentManager.getInstance().reloadFiles(syncAssetsInfo.pubspec);
            FileEditorManager.getInstance(syncAssetsInfo.project).openTextEditor(
                    new OpenFileDescriptor(
                            syncAssetsInfo.project,
                            syncAssetsInfo.pubspec,
                            offset
                    ),
                    true // request focus to editor
            );
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
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
    private ArrayList<VirtualFile> expandAssetFile(@NotNull VirtualFile file) {
        ArrayList<VirtualFile> ret = new ArrayList<>();
        if (!file.exists()) {
            return ret;
        }
        if (!file.isValid()) {
            return ret;
        }
        if (!file.isDirectory()) {
            String ext = file.getExtension();
            if (ext != null && !ext.toLowerCase().equals("dart")) {
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
}
