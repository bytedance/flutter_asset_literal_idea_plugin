package com.ixigua.completion.assets;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.ixigua.completion.icon.IconDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FontAsset extends Asset {

    private static final Logger LOG = Logger.getInstance(FontAsset.class);

    public FontAsset(@NotNull String name, @Nullable VirtualFile file, @Nullable String sourceDescription) {
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
        return getSourceDescription();
    }

    @Nullable
    @Override
    public Icon icon() {
        return IconDecorator.defaultFontIcon();
    }
}
