package com.ixigua.completion.assets;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.ixigua.completion.icon.IconDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ImageAsset extends Asset {

    private static final Logger LOG = Logger.getInstance(FontAsset.class);

    public ImageAsset(@NotNull String name, @Nullable VirtualFile file, @Nullable String sourceDescription) {
        super(name, file, sourceDescription);
    }

    @NotNull
    @Override
    public String lookupStringForPackage(String packageName) {
        if (StringUtil.isEmpty(packageName)) {
            return getName();
        }
        if (StringUtil.isEmpty(getSourceDescription())) {
            return getName();
        }
        if (packageName.contentEquals(getSourceDescription())) {
            return getName();
        }
        //this asset is not declared in 'packageName', so we return 'packages/{source_package}/name'
        return "packages/" + getSourceDescription() + "/" + getName();
    }

    @Nullable
    @Override
    public String typeText() {
        return null;
    }

    @NotNull
    private String getCacheKey() {
        VirtualFile file = getFile();
        //      Add the timestamp of the file to the cache key so that when the file changes, we can get different icon
        String fullName = getSourceDescription() + "/" + getName();
        return file == null ? fullName : (fullName + file.getTimeStamp());
    }

    @Nullable
    @Override
    public Icon icon() {
        VirtualFile file = getFile();
        return IconDecorator.get(file, getCacheKey());
    }
}
