package com.ixigua.completion.assets;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AssetFinder {

    private static final Logger LOG = Logger.getInstance(AssetFinder.class);

    // 解析到所有的 asset 声明，包括 assets 和 fonts
    public static List<Asset> findAllAsset(@NotNull VirtualFile pubspec, @NotNull Map<String, Object> pubspecInfo) {
        final List<Asset> assets = new ArrayList<>();

        // 首选找到 flutter 声明，如果没有，那么就认为没有 assets
        Object flutterDeclaration = pubspecInfo.get("flutter");
        if (!(flutterDeclaration instanceof Map)) {
            return Collections.emptyList();
        }
        Object packageNameDeclaration = pubspecInfo.get("name");
        String packageName = packageNameDeclaration instanceof String ? (String) packageNameDeclaration : null;
//        找到所有的 assets 声明
        List<String> assetsDeclarations = findAssetsDeclarations((Map<String, Object>) flutterDeclaration);
//        展开所有 assets
        assets.addAll(expandAssetsDeclarations(pubspec, assetsDeclarations, packageName));
        // 再找到所有的 fonts 声明
        List<String> fontsDeclarations = findFontsDeclarations((Map<String, Object>) flutterDeclaration);
//        展开所有 fonts
        assets.addAll(expandFontsDeclarations(fontsDeclarations, packageName));
        return assets;
    }

    @NotNull
    private static List<String> findAssetsDeclarations(@NotNull Map<String, Object> flutterDeclaration) {
        Object ats = flutterDeclaration.get("assets");
        LOG.info("assets class " + ats.getClass());
        LOG.info("assets in flutter " + ats);
        if (!(ats instanceof List)) {
            return Collections.emptyList();
        }
        ((List) ats).removeIf(new Predicate() {
            @Override
            public boolean test(Object o) {
                ProgressManager.checkCanceled();
                // assets 中有可能有空元素，
                // 比如 assets:
                //          - images
                //          -
                // 第二行就会被解析成空
                return o == null;
            }
        });
        return (List<String>) ats;
    }

    @NotNull
    private static List<Asset> expandAssetsDeclarations(@NotNull VirtualFile pubspec, @NotNull List<String> declarations, String currentPackage) {
        List<Asset> ret = new ArrayList<>();
        declarations.forEach(new Consumer<String>() {
            @Override
            public void accept(String declaration) {
                ProgressManager.checkCanceled();
                if (declaration == null) {
                    return;
                }
                VirtualFile parent = pubspec.getParent();
                VirtualFile child = null;
                try {
                    child = parent.findFileByRelativePath(declaration);
                } catch (Exception e) {
                    LOG.error("find normal asset failed " + e);
                }

                if (child == null) {
                    VirtualFile libDirectory = parent.findFileByRelativePath("lib/");
                    if (libDirectory != null) {
                        // Flutter 支持在 pubspec 中指定 lib 文件夹下的文件作为资源文件（文件必须直接放在 lib 文件夹下，不能放在 lib 的子文件夹下）
                        try {
                            child = libDirectory.findFileByRelativePath(declaration);
                            parent = libDirectory;
                        } catch (Exception e) {
                            LOG.error("find lib child asset failed " + e);
                        }
                    }
                }
                if (child == null) {
                    // Flutter 还支持以 packages/{xx_lib}/yy.png 模式引用子库的资源，如果是这种case，我们暂时没法确定文件是否真实存在，直接算作候选词条
                    String[] splited = declaration.split("/");
                    if (splited.length < 3) {
                        return;
                    }
                    if (!splited[0].contentEquals("packages")) {
                        return;
                    }
                    if (splited[1].isEmpty()) {
                        return;
                    }
                    ret.add(new Asset(declaration, AssetType.asset,null,splited[1]));
                    return;
                }
                LOG.info("find assets file " + child);
                if (!child.exists()) {
                    return;
                }
                List<Pair<String, VirtualFile>> filePairs = flattenRelativePaths(child, parent);
                List<Asset> assets = filePairs.stream().map(new Function<Pair<String, VirtualFile>, Asset>() {
                    @Override
                    public Asset apply(Pair<String, VirtualFile> stringVirtualFilePair) {
                        return new Asset(stringVirtualFilePair.first, AssetType.asset, stringVirtualFilePair.second, currentPackage);
                    }
                }).collect(Collectors.toList());
                ret.addAll(assets);
            }
        });
        return ret;
    }

    @NotNull
    private static List<String> findFontsDeclarations(@NotNull Map<String, Object> flutterDeclaration) {
        Object fts = flutterDeclaration.get("fonts");
        if (fts == null) {
            return Collections.emptyList();
        }
        LOG.info("fonts class " + fts.getClass());
        LOG.info("fonts in flutter " + fts);
        if (!(fts instanceof List)) {
            return Collections.emptyList();
        }
        List<String> ret = new ArrayList<String>();
        ((List<Map<String, Object>>) fts).forEach(new Consumer<Map<String, Object>>() {
            @Override
            public void accept(Map<String, Object> stringObjectMap) {
                ProgressManager.checkCanceled();
                if (!(stringObjectMap instanceof Map)) {
                    return;
                }
                Object familyDecl = ((Map) stringObjectMap).get("family");
                if (familyDecl == null) {
                    return;
                }
                if (!(familyDecl instanceof String)) {
                    return;
                }
                ret.add((String) familyDecl);
            }
        });
        return ret;
    }

    @NotNull
    private static List<Asset> expandFontsDeclarations(@NotNull List<String> declarations, String currentPackage) {
        List<Asset> ret = new ArrayList<>();
        return declarations.stream().map(new Function<String, Asset>() {
            @Override
            public Asset apply(String s) {
                return new Asset(s, AssetType.font,null, currentPackage);
            }
        }).collect(Collectors.toList());
    }

    private static List<Pair<String, VirtualFile>> flattenRelativePaths(@NotNull VirtualFile directory, @NotNull VirtualFile relativeTo) {
        List<Pair<String, VirtualFile>> ret = new ArrayList<Pair<String, VirtualFile>>();
        if (!directory.isDirectory()) {
            ret.add(Pair.create(directory.getPath().replaceFirst(relativeTo.getPath() + "/", ""), directory));
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
                ret.add(Pair.create(file.getPath().replaceFirst(relativeTo.getPath() + "/", ""), file));
                return true;
            }
        });

        return ret;
    }
}
