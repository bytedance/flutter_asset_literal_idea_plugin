package com.ixigua.completion.contributor;

import com.intellij.openapi.vfs.VirtualFile;
import com.ixigua.completion.pubspec.PubspecUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Every time the user triggers completion, we will create a context, which contains some information of the current
// project to avoid calculating this information multiple times
public class CompletionContext {

    // The pubspec file of the project to which the file the user is editing belongs
    private VirtualFile pubspec;
    // This is the string we need to complete
    private String prefix;
    // The name of current project
    private String packageName;
    // Structured information parsed from the pubspec file
    private Map<String, Object> pubspecInfo;
    // completion context of all packages that the current project depends on
    private Map<String, CompletionContext> children;

    public CompletionContext(@NotNull VirtualFile pubspec, @NotNull String prefix) {
        this.pubspec = pubspec;
        this.prefix = prefix;
    }

    @NotNull
    public VirtualFile getPubspec() {
        return pubspec;
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    @NotNull
    public String getPackageName() {
        if (packageName == null) {
            packageName = PubspecUtil.getPackageName(getPubspecInfoOfCurrentProject());
        }
        return packageName;
    }

    @NotNull
    public Map<String, Object> getPubspecInfoOfCurrentProject() {
        if (pubspecInfo == null) {
            pubspecInfo = PubspecUtil.getPubspecYamlInfo(pubspec);
        }
        if (pubspecInfo == null) {
            pubspecInfo = Collections.emptyMap();
        }
        return pubspecInfo;
    }

    @NotNull
    public Map<String, CompletionContext> getChildren() {
        if (children == null) {
            children = new HashMap<>();
            PubspecUtil.findAllDependentPubspecFiles(pubspec).forEach((s, virtualFile) -> {
                if (virtualFile == null) {
                    return;
                }
                children.put(s, new CompletionContext(virtualFile, getPrefix()));
            });
        }
        return children;
    }

    @Nullable
    public CompletionContext getChild(String packageName) {
        return getChildren().get(packageName);
    }

}
