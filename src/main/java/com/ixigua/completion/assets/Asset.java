package com.ixigua.completion.assets;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class Asset {
    private String name;
    private VirtualFile file;
    private String sourceDescription;

    public Asset(@NotNull String name, @Nullable VirtualFile file, @Nullable String sourceDescription) {
        this.name = name;
        this.file = file;
        this.sourceDescription = sourceDescription;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public VirtualFile getFile() {
        return file;
    }

    @Nullable
    public String getSourceDescription() {
        return sourceDescription;
    }


    // lookup string for package, 'packageName' is the package's name to which the file being edited belongs
    // let's say we have two packages: PA and PB, PA depend on PB and there is an image named 'B.png' in PB.
    // when user type 'B.png' in PA's dart file,  lookupStringForPackage will be called with 'PA'.
    // when user type 'B.png' in PB's dart file,  lookupStringForPackage will be called with 'PB'.
    // subclasses can choose to return different lookup strings according to different package names
    @NotNull
    public abstract String lookupStringForPackage(String packageName);

    @Nullable
    public abstract String typeText();

    @Nullable
    public abstract Icon icon();

    @Override
    public String toString() {
        return "Asset{" +
                "name='" + name + '\'' +
                ", file=" + file +
                ", bundledPackage='" + sourceDescription + '\'' +
                '}';
    }
}
