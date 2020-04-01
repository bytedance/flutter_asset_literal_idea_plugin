package com.ixigua.completion.icon;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IconUtil;
import com.ixigua.completion.transform.TransformImage;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IconDecorator {

    private static final int ICON_WIDTH = 32;
    private static final int ICON_HEIGHT = 32;
    private static final Logger LOG = Logger.getInstance(IconDecorator.class);

    //In order to prevent the default icon and user icon from using the same cache key, the default icon is not placed
    // in ICON_CACHE
    private static Icon DEFAULT_FONT_ICON = null;
    private static Icon BLANK_ICON = null;
    private static Map<String, Icon> ICON_CACHE = new HashMap<>();

    @Nullable
    public static Icon get(@Nullable VirtualFile file, @Nullable String cacheKey) {
        Icon icon = cacheKey != null ? ICON_CACHE.get(cacheKey) : null;
        if (icon != null) {
            LOG.info("icon cache hit, key: " + cacheKey);
            return icon;
        }
        if (file == null) {
            return blankIcon();
        }
        BufferedImage iconImage = null;
        try {
            iconImage = ImageIO.read(new File(file.getPath()));
        } catch (IOException e) {
            LOG.info("read icon failed, file: " + file + " exception: " + e);
        }
        if (iconImage == null) {
            return blankIcon();
        }

        icon = create(iconImage, cacheKey);
        LOG.info("created new icon, key: " + cacheKey);
        return icon;
    }

    @Nullable
    private static Icon create(@Nullable BufferedImage image, @Nullable String cacheKey) {
        Icon icon = null;
        try {
            Image outputImage = TransformImage.resizeAspectFitCenter(image, ICON_WIDTH, ICON_HEIGHT);
            icon = IconUtil.createImageIcon(outputImage);
            if (icon != null && cacheKey != null) {
                ICON_CACHE.put(cacheKey, icon);
            }
        } catch (Exception e) {
            LOG.error("create icon failed " + e);
        }
        return icon;
    }

    public static Icon defaultFontIcon() {
        if (DEFAULT_FONT_ICON != null) {
            return DEFAULT_FONT_ICON;
        }
        BufferedImage iconImage = null;
        try {
            iconImage = ImageIO.read(IconDecorator.class.getResourceAsStream("font_icon.png"));
        } catch (Exception e) {
            LOG.error("read font icon failed " + e);
        }
        DEFAULT_FONT_ICON = create(iconImage, null);
        return DEFAULT_FONT_ICON;
    }

    public static Icon blankIcon() {
        if (BLANK_ICON != null) {
            return BLANK_ICON;
        }
        BufferedImage iconImage = null;
        try {
            iconImage = ImageIO.read(IconDecorator.class.getResourceAsStream("blank_icon.png"));
        } catch (Exception e) {
            LOG.error("read font icon failed " + e);
        }
        BLANK_ICON = create(iconImage, null);
        return BLANK_ICON;
    }
}
