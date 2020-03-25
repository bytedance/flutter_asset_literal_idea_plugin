package com.ixigua.completion.assets;

import com.intellij.openapi.vfs.VirtualFile;

public class Asset {
    private String name;
    private AssetType type;
    private VirtualFile file;

    public Asset(String name, AssetType type, VirtualFile file, String sourceDescription) {
        this.name = name;
        this.type = type;
        this.file = file;
        this.sourceDescription = sourceDescription;
    }

    private String sourceDescription;

    public String getName() {
        return name;
    }

    public VirtualFile getFile() {
        return file;
    }

    public AssetType getType() {
        return type;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "name='" + name + '\'' +
                ", file=" + file +
                ", bundledPackage='" + sourceDescription + '\'' +
                '}';
    }
}
