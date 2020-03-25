package com.ixigua.completion.assets;

import com.intellij.openapi.vfs.VirtualFile;

public class Asset {
    private String name;
    private AssetType type;
    private VirtualFile file;

    public Asset(String name, AssetType type, VirtualFile file, String bundledPackage) {
        this.name = name;
        this.type = type;
        this.file = file;
        this.bundledPackage = bundledPackage;
    }

    private String bundledPackage;

    public String getName() {
        return name;
    }

    public VirtualFile getFile() {
        return file;
    }

    public AssetType getType() {
        return type;
    }

    public String getBundledPackage() {
        return bundledPackage;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "name='" + name + '\'' +
                ", file=" + file +
                ", bundledPackage='" + bundledPackage + '\'' +
                '}';
    }
}
