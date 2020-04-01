package com.ixigua.completion.svg;

import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Class responsible for parsing mapping file of extension->mime type pairs.
 *
 */
public class ExtensionMatchParser {

    /**
     * The Logger.
     */
    private static final Logger LOGGER = Logger.getInstance(ExtensionMatchParser.class);

    /**
     * The name of the resource that contains extensions mapping.
     */
    private static final String EXTENSIONS_MAP_RESOURCE = "src/extensions.properties";

    /**
     * The map with extension -> mime type mapping.
     */
    private Properties map = new Properties();

    /**
     * Constructor.
     */
    public ExtensionMatchParser() {
        try {
            InputStream is = getExtensionMapStream();
            map.load(is);
        } catch (IllegalStateException ise) {
            LOGGER.error(ise);
        } catch (IOException ie) {
            LOGGER.error(ie);
        }
    }

    /**
     * Returns stream of extension mapping file.
     *
     * @return a non-null BufferedReader
     * @throws IllegalStateException if the resource isn't on the classpath
     */
    private InputStream getExtensionMapStream() {
        InputStream in = ExtensionMatchParser.class
                .getResourceAsStream(EXTENSIONS_MAP_RESOURCE);
        if (in == null) {
            throw new IllegalStateException(
                    "Unable to load extension->mime map file: " + EXTENSIONS_MAP_RESOURCE);
        }

        return in;
    }

    /**
     * Get a list of the matchers that have been created from parsing the map file.
     *
     * @return a non-null Map of Matchers
     */
    public Map<Object,Object> getMatchers() {
        return map;
    }
}