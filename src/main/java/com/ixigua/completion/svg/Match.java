package com.ixigua.completion.svg;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class contains data to match.
 *
 */
public class Match {

    private int offset;

    /**
     * possible types: byte, short, long, string, beshort, belong, leshort,
     * lelong See magic.mime and *nix man pages for more information
     */
    private String type = "";

    private ByteBuffer test;

    private String mimeType;

    private long bitmask = 0xFFFFFFFFL;

    private boolean caseInsensitive = false;

    private char comparator = '=';

    private List<Match> matches = new ArrayList<Match>(0);

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ByteBuffer getTest() {
        return test;
    }

    public void setTest(ByteBuffer test) {
        this.test = test;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getBitmask() {
        return bitmask;
    }

    public void setBitmask(long bitmask) {
        this.bitmask = bitmask;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void clearMatches() {
        matches.clear();
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public char getComparator() {
        return comparator;
    }

    public void setComparator(char comparator) {
        this.comparator = comparator;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("offset:").append(offset);
        b.append("\ttype:").append(type);
        b.append("\ttest:").append(new String(test.array()));
        b.append("\tmimeType:").append(mimeType);
        b.append("\tignoreCase:").append(caseInsensitive);
        b.append("\tbitmask:").append(bitmask);
        b.append("\tcomparator:").append(comparator);
        return b.toString();
    }

}
