package com.ixigua.completion.transform;

import com.intellij.util.ui.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TransformImage {
    private static Rectangle getAspectRatioRect(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Rectangle((bound_width - new_width)/2, (bound_height - new_height)/2, new_width, new_height);
    }

    public static Image resizeAspectFitCenter(BufferedImage originalImage, int boundWidth, int boundHeight) {
        int type = BufferedImage.TYPE_INT_ARGB;


        BufferedImage resizedImage = ImageUtil.createImage(boundWidth, boundHeight, type);
        Graphics2D g = resizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle rectangle = getAspectRatioRect(new Dimension(originalImage.getWidth(), originalImage.getHeight()), new Dimension(boundWidth,boundHeight));
        g.drawImage(originalImage, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);
        g.dispose();


        return resizedImage;
    }
}
