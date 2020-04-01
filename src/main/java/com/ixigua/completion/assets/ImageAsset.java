package com.ixigua.completion.assets;

import com.intellij.openapi.diagnostic.Logger;
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
    public String lookupString() {
        return getName();
    }

    @Nullable
    @Override
    public String typeText() {
        return null;
    }

    @Nullable
    @Override
    public Icon icon() {
        VirtualFile file = getFile();
//      Add the timestamp of the file to the cache key so that when the file changes, we can get different icon
        String cacheKey = file == null ? lookupString() : (lookupString() + file.getTimeStamp());
        return IconDecorator.get(file, cacheKey);
    }
}
