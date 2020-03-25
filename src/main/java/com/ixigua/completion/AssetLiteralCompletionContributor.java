package com.ixigua.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.IconUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.text.EditDistance;
import com.ixigua.completion.assets.Asset;
import com.ixigua.completion.assets.AssetFinder;
import com.ixigua.completion.assets.AssetType;
import com.ixigua.completion.pubspec.PubspecUtil;
import com.ixigua.completion.svg.SVGActivator;
import com.ixigua.completion.transform.TransformImage;
import com.jetbrains.lang.dart.DartTokenTypes;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER;

public class AssetLiteralCompletionContributor extends CompletionContributor {

    private static final Logger LOG = Logger.getInstance(AssetLiteralCompletionContributor.class);
    private final SVGActivator svgActivator = new SVGActivator();
    private static final int ICON_WIDTH = 32;
    private static final int ICON_HEIGHT = 32;

    public AssetLiteralCompletionContributor() {
        svgActivator.activate();
//        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
//        iioRegistry.getServiceProviders(ImageReaderSpi.class, true).forEachRemaining(new Consumer<ImageReaderSpi>() {
//            @Override
//            public void accept(ImageReaderSpi imageReaderSpi) {
//                System.out.println("image reader vendor: " + imageReaderSpi.getVendorName() + " desc: " + imageReaderSpi.getDescription(Locale.getDefault()));
//            }
//        });
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
               List<Asset> assets = allAssets(parameters);

               if (assets == null) {
                   LOG.error("all asset list is null");
                   return;
               }
               List<Asset> filteredPaths = filterAssets(assets, text);
               if (filteredPaths == null) {
                   LOG.error("filtered path list is null");
                   return;
               }
               if (filteredPaths.isEmpty()) {
                   LOG.error("filtered path list is empty");
                   return;
               }


               List<Asset> sortedPaths = sortedAssets(text, filteredPaths);
               LOG.info("all sorted asset paths " + sortedPaths);
               result = result.withPrefixMatcher(new PlainPrefixMatcher(text, false)).caseInsensitive();
               for (Asset asset :
                       sortedPaths) {
                   ProgressManager.checkCanceled();
                   VirtualFile file = asset.getFile();
                   LookupElementBuilder elementBuilder = LookupElementBuilder.create(asset.getName());
                   BufferedImage iconImage = null;
                   try {
                       if (file != null && asset.getType() == AssetType.asset) {
                           iconImage = ImageIO.read(new File(file.getPath()));
                       } else if (asset.getType() == AssetType.font) {
                           iconImage = ImageIO.read(this.getClass().getResourceAsStream("font_icon.png"));
                       }

                   } catch (Exception e) {
                       LOG.error("read icon failed " + e);
                   }
                   if (iconImage == null) {
                       try {
                           iconImage = ImageIO.read(this.getClass().getResourceAsStream("blank_icon.png"));
                       } catch (IOException e) {
                           LOG.error("read error icon failed " + e);
                       }
                   }
                   if (iconImage != null) {
                       Image outputImage = TransformImage.resizeAspectFitCenter(iconImage, ICON_WIDTH, ICON_HEIGHT);
                       Icon icon = IconUtil.createImageIcon(outputImage);
                       elementBuilder = elementBuilder.withIcon(icon);
                   }
                   if (asset.getSourceDescription() != null) {
                       elementBuilder = elementBuilder.withTypeText(asset.getSourceDescription());
                   }
                   result.addElement(elementBuilder);
               }

           }
       });
    }

    private static  List<Asset> filterAssets(@NotNull List<Asset> paths, @NotNull String prefix) {
        if (paths.isEmpty()) {
            return null;
        }
        PlainPrefixMatcher matcher = new PlainPrefixMatcher(prefix, false);
        List<Asset> ret = new ArrayList<Asset>(paths);
        ret.removeIf(new Predicate<Asset>() {
            @Override
            public boolean test(Asset s) {
                ProgressManager.checkCanceled();
                return !matcher.prefixMatches(s.getName());
            }
        });
        return ret;
    }

    private static List<Asset> allAssets(@NotNull CompletionParameters parameters) {
        // 找到 pubspec 文件
        VirtualFile pubspec =  PubspecUtil.findPubspecYamlFile(parameters.getPosition().getProject(), parameters.getOriginalFile().getVirtualFile());
        LOG.info("pubspec file " + pubspec);
        if (pubspec == null) {
            LOG.error("pub spec file is null");
            return null;
        }
        // 获得 pubspec 中 assets 自动对应的信息
        Map<String, Object> pubInfo = PubspecUtil.getPubspecYamlInfo(pubspec);
        if (pubInfo == null) {
            LOG.error("pub spec info is null");
            return null;
        }

        List<Asset> assets = AssetFinder.findAllAsset(pubspec, pubInfo);

        LOG.info("find all assets" + assets);
        return assets;
    }

    private static List<Asset> sortedAssets(String prefix, List<Asset> paths) {
        paths.sort(new Comparator<Asset>() {
            @Override
            public int compare(Asset o1, Asset o2) {
                int o1d = EditDistance.levenshtein(prefix, o1.getName(), false);
                int o2d = EditDistance.levenshtein(prefix, o2.getName(), false);
                return o1d - o2d;
            }
        });
        return paths;
    }

}
