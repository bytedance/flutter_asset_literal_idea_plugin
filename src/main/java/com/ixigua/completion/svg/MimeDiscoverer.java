package com.ixigua.completion.svg;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface provides utility methods for detecting MIME type
 *
 * @volantis-api-include-in InternalAPI
 */
public interface MimeDiscoverer {

    /**
     * Get the MIME type for specified stream of bytes.
     *
     * @param data byte stream
     * @return String representing the asset mime type
     */
    public String discoverMimeType(byte[] data);

    /**
     * Get the MIME type for specified stream of bytes and file extension.
     *
     * @param data byte stream
     * @param fileExtension the filename extension (can be null)
     * @return String representing the asset mime type
     */
    public String discoverMimeType(byte[] data, String fileExtension);

    /**
     * Get the MIME type for specified input stream. If mark is supported then
     * the InputStream will be returned to the position it had on calling the
     * method (note that previous marks will be lost). If the InputStream does
     * not support mark then the stream will be closed prior to the method
     * returning.
     *
     * @param stream input stream
     * @return String representing the asset mime type
     */
    public String discoverMimeType(InputStream stream) throws IOException;

    /**
     * Get the MIME type for specified input stream and file extension.
     * If mark is supported then the InputStream will be returned to the position it had
     * on calling the method (note that previous marks will be lost). If the InputStream does
     * not support mark then the stream will be closed prior to the method
     * returning.
     *
     * @param stream input stream
     * @param fileExtension the filename extension (can be null)
     * @return String representing the asset mime type
     */
    public String discoverMimeType(InputStream stream, String fileExtension) throws IOException;

    /**
     * Get the MIME type for specified data input. This method returns the
     * ImageInputStream to the position it was in before calling the method.
     *
     * @param data the DataInput
     * @return String representing the asset mime type
     */
    public String discoverMimeType(ImageInputStream data) throws IOException;

    /**
     * Get the MIME type for specified data input. This method returns the
     * ImageInputStream to the position it was in before calling the method.
     *
     * @param data the DataInput
     * @param fileExtension the filename extension (can be null)
     * @return String representing the asset mime type
     */
    public String discoverMimeType(ImageInputStream data, String fileExtension) throws IOException;

    /**
     * Get the MIME type for specified file.
     *
     * @param file file
     * @return String representing the asset mime type
     */
    public String discoverMimeType(File file) throws IOException;

}
