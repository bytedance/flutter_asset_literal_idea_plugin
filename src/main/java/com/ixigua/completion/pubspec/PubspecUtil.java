package com.ixigua.completion.pubspec;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PubspecUtil {

    private static final Key<Pair<Long, Map<String, Object>>> MOD_STAMP_TO_PUBSPEC_NAME = Key.create("MOD_STAMP_TO_PUBSPEC_NAME");
    private static final Logger LOG = Logger.getInstance(PubspecUtil.class);

    public static VirtualFile findPubspecYamlFile(@NotNull Project project, @NotNull VirtualFile currentFile) {
        return PubspecYamlUtil.findPubspecYamlFile(project, currentFile);
    }

    @NotNull
    public static String getPackageName(@NotNull Map<String, Object> pubspecInfo) {
        Object packageNameDeclaration = pubspecInfo.get("name");
        return packageNameDeclaration instanceof String ? (String) packageNameDeclaration : "";
    }


    @Nullable
    public static Map<String, Object> getPubspecYamlInfo(@NotNull VirtualFile pubspecYamlFile) {

        Pair<Long, Map<String, Object>> data = pubspecYamlFile.getUserData(MOD_STAMP_TO_PUBSPEC_NAME);
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        Document cachedDocument = documentManager.getCachedDocument(pubspecYamlFile);
        Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : pubspecYamlFile.getModificationCount();
        Long cachedTimestamp = Pair.getFirst(data);
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

        return Pair.getSecond(data);
    }

    @Nullable
    private static Map<String, Object> loadPubspecYamlInfo(@NotNull String pubspecYamlFileContents) {

        Yaml yaml = new Yaml(new SafeConstructor(), new Representer(), new DumperOptions(), new Resolver() {
            protected void addImplicitResolvers() {
                this.addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
                this.addImplicitResolver(Tag.NULL, NULL, "~nN\u0000");
                this.addImplicitResolver(Tag.NULL, EMPTY, null);
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

    public static Map<String, VirtualFile> findAllDependentPubspecFiles(@NotNull VirtualFile pubspecYamlFile) {
        // flutter project must include a file named ".packages", this file records all dependency information.
        VirtualFile packagesFile = pubspecYamlFile.getParent().findFileByRelativePath(".packages");
        if (packagesFile == null) {
            LOG.info("\".packages\" file is null!");
            return Collections.emptyMap();
        }
        if (!packagesFile.isValid()) {
            LOG.info("\".packages\" file is invalid!");
            return Collections.emptyMap();
        }
        if (!packagesFile.exists() || packagesFile.isDirectory()) {
            LOG.info("\".packages\" file is either a folder or does not exist!");
            return Collections.emptyMap();
        }
        //The format of the information in ".packages" file is as follows:
        //package_name:file:///path/to/package_name/lib/
        //each line can be spilt by ":" into 2 parts: package name and url
        // note: the first line is comment should be ignored, the last line is current package should be ignored too.
        Map<String, VirtualFile> ret = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(packagesFile.getInputStream()));
            while (reader.ready()) {
                String line = reader.readLine().trim();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] pair = line.split(":", 2);
                if (pair.length < 2) {
                    continue;
                }
                ProgressManager.checkCanceled();
                String path = pair[1];
                path = path.replace("lib/", "pubspec.yaml");
                VirtualFile dep;
                if (path.startsWith("file:")) {
                    File depPubspecFile = new File(new URI(path));
                    dep = LocalFileSystem.getInstance().findFileByIoFile(depPubspecFile);
                } else {
                    dep = pubspecYamlFile.getParent().findFileByRelativePath(path);
                }
                ret.put(pair[0], dep);
            }
        } catch (Exception e) {
            LOG.info("cannot read \".packages\" file, error: " + e);
        }
        LOG.debug("packages file content: " + ret);
        return ret;
    }
}
