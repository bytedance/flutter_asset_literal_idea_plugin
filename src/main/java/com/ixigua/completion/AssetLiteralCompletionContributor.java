package com.ixigua.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.intellij.util.text.EditDistance;
import com.jetbrains.lang.dart.DartTokenTypes;
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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER;

public class AssetLiteralCompletionContributor extends CompletionContributor {

    private static final Key<Pair<Long, Map<String, Object>>> MOD_STAMP_TO_PUBSPEC_NAME = Key.create("MOD_STAMP_TO_PUBSPEC_NAME");

    public AssetLiteralCompletionContributor() {
           extend(CompletionType.BASIC, PlatformPatterns.psiElement(DartTokenTypes.REGULAR_STRING_PART), new CompletionProvider<CompletionParameters>() {

               @Override
               protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                   ProgressManager.checkCanceled();
                   String text = parameters.getPosition().getText().replaceFirst(DUMMY_IDENTIFIER, "");
//                   System.out.println("asset literal prefix string " + text);
                   if (text.isEmpty()) {
                       return;
                   }
                   List<String> allPaths = allAssetPaths(parameters);

                   if (allPaths == null) {
                       return;
                   }
                   List<String> filteredPaths = filterPaths(allPaths, text);
                   if (filteredPaths == null) {
                       return;
                   }
                   if (filteredPaths.isEmpty()) {
                       return;
                   }

                   List<String> sortedPaths = sortedAssetPaths(text, filteredPaths);
                   result = result.withPrefixMatcher(new AssetPathMatcher(text));
                   for (String path :
                           sortedPaths) {
                       result.addElement(LookupElementBuilder.create(path));
                   }
               }

           });
    }


    private static  List<String> filterPaths(@NotNull List<String> paths, @NotNull String prefix) {
        if (paths.isEmpty()) {
            return null;
        }
        AssetPathMatcher matcher = new AssetPathMatcher(prefix);
        List<String> ret = new ArrayList<String>(paths);
        ret.removeIf(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !matcher.prefixMatches(s);
            }
        });
        return ret;
    }

    private static List<String> allAssetPaths(@NotNull CompletionParameters parameters) {
        // 找到 pubspec 文件
        VirtualFile pubspec =  PubspecYamlUtil.findPubspecYamlFile(parameters.getPosition().getProject(), parameters.getOriginalFile().getVirtualFile());
//        System.out.println("pubspec file " + pubspec);
        if (pubspec == null) {
            return new ArrayList<String>();
        }
        // 获得 pubspec 中 assets 自动对应的信息
        Map<String, Object> pubInfo = getPubspecYamlInfo(pubspec);
        if (pubInfo == null) {
            return null;
        }
        final List<String> assets = new ArrayList<String>();

        pubInfo.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                if (!s.equalsIgnoreCase("flutter")) {
                    return;
                }
                if (!(o instanceof Map)) {
                    return;
                }
                Object ats = ((Map)o).get("assets");
//                System.out.println("assets class " + ats.getClass());
//                System.out.println("assets in flutter " + ats + " key " + s);
                if (!(ats instanceof List)) {
                    return;
                }
                assets.addAll((Collection<String>) ats);
            }
        });
//        System.out.println("all asssets " + assets);
        // 拿到 assets 对应的所有文件
        ArrayList<String> ret = new ArrayList<String>();
        assets.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                VirtualFile parent = pubspec.getParent();
                VirtualFile child = parent.findChild(s);
                if (child == null) {
                    return;
                }
                String parentPath = parent.getPath() + "/";
//                System.out.println("find assets file " + child);
                if (!child.exists()) {
                    return;
                }
                ret.addAll(flattenRelativePaths(child, parentPath));
            }
        });

//        System.out.println("find all asset relative paths " + ret);
        return ret;
    }

    private static List<String> sortedAssetPaths(String prefix, List<String> paths) {
        paths.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int o1d = EditDistance.levenshtein(prefix, o1, false);
                int o2d = EditDistance.levenshtein(prefix, o2, false);
                return o1d - o2d;
            }
        });
        return paths;
    }

    private static List<String> flattenRelativePaths(@NotNull VirtualFile directory, @NotNull String relativeTo) {
        List<String> ret = new ArrayList<String>();
        if (!directory.isDirectory()) {
            ret.add(directory.getPath());
            return ret;
        }

        VirtualFileVisitor.Result result = VfsUtilCore.visitChildrenRecursively(directory, new VirtualFileVisitor<Object>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.is(VFileProperty.HIDDEN)) {
                    return true;
                }
                if (file.getName().equalsIgnoreCase(".DS_Store")) {
                    return true;
                }
                ret.add(file.getPath().replaceFirst(relativeTo, ""));
                return true;
            }
        });

        return ret;
    }

    @Nullable
    private static Map<String, Object> getPubspecYamlInfo(@NotNull VirtualFile pubspecYamlFile) {

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
