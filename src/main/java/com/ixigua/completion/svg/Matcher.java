package com.ixigua.completion.svg;

import com.intellij.openapi.diagnostic.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a mechanism for matching byte streams and detecting a given MIME
 * type. There may be multiple ways of recognising a byte stream is a
 * particular MIME type, so a Matcher can contain a list of Matchers that
 * detect the same file type, but by a different method.
 *
 */
public class Matcher {

    private static final Logger LOGGER = Logger.getInstance(Matcher.class);

    private Match match;

    private List<Matcher> matchers = new ArrayList<Matcher>(0);

    /**
     * Test data stream mime type.
     *
     * @param data stream of bytes to test
     * @return true if data pass test, otherwise false.
     */
    public boolean test(byte[] data) {
        boolean matched = false;
        int length = 0;
        int offset = match.getOffset();
        String type = match.getType();
        if ("byte".equals(type)) {
            length = 1;
        } else if (type.endsWith("short")) {
            length = 4;
        } else if (type.endsWith("long")) {
            length = 8;
        } else if ("string".equals(type)) {
            length = match.getTest().capacity();
        } else {
            throw new IllegalStateException("Unrecognised type:" + type);
        }

        byte[] buf = new byte[length];
        if (offset + length <= data.length) {
            System.arraycopy(data, offset, buf, 0, length);
            if (testInternal(buf)) {
                matched = true;
                if (matchers.size() > 0) {
                    matched = false;
                    for (int i = 0; i < matchers.size() && !matched; i++) {
                        Matcher m = matchers.get(i);
                        if (m.test(data)) {
                            match.addMatch(m.getMatch());
                            matched = true;
                        }
                    }
                }
            }
        }

        return matched;
    }

    /**
     * @param data stream of bytes to test
     * @return true if data pass test, otherwise false.
     */
    private boolean testInternal(byte[] data) {
        boolean matched = false;
        String type = match.getType();
        byte[] test = match.getTest().array();
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        if (type != null && test != null) {
            if (type.equals("string")) {
                buffer.put(data);
                matched = testString(buffer);
            } else if (type.equals("byte")) {
                buffer.put(data);
                matched = testByte(buffer);
            } else if (type.equals("short")) {
                buffer.put(data);
                matched = testShort(buffer);
            } else if (type.equals("leshort")) {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.put(data);
                matched = testShort(buffer);
            } else if (type.equals("beshort")) {
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.put(data);
                matched = testShort(buffer);
            } else if (type.equals("long")) {
                buffer.put(data);
                matched = testLong(buffer);
            } else if (type.equals("lelong")) {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.put(data);
                matched = testLong(buffer);
            } else if (type.equals("belong")) {
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.put(data);
                matched = testLong(buffer);
            } else {
                LOGGER.error("unsuported-match-type" + new Object[]{type});
            }
        } else {
            LOGGER.error("invalid-match" + new Object[]{match});
        }
        return matched;
    }

    /**
     * test the data against the test byte
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testByte(ByteBuffer data) {
        boolean matched = false;
        String test = new String(match.getTest().array());
        long bitmask = match.getBitmask();
        char comparator = match.getComparator();
        byte b = data.get(0);
        // apply bitmask before the comparison
        b = (byte) (b & bitmask);
        if ("x".equals(test)) {
            matched = true;
        }
        if (!matched) {
            int tst = Integer.decode(test).byteValue();
            byte t = (byte) (tst & 0xff);
            switch (comparator) {
                case '=':
                    matched = t == b;
                    break;
                case '!':
                    matched = t != b;
                    break;
                case '>':
                    matched = t > b;
                    break;
                case '<':
                    matched = t < b;
                    break;
            }
        }
        return matched;
    }

    /**
     * test the data against the byte array
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testString(ByteBuffer data) {
        boolean matched = false;
        String b = new String(data.array());
        String t = new String(match.getTest().array());
        if (match.isCaseInsensitive()) {
            matched = t.equalsIgnoreCase(b);
        } else {
            matched = t.equals(b);
        }
        return matched;
    }

    /**
     * test the data against a short
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testShort(ByteBuffer data) {
        boolean matched = false;
        short val = byteArrayToShort(data);
        String test = new String(match.getTest().array());
        long bitmask = match.getBitmask();
        char comparator = match.getComparator();
        val = byteArrayToShort(data);
        // apply bitmask before the comparison
        val = (short) (val & (short) bitmask);
        short tst = Integer.decode(test).shortValue();
        switch (comparator) {
            case '=':
                matched = val == tst;
                break;
            case '!':
                matched = val != tst;
                break;
            case '>':
                matched = val > tst;
                break;
            case '<':
                matched = val < tst;
                break;
        }
        return matched;
    }

    /**
     * convert a byte array to a short
     *
     * @param data buffer of byte data
     * @return byte array converted to a short
     */
    private short byteArrayToShort(ByteBuffer data) {
        return data.getShort(0);
    }

    /**
     * test the data against a long
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testLong(ByteBuffer data) {
        boolean matched = false;
        long bitmask = match.getBitmask();
        char comparator = match.getComparator();
        String test = new String(match.getTest().array());
        long val = byteArrayToLong(data);
        // apply bitmask before the comparison
        val = val & bitmask;
        long tst = Long.decode(test).longValue();
        switch (comparator) {
            case '=':
                matched = val == tst;
                break;
            case '!':
                matched = val != tst;
                break;
            case '>':
                matched = val > tst;
                break;
            case '<':
                matched = val < tst;
                break;
        }
        return matched;
    }

    /**
     * convert a byte array to a long
     *
     * @param data buffer of byte data
     * @return byte arrays (high and low bytes) converted to a long value
     */
    private long byteArrayToLong(ByteBuffer data) {
        return (long) data.getInt(0);
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public List<Matcher> getMatchers() {
        return matchers;
    }

    public void addMatcher(Matcher matcher) {
        this.matchers.add(matcher);
    }

    /**
     * @return mime type if data matched, otherwise null
     */
    public String getMimeType() {
        StringBuffer b = new StringBuffer();
        for (Iterator<Match> iter = match.getMatches().iterator(); iter.hasNext();) {
            Match subMatch = iter.next();
            b.append(subMatch.getMimeType());
        }
        if (match.getMatches().size() == 0) {
            b.append(match.getMimeType());
        }
        match.clearMatches();
        if (b.length() == 0) {
            return null;
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(match);
        for (Iterator<Matcher> iter = matchers.iterator(); iter.hasNext();) {
            Matcher matcher = iter.next();
            b.append("\n  > ");
            b.append(matcher);
        }
        return b.toString();
    }

}
