package com.ixigua.completion.svg;

import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class responsible for parsing magic file and building a list of Matcher
 * objects
 *
 */
public class MatchParser {

    private static final Logger LOGGER = Logger.getInstance(MatchParser.class);

    private static final String MAGIC_FILE_RESOURCE = "magic.mime";

    private static final char ADDITIONAL_MATCH_CHAR = '>';

    private List<Matcher> matchers = new ArrayList<Matcher>();

    private int maxDataLength = 0;

    public MatchParser() {
        initialize();
    }

    /**
     * Create hierarchy of machers: -parse file witch matches. Each line of the
     * file specifies a test to be performed.
     */
    private void initialize() {
        try {
            BufferedReader br = getStreamReader();

            String line = null;
            Matcher matcher = null;
            Match match = null;
            Pattern p = Pattern.compile("^(>?&?\\d+)" + // offset
                    "[ \\t]+" + // separator
                    "([\\w]+)" + // type
                    "/?([bcB]*)" + // property (ie. case insensitive)
                    "&?([\\w]*)" + // bitmask
                    "[ \\t]+" + // separator
                    "([=><!]?)" + // comparator
                    "(([\\w#?.<!_\\-:%&/+\\*\"'.@;`^\\[\\]\\(\\){}]*(\\\\ )*(\\\\)*)*)" + // test value
                    "[ \\t]*" + // separator
                    "([\\w\\-+/.]*)");                  // mimetype
            while ((line = br.readLine()) != null) {
                // check if line is not commented
                line = line.trim();
                if (!line.startsWith("#")) {
                    java.util.regex.Matcher m = p.matcher(line);
                    if (m.find()) {
                        match = new Match();
                        matcher = new Matcher();
                        matcher.setMatch(match);
                        match.setType(m.group(2));
                        setCaseInsensitive(match, m.group(3));
                        setBitMask(match, m.group(4));
                        setComparator(match, m.group(5));
                        String test = m.group(6).replaceAll("\\\\<", "<")
                                .replaceAll("\\\\ ", " ")
                                .replaceAll("\\\\\\\\", "\\\\");
                        setTest(test, match);
                        match.setMimeType(m.group(10));
                        setOffset(match, matcher, m.group(1));
                        updateMaxDataLength(match);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private void setTest(String test, Match match) {
        ByteBuffer buffer = null;
        if (test.indexOf("\\x") != -1) {
            buffer = convertHexals(test);
        } else {
            buffer = convertOctals(test);
        }
        match.setTest(buffer);
    }

    private void setComparator(Match match, String comparatorField) {
        if (!"".equals(comparatorField)) {
            match.setComparator(comparatorField.charAt(0));
        }
    }

    private void setBitMask(Match match, String bitMaskField) {
        if (!"".equals(bitMaskField)) {
            match.setBitmask(Long.decode(bitMaskField).longValue());
        }
    }

    private void setCaseInsensitive(Match match, String property) {
        if (!"".equals(property) && property.indexOf('c') != -1) {
            match.setCaseInsensitive(true);
        }
    }

    private void setOffset(Match match, Matcher matcher, String offSetField) {
        char first = offSetField.charAt(0);
        if (ADDITIONAL_MATCH_CHAR == first) {
            // for match continuation
            match.setOffset(Integer.parseInt(offSetField.substring(1).trim()));
            Matcher mainMatcher = matchers.get(matchers.size() - 1);
            mainMatcher.addMatcher(matcher);
        } else {
            // for first match line
            match.setOffset(Integer.parseInt(offSetField));
            matchers.add(matcher);
        }
    }

    /**
     * Tries to read the magic.mime file resource and returns a BufferedReader
     * ready to read the resource
     *
     * @return a non-null BufferedReader
     *
     * @throws IllegalStateException if the resource isn't on the classpath
     */
    private BufferedReader getStreamReader() {
        InputStream in = MatchParser.class.getResourceAsStream(
                MAGIC_FILE_RESOURCE);
        if (in == null) {
            throw new IllegalStateException(
                    "Unable to load magic file: " + MAGIC_FILE_RESOURCE);
        }

        return new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Get a list of the matchers that have been created from parsing the magic
     * file
     *
     * @return a non-null List of Matchers
     */
    public List<Matcher> getMatchers() {
        return matchers;
    }


    /**
     * Returns a ByteBuffer with any hex-encoded (written as \xdd) parts of the
     * specified string decoded to the actual byte value
     *
     * @param s a string with encoded hex
     * @return a ByteBuffer without any hex encoding
     */
    public ByteBuffer convertHexals(String s) {
        int beg = 0;
        int end = 0;
        int chr;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        while ((end = s.indexOf("\\x", beg)) != -1) {
            for (int z = beg; z < end; z++) {
                buf.write((int) s.charAt(z));
            }
            if (end + 4 <= s.length()) {
                try {
                    chr =
                            Integer.decode(
                                    s.substring(end, end + 4).replaceAll("\\\\x",
                                            "0x"))
                                    .intValue();
                    buf.write(chr);
                    beg = end + 4;
                    end = beg;
                } catch (NumberFormatException nfe) {
                    buf.write((int) '\\');
                    beg = end + 1;
                    end = beg;
                }
            } else {
                buf.write((int) '\\');
                beg = end + 1;
                end = beg;
            }
        }

        if (end < s.length()) {
            for (int z = beg; z < s.length(); z++) {
                buf.write((int) s.charAt(z));
            }
        }

        try {
            ByteBuffer b = ByteBuffer.allocate(buf.size());
            return b.put(buf.toByteArray());
        } catch (Exception e) {
            return ByteBuffer.allocate(0);
        }
    }


    /**
     * Returns a ByteBuffer with any octal-encoded(written as \ddd) parts of
     * the specified string having been decoded to the actual byte values
     *
     * @param s a string with encoded octals
     * @return a ByteBuffer without any octal encoding
     */
    public ByteBuffer convertOctals(String s) {
        int beg = 0;
        int end = 0;
        int chr;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        while ((end = s.indexOf('\\', beg)) != -1) {
            if (s.charAt(end + 1) != '\\') {
                for (int z = beg; z < end; z++) {
                    buf.write((int) s.charAt(z));
                }
                if (end + 4 <= s.length()) {
                    try {
                        chr = Integer.parseInt(s.substring(end + 1, end + 4),
                                8);
                        buf.write(chr);
                        beg = end + 4;
                        end = beg;
                    } catch (NumberFormatException nfe) {
                        buf.write((int) '\\');
                        beg = end + 1;
                        end = beg;
                    }
                } else if (s.charAt(end + 1) == '0') {
                    chr = Integer.parseInt(s.substring(end + 1, end + 2), 8);
                    buf.write(chr);
                    beg = end + 2;
                    end = beg;
                } else {
                    buf.write((int) '\\');
                    beg = end + 1;
                    end = beg;
                }
            } else {
                buf.write((int) '\\');
                beg = end + 1;
                end = beg;
            }
        }

        if (end < s.length()) {
            for (int z = beg; z < s.length(); z++) {
                buf.write((int) s.charAt(z));
            }
        }
        try {
            ByteBuffer b = ByteBuffer.allocate(buf.size());
            return b.put(buf.toByteArray());
        } catch (Exception e) {
            return ByteBuffer.allocate(0);
        }
    }

    /**
     * Get max data length expected by matcher to test data.
     *
     * @return the maximum number of bytes that this expects to have to parse
     */
    public int getMaxDataLength() {
        return maxDataLength;
    }

    /**
     * Update max data length expected by matcher to test data.
     *
     * @param match
     */
    private void updateMaxDataLength(Match match) {
        int lastIndex = match.getOffset() + match.getTest().array().length;
        if (lastIndex > maxDataLength) {
            maxDataLength = lastIndex;
        }
    }
}
