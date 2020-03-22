package com.ixigua.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
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

    private static final Logger LOG = Logger.getInstance(AssetLiteralCompletionContributor.class);

    private static final Key<Pair<Long, Map<String, Object>>> MOD_STAMP_TO_PUBSPEC_NAME = Key.create("MOD_STAMP_TO_PUBSPEC_NAME");

    public AssetLiteralCompletionContributor() {
           extend(CompletionType.BASIC, PlatformPatterns.psiElement(DartTokenTypes.REGULAR_STRING_PART), new CompletionProvider<CompletionParameters>() {

               @Override
               protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                   ProgressManager.checkCanceled();
                   String text = parameters.getPosition().getText().replaceFirst(DUMMY_IDENTIFIER, "");
                   LOG.info("asset literal prefix string " + text);
                   if (text.isEmpty()) {
                       LOG.error("dart string is empty");
                       return;
                   }
                   List<Pair<String, String>> allPaths = allAssetPaths(parameters);

                   if (allPaths == null) {
                       LOG.error("all asset path list is null");
                       return;
                   }
                   List<Pair<String, String>> filteredPaths = filterPaths(allPaths, text);
                   if (filteredPaths == null) {
                       LOG.error("filtered path list is null");
                       return;
                   }
                   if (filteredPaths.isEmpty()) {
                       LOG.error("filtered path list is empty");
                       return;
                   }

                   
                   List<Pair<String, String>> sortedPaths = sortedAssetPaths(text, filteredPaths);
                   result = result.withPrefixMatcher(new AssetPathMatcher(text)).caseInsensitive();
                   for (Pair<String, String> pathPair :
                           sortedPaths) {
                       result.addElement(LookupElementBuilder.create(pathPair.first).withCaseSensitivity(false));
                   }
                   
               }

           });
    }

    private static  List<Pair<String, String>> filterPaths(@NotNull List<Pair<String, String>> paths, @NotNull String prefix) {
        if (paths.isEmpty()) {
            return null;
        }
        AssetPathMatcher matcher = new AssetPathMatcher(prefix);
        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>(paths);
        ret.removeIf(new Predicate<Pair<String, String>>() {
            @Override
            public boolean test(Pair<String, String> s) {
                ProgressManager.checkCanceled();
                return !matcher.prefixMatches(s.first);
            }
        });
        return ret;
    }

    private static List<Pair<String, String>> allAssetPaths(@NotNull CompletionParameters parameters) {
        // 找到 pubspec 文件
        VirtualFile pubspec =  PubspecYamlUtil.findPubspecYamlFile(parameters.getPosition().getProject(), parameters.getOriginalFile().getVirtualFile());
        LOG.info("pubspec file " + pubspec);
        if (pubspec == null) {
            LOG.error("pub spec file is null");
            return null;
        }
        // 获得 pubspec 中 assets 自动对应的信息
        Map<String, Object> pubInfo = getPubspecYamlInfo(pubspec);
        if (pubInfo == null) {
            LOG.error("pub spec info is null");
            return null;
        }
        final List<String> assets = new ArrayList<String>();

        pubInfo.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                ProgressManager.checkCanceled();
                if (!s.equalsIgnoreCase("flutter")) {
                    return;
                }
                if (!(o instanceof Map)) {
                    return;
                }
                Object ats = ((Map)o).get("assets");
                LOG.info("assets class " + ats.getClass());
                LOG.info("assets in flutter " + ats + " key " + s);
                if (!(ats instanceof List)) {
                    return;
                }
                ((List) ats).removeIf(new Predicate() {
                    @Override
                    public boolean test(Object o) {
                        ProgressManager.checkCanceled();
                        return o == null;
                    }
                });
                assets.addAll((Collection<String>) ats);
            }
        });
        LOG.info("all asssets " + assets);
        // 拿到 assets 对应的所有文件
        ArrayList<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        assets.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                ProgressManager.checkCanceled();
                if (s == null) {
                    return;
                }
                VirtualFile parent = pubspec.getParent();
                VirtualFile child;
                try {
                    child = parent.findFileByRelativePath(s);
                } catch (Exception e) {
                    LOG.error(e);
                    return;
                }
                String parentPath = parent.getPath() + "/";
                if (child == null) {
                    // Flutter 支持在 pubspec 中指定 lib 文件夹下的文件作为资源文件（文件必须直接放在 lib 文件夹下，不能放在 lib 的子文件夹下）
                    child = parent.findFileByRelativePath("lib/" + s);
                    parentPath = parent.getPath() + "/lib/";
                }
                if (child == null) {
                    // Flutter 还支持以 packages/{xx_lib}/yy.png 模式引用子库的资源，如果是这种case，我们暂时没法确定文件是否真实存在，直接算作候选词条
                    String[] splited = s.split("/");
                    if (splited.length < 3) {
                        return;
                    }
                    if (!splited[0].contentEquals("packages")) {
                        return;
                    }
                    if (splited[1].isEmpty()) {
                        return;
                    }
                    ret.add(Pair.create(s, null));
                    return;
                }
                LOG.info("find assets file " + child);
                if (!child.exists()) {
                    return;
                }
                ret.addAll(flattenRelativePaths(child, parentPath));
            }
        });

        LOG.info("find all asset relative paths " + ret);
        return ret;
    }

    private static List<Pair<String, String>> sortedAssetPaths(String prefix, List<Pair<String, String>> paths) {
        paths.sort(new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                int o1d = EditDistance.levenshtein(prefix, o1.first, false);
                int o2d = EditDistance.levenshtein(prefix, o2.first, false);
                return o1d - o2d;
            }
        });
        return paths;
    }

    private static List<Pair<String, String>> flattenRelativePaths(@NotNull VirtualFile directory, @NotNull String relativeTo) {
        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        if (!directory.isDirectory()) {
            ret.add(new Pair<>(directory.getPath().replaceFirst(relativeTo, ""), directory.getPath()));
            return ret;
        }

        VirtualFileVisitor.Result result = VfsUtilCore.visitChildrenRecursively(directory, new VirtualFileVisitor<Object>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                ProgressManager.checkCanceled();
                if (file.isDirectory()) {
                    return true;
                }
                if (file.is(VFileProperty.HIDDEN)) {
                    return true;
                }
                if (file.getName().equalsIgnoreCase(".DS_Store")) {
                    return true;
                }
                ret.add(new Pair<>(file.getPath().replaceFirst(relativeTo, ""), file.getPath()));
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
