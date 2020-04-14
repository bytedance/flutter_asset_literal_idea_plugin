package com.ixigua.completion.assets;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.ixigua.completion.icon.IconDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FontAsset extends Asset {

    private static final Logger LOG = Logger.getInstance(FontAsset.class);
    static final String PRE_INSTALLED_IOS_8_SOURCE = "iOS 8 Font";
    static final String PRE_INSTALLED_IOS_9_SOURCE = "iOS 9 Font";
    static final String PRE_INSTALLED_Android_SOURCE = "Android Font";

    public FontAsset(@NotNull String name, @Nullable VirtualFile file, @Nullable String sourceDescription) {
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
        if (getSourceDescription().contentEquals(PRE_INSTALLED_IOS_8_SOURCE)) {
            return getName();
        }
        if (getSourceDescription().contentEquals(PRE_INSTALLED_IOS_9_SOURCE)) {
            return getName();
        }
        if (getSourceDescription().contentEquals(PRE_INSTALLED_Android_SOURCE)) {
            return getName();
        }
        //this font is not declared in 'packageName', so we return 'packages/{source_package}/name'
        return "packages/" + getSourceDescription() + "/" + getName();
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
