package com.ixigua.completion.pubspec;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.util.Map;

public class PubspecUtil {

    private static final Key<Pair<Long, Map<String, Object>>> MOD_STAMP_TO_PUBSPEC_NAME = Key.create("MOD_STAMP_TO_PUBSPEC_NAME");

    public static VirtualFile findPubspecYamlFile(@NotNull Project project, @NotNull VirtualFile currentFile) {
        return PubspecYamlUtil.findPubspecYamlFile(project, currentFile);
    }

    @Nullable
    public static Map<String, Object> getPubspecYamlInfo(@NotNull VirtualFile pubspecYamlFile) {

        Pair<Long, Map<String, Object>> data = (Pair<Long, Map<String, Object>>)pubspecYamlFile.getUserData(MOD_STAMP_TO_PUBSPEC_NAME);
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        Document cachedDocument = documentManager.getCachedDocument(pubspecYamlFile);
        Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : pubspecYamlFile.getModificationCount();
        Long cachedTimestamp = (Long)Pair.getFirst(data);
        if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
            data = null;
            pubspecYamlFile.putUserData(MOD_STAMP_TO_PUBSPEC_NAME, null);

            try {
                Map<String, Object> pubspecYamlInfo;
                if (cachedDocument != null) {
                    pubspecYamlInfo = loadPubspecYamlInfo(cachedDocument.getText());
                } else {
                    pubspecYamlInfo = loadPubspecYamlInfo(VfsUtilCore.loadText(pubspecYamlFile));
                }

                if (pubspecYamlInfo != null) {
                    data = Pair.create(currentTimestamp, pubspecYamlInfo);
                    pubspecYamlFile.putUserData(MOD_STAMP_TO_PUBSPEC_NAME, data);
                }
            } catch (IOException ignored) {
            }
        }

        return (Map<String, Object>)Pair.getSecond(data);
    }

    @Nullable
    private static Map<String, Object> loadPubspecYamlInfo(@NotNull String pubspecYamlFileContents) {

        Yaml yaml = new Yaml(new SafeConstructor(), new Representer(), new DumperOptions(), new Resolver() {
            protected void addImplicitResolvers() {
                this.addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
                this.addImplicitResolver(Tag.NULL, NULL, "~nN\u0000");
                this.addImplicitResolver(Tag.NULL, EMPTY, (String)null);
                this.addImplicitResolver(new Tag("tag:yaml.org,2002:value"), VALUE, "=");
                this.addImplicitResolver(Tag.MERGE, MERGE, "<");
            }
        });

        try {
            return yaml.load(pubspecYamlFileContents);
        } catch (Exception var3) {
            return null;
        }
    }
}
