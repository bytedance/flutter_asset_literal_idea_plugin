package com.ixigua.completion.svg;

import com.intellij.util.ui.UIUtil;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This class is an <tt>ImageTranscoder</tt> that produces a BufferedImage
 * image.
 */
public class BufferedImageTranscoder extends ImageTranscoder {

    /**
     * Initializes a BufferedImageTranscoder which converts an SVG to a
     * BufferedImage in which transparent pixels are replaced with the colour
     * white.
     */
    public BufferedImageTranscoder() {
        // ensure that transparent pixels are converted to white ones.
        // The default is black.
        hints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.white);
    }

    /**
     * Writes the specified image to the specified output.
     *
     * @param img                 the image to write
     * @param output              the output where to store the image
     */
    @Override
    public void writeImage(BufferedImage img,
                           TranscoderOutput output)
            throws TranscoderException {

        // We want the SVG in the form of a BufferedImage so write the
        // supplied image to the output servlet

        // Need to check that the cast is legal.
        if (output instanceof BufferedImageTranscoderOutput) {
            BufferedImageTranscoderOutput transcoderOutput =
                    (BufferedImageTranscoderOutput) output;
            transcoderOutput.setBufferedImage(img);
        } else {

            throw new IllegalArgumentException(
                    "Expected a BufferedImageTranscoderOutput");
        }
    }


    /**
     * Creates a new RGB image with the specified dimension.
     *
     * @param width  the image width in pixels
     * @param height the image height in pixels
     */
    @Override
    public BufferedImage createImage(int width, int height) {
        final BufferedImage image = UIUtil.createImage(width, height, BufferedImage.TYPE_INT_RGB);
        return image;
    }
}
