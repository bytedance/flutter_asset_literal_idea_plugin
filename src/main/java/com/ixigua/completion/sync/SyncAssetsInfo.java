package com.ixigua.completion.sync;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.ixigua.completion.pubspec.PubspecUtil;

import java.io.File;
import java.util.ArrayList;

public class SyncAssetsInfo {
    ArrayList<VirtualFile> selectedFileOrFolder;
    VirtualFile projectDir;
    Project project;
    VirtualFile pubspec;

    public SyncAssetsInfo(VirtualFile[] selectedFileOrFolder, Project project) {
        this.project = project;
        String projectBasePath = project.getBasePath();
        if (projectBasePath != null) {
            VirtualFile projectDir = LocalFileSystem.getInstance().findFileByIoFile(new File(projectBasePath));
            if (projectDir != null) {
                this.projectDir = projectDir;
            }
        }
        this.selectedFileOrFolder = filterFiles(selectedFileOrFolder);
        this.pubspec = PubspecUtil.findPubspecYamlFile(project, selectedFileOrFolder[0]);
    }

    private ArrayList<VirtualFile> filterFiles(VirtualFile[] input) {
        if (input == null) {
            return null;
        }
        if (projectDir == null) {
            return null;
        }
        ArrayList<VirtualFile> ret = new ArrayList<>();
        for (VirtualFile file : input) {
            if (!file.isValid()) {
                continue;
            }
            if (!VfsUtilCore.isAncestor(projectDir, file, true)) {
                continue;
            }
            ret.add(file);
        }
        return ret;
    }

    boolean isValid() {
        if (selectedFileOrFolder == null) {
            return false;
        }
        if (projectDir == null) {
            return false;
        }
        if (pubspec == null) {
            return false;
        }
        if (selectedFileOrFolder.isEmpty()) {
            return false;
        }
        if (!projectDir.isValid()) {
            return false;
        }
        return pubspec.isValid();
    }
}
