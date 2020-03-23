package com.ixigua.completion.svg;

import org.apache.batik.transcoder.TranscoderOutput;

import java.awt.image.BufferedImage;

/**
 * This class is responsible for defining a <code>TranscoderOuput</code> class
 * that is capable of storing a buffered image that is the result of
 * transcoding an SVG file.
 */
public class BufferedImageTranscoderOutput extends TranscoderOutput {

    /**
     * The resultant image of transcoding the SVG.
     */
    private BufferedImage image;

    /**
     * Initializes the new instance.
     */
    public BufferedImageTranscoderOutput() {
        super();
    }

    /**
     * Sets the BufferedImage.
     *
     * @param image the image to be stored.
     */
    public void setBufferedImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Returns the BufferedImage currently stored.
     *
     * @return the resultant image of the transcoding process.
     */
    public BufferedImage getBufferedImage() {
        return image;
    }
}
