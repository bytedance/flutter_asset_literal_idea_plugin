package com.ixigua.completion.assets;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.ixigua.completion.contributor.CompletionContext;
import com.ixigua.completion.fonts.AndroidFonts;
import com.ixigua.completion.fonts.IOSFonts;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssetFinder {

    private static final Logger LOG = Logger.getInstance(AssetFinder.class);

    // Parse all assets including:
    // 1. "assets" and "fonts" declarations in pubspec
    // 2. "assets" and "fonts" declarations in dependent packages
    // 3. pre-installed fonts on iOS and Android
    @NotNull
    public static List<Asset> findAllAsset(@NotNull CompletionContext context) {
        List<Asset> ret = new ArrayList<>();
        ret.addAll(findAllAssetInPubspec(context));
        ret.addAll(allPreInstalledFonts());
        return ret;
    }

    // Parse all assets directly defined in this pubspec including:
    // 1. "assets" declarations
    // 2. "fonts" declarations
    @NotNull
    private static List<Asset> findAllMyAssets(@NotNull CompletionContext context) {
        VirtualFile pubspec = context.getPubspec();
        LOG.info("pubspec file " + pubspec);
        // parse the pubspec file
        Map<String, Object> pubInfo = context.getPubspecInfoOfCurrentProject();

        // It is preferred to find the flutter statement, if there is no, then it is assumed that there are no assets
        Object flutterDeclaration = pubInfo.get("flutter");
        if (!(flutterDeclaration instanceof Map)) {
            return Collections.emptyList();
        }
        String packageName = context.getPackageName();
//        Find all declarations under the "assets:" statement
        List<String> assetsDeclarations = findAssetsDeclarations((Map<String, Object>) flutterDeclaration);
//        Expand all asset declarations，we will recursively traverse each declared
//        folder, all sub-files of these folders will be included, which is different from the behavior of Flutter:
//        Flutter will only include the direct children of each declared folder.
        final List<Asset> assets = new ArrayList<>(expandAssetsDeclarations(context, assetsDeclarations));
        // Find all declarations under the "fonts:" statement
        List<String> fontsDeclarations = findFontsDeclarations((Map<String, Object>) flutterDeclaration);
//        Expand all font declarations, we only care about the font family and will not verify the existence of the font file
        assets.addAll(expandFontsDeclarations(fontsDeclarations, packageName));


        LOG.info("find all assets" + assets);
        return assets;
    }

    private static List<Asset> allPreInstalledFonts() {
        // Add all pre-installed fonts on iOS
        List<String> iOS9Fonts = Arrays.asList(IOSFonts.IOS_9_FONT_LIST);
        final List<Asset> assets = new ArrayList<>(expandFontsDeclarations(iOS9Fonts, "iOS 9 Font"));
        List<String> iOS8Fonts = Arrays.asList(IOSFonts.IOS_8_FONT_LIST);
        assets.addAll(expandFontsDeclarations(iOS8Fonts, "iOS 8 Font"));
        //Add all pre-installed fonts on Android
        List<String> androidFonts = Arrays.asList(AndroidFonts.FONT_LIST);
        assets.addAll(expandFontsDeclarations(androidFonts, "Android Font"));
        return assets;
    }

    // A package may depend on packages listed in '.packages', parse all assets defined in those pubspec files including:
    // 1. "assets" declarations
    // 2. "fonts" declarations
    @NotNull
    private static List<Asset> findAllAssetInPubspec(@NotNull CompletionContext context) {
        List<Asset> assets = new ArrayList<>();
        context.getChildren().forEach(new BiConsumer<String, CompletionContext>() {
            @Override
            public void accept(String s, CompletionContext context) {
                List<Asset> assetsInThisPackage = findAllMyAssets(context);
                assets.addAll(assetsInThisPackage);
            }
        });
        return assets;
    }

    @NotNull
    private static List<String> findAssetsDeclarations(@NotNull Map<String, Object> flutterDeclaration) {
        Object ats = flutterDeclaration.get("assets");
        LOG.info("assets in flutter " + ats);
        if (!(ats instanceof List)) {
            return Collections.emptyList();
        }
        ((List) ats).removeIf(o -> {
            ProgressManager.checkCanceled();
            // There may be empty elements under "assets" state，
            // like:
            // 1. assets:
            // 2.  - images
            // 3.  -
            // The third line will be parsed as null
            return o == null;
        });
        return (List<String>) ats;
    }

    @NotNull
    private static List<Asset> expandAssetsDeclarations(@NotNull CompletionContext context, @NotNull List<String> declarations) {
        List<Asset> ret = new ArrayList<>();
        declarations.forEach(new Consumer<String>() {
            @Override
            public void accept(String declaration) {
//                Check frequently if the user cancels the current operation
                ProgressManager.checkCanceled();
                if (declaration == null) {
                    return;
                }
                VirtualFile pubspec = context.getPubspec();
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
                        // Flutter supports specifying files in the "lib" folder as assets in the pubspec (files
                        // must be placed directly in the "lib" folder, not in sub-folders of "lib")
                        try {
                            child = libDirectory.findFileByRelativePath(declaration);
                            parent = libDirectory;
                        } catch (Exception e) {
                            LOG.error("find lib child asset failed " + e);
                        }
                    }
                }
                if (child == null) {
                    // Flutter also supports referencing dependent package assets in "packages/xx_lib/yy" mode.
                    String[] segmented = declaration.split("/");
                    if (segmented.length < 3) {
                        return;
                    }
                    if (!segmented[0].contentEquals("packages")) {
                        return;
                    }
                    String packageName = segmented[1];
                    if (StringUtil.isEmpty(packageName)) {
                        return;
                    }
                    String assetName = segmented[2];
                    if (StringUtil.isEmpty(assetName)) {
                        return;
                    }
                    CompletionContext childContext = context.getChild(packageName);
                    VirtualFile assetFile = null;
                    if (childContext != null) {
                        VirtualFile childPackageRoot = childContext.getPubspec().getParent();
                        assetFile = childPackageRoot.findFileByRelativePath("lib/" + assetName);
                    }
                    ret.add(new ImageAsset(assetName, assetFile, packageName));
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
                        return new ImageAsset(stringVirtualFilePair.first, stringVirtualFilePair.second, context.getPackageName());
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
        return declarations.stream().map(new Function<String, Asset>() {
            @Override
            public Asset apply(String s) {
                return new FontAsset(s,null, currentPackage);
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
