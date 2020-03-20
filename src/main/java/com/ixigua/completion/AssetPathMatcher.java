package com.ixigua.completion;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.util.text.EditDistance;
import org.assertj.core.annotations.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AssetPathMatcher extends PrefixMatcher {

    protected AssetPathMatcher(String prefix) {
        super(prefix);
    }

    @Override
    public boolean prefixMatches(@NotNull String name) {
        if (name.isEmpty()) {
            return false;
        }
        int currentPrefixCharIndex = 0;
        for (char c :
                name.toCharArray()) {
            if (currentPrefixCharIndex >= myPrefix.length()) {
                break;
            }
            char currentPrefixChar = myPrefix.charAt(currentPrefixCharIndex);
            if (c == currentPrefixChar) {
                currentPrefixCharIndex += 1;
                System.out.println("match " + c + " at " + currentPrefixCharIndex);
            }
        }
        boolean matched = currentPrefixCharIndex >= myPrefix.length();
        if (matched) {
            System.out.println("prefix " + myPrefix + " matched " + name);
        }
        return matched;
    }

    @NotNull
    @Override
    public PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
        return new AssetPathMatcher(prefix);
    }
}
