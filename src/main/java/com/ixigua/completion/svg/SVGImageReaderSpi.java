package com.ixigua.completion.svg;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * This class is responsible for defining the configuration properties of
 * <code> SVGImageReader</code>.
 */
public class SVGImageReaderSpi extends ImageReaderSpi {

    static final String VENDOR_NAME = "iXigua";

    static final String VERSION = "1.0";

    static final String READER_CLASS_NAME = SVGImageReader.class.getName();

    static final String[] NAMES = {"svg", "SVG"};

    static final String[] SUFFIXES = {"svg", "SVG"};

    private static final String XML_MIME_TYPE = "text/xml";

    private static final String SVG_MIME_TYPE = "image/svg+xml";

    static final String[] MIME_TYPES = { SVG_MIME_TYPE };

    static final String[] WRITER_SPI_NAMES = {""};

    // Metadata formats, more information below
    static final boolean SUPPORTS_STANDARD_STREAM_METADATA_FORMAT = false;

    static final String NATIVE_STREAM_METADATA_FORMAT_NAME = null;

    static final String NATIVE_STREAM_METADATA_FORMAT_CLASS_NAME = null;

    static final String[] EXTRA_STREAM_METADATA_FORMAT_NAMES = null;

    static final String[] EXTRA_STREAM_METADATA_FORMAT_CLASS_NAMES = null;

    static final boolean SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT = false;

    static final String NATIVE_IMAGE_METADATA_FORMAT_NAME = null;

    static final String NATIVE_IMAGE_METADATA_FORMAT_CLASS_NAME = null;

    static final String[] EXTRA_IMAGE_METADATA_FORMAT_NAMES = null;

    static final String[] EXTRA_IMAGE_METADATA_FORMAT_CLASS_NAMES = null;

    // Javadoc inherited
    public SVGImageReaderSpi() {
        super(VENDOR_NAME,
                VERSION,
                NAMES,
                SUFFIXES,
                MIME_TYPES,
                READER_CLASS_NAME,
                // The standard input type for this class is ImageInputStream
                STANDARD_INPUT_TYPE,
                WRITER_SPI_NAMES,
                SUPPORTS_STANDARD_STREAM_METADATA_FORMAT,
                NATIVE_STREAM_METADATA_FORMAT_NAME,
                NATIVE_STREAM_METADATA_FORMAT_CLASS_NAME,
                EXTRA_STREAM_METADATA_FORMAT_NAMES,
                EXTRA_STREAM_METADATA_FORMAT_CLASS_NAMES,
                SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT,
                NATIVE_IMAGE_METADATA_FORMAT_NAME,
                NATIVE_IMAGE_METADATA_FORMAT_CLASS_NAME,
                EXTRA_IMAGE_METADATA_FORMAT_NAMES,
                EXTRA_IMAGE_METADATA_FORMAT_CLASS_NAMES);
    }

    // Javadoc inherited
    @Override
    public String getDescription(Locale locale) {
        // @todo later return a localized description
        // Localize as appropriate
        return "iXigua SVG Reader";
    }

    // Javadoc inherited
    @Override
    public boolean canDecodeInput(Object input)
            throws IOException {

        boolean canDecode = false;
        if (input instanceof ImageInputStream) {

            ImageInputStream inputStream = (ImageInputStream) input;

            MimeDiscoverer mimeDiscoverer = new DefaultMimeDiscoverer();
            String mimeType = mimeDiscoverer.discoverMimeType(inputStream);
            if (SVG_MIME_TYPE.equals(mimeType))
            {
                canDecode = true;
            }
            else if (XML_MIME_TYPE.equals(mimeType))
            {
                // check if the file is SVG without a doctype
                try {
                    inputStream.mark();
                    String line = inputStream.readLine();
                    while (line != null && line.indexOf("<svg") == -1)
                    {
                        line = inputStream.readLine();
                    }
                    canDecode = line != null &&
                            line.indexOf("<svg") > -1;
                } finally {
                    inputStream.reset();
                }
            }
        }
        return canDecode;
    }

    // Javadoc inherited
    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new SVGImageReader(this);
    }
}
