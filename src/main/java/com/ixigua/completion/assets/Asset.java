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

    @NotNull
    public abstract String lookupString();

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
