package com.ixigua.completion.svg;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is responsible for wrapping <code> javax.imageio.stream.ImageInputStream
 * </code> with an <code> InputStream </code> thereby making it compatible with
 * APIs that expect images to be read from a standard <code> InputStream
 * </code>.
 */
public class ImageInputStreamAdaptor extends InputStream {

    /**
     * The stream to be wrapped
     */
    private ImageInputStream imageInputStream;

    /**
     * Creates a wrapper compatable with <code> InputStream </code> for the
     * supplied ImageInputStream.
     *
     * @param theImageInputStream the stream to be wrapped for compatability
     *                            with <code> InputStream </code>
     */
    public ImageInputStreamAdaptor(ImageInputStream theImageInputStream) {
        imageInputStream = theImageInputStream;
    }

    // Javadoc inherited
    @Override
    public int available() throws IOException {
        return super.available();
    }

    // Javadoc inherited
    @Override
    public int read() throws IOException {
        return imageInputStream.read();
    }

    // Javadoc inherited
    @Override
    public void close() throws IOException {

        // This class was originally created to enable an ImageInputStream
        // to be used as input to a <code>TranscoderInput</code>
        // defined in the Batik API.

        // During the transcoding process, which is contained in the ImageIO
        // reader plugin (<code> SVGImageReader </code>) the input stream
        // is closed.  This causes an IOException to be thrown when the
        // SVGImageReader is invoked via a call to ImageIO.read as the last
        // stage of the read operation is to close the input stream
        // (which has already been closed by the transcoding process).

        // Hence this method is not implemented to avoid the IOException being
        // thrown.
    }

    // Javadoc inherited
    @Override
    public synchronized void reset() throws IOException {
        imageInputStream.reset();
    }

    // Javadoc inherited
    @Override
    public boolean markSupported() {
        return false;
    }

    // Javadoc inherited
    @Override
    public synchronized void mark(int readlimit) {
        // No need to implement as mark is not supported by this class.
    }

    // Javadoc inherited
    @Override
    public long skip(long n) throws IOException {
        return imageInputStream.skipBytes(n);
    }

    // Javadoc inherited
    @Override
    public int read(byte b[]) throws IOException {
        return imageInputStream.read(b);
    }

    // Javadoc inherited
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return imageInputStream.read(b, off, len);
    }

}
