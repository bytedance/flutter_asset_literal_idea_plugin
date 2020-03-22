package com.ixigua.completion;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

public class AssetPathMatcher extends PrefixMatcher {

    private static final Logger LOG = Logger.getInstance(AssetPathMatcher.class);

    protected AssetPathMatcher(String prefix) {
        super(prefix);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssetPathMatcher) {
            return obj.hashCode() == hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPrefix().hashCode() ^ 287936;
    }

    @Override
    public boolean prefixMatches(@NotNull String name) {
        if (name.isEmpty()) {
            return false;
        }
        int currentPrefixCharIndex = 0;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (currentPrefixCharIndex >= getPrefix().length()) {
                break;
            }
            char currentPrefixChar = Character.toLowerCase(getPrefix().charAt(currentPrefixCharIndex));
            if (Character.toLowerCase(c) == currentPrefixChar) {
                currentPrefixCharIndex += 1;
                LOG.info("match " + c + " at " + i);
            }
        }
        boolean matched = currentPrefixCharIndex >= getPrefix().length();
        if (matched) {
            LOG.info("prefix " + getPrefix() + " matched " + name);
        }
        return matched;
    }

    @NotNull
    @Override
    public PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
        return new AssetPathMatcher(prefix);
    }
}
