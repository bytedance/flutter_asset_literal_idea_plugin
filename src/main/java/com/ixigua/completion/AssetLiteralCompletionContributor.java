package com.ixigua.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.ixigua.completion.assets.Asset;
import com.ixigua.completion.assets.AssetFinder;
import com.ixigua.completion.pubspec.PubspecUtil;
import com.ixigua.completion.svg.SVGActivator;
import com.jetbrains.lang.dart.DartTokenTypes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER;

public class AssetLiteralCompletionContributor extends CompletionContributor {

    private static final Logger LOG = Logger.getInstance(AssetLiteralCompletionContributor.class);

    public AssetLiteralCompletionContributor() {
        SVGActivator svgActivator = new SVGActivator();
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
               int caretPosition =  parameters.getPosition().getText().indexOf(DUMMY_IDENTIFIER);
               String prefix = parameters.getPosition().getText().substring(0, caretPosition);
               LOG.info("asset literal prefix string " + prefix);
               // Find the pubspec file
               VirtualFile pubspec =  PubspecUtil.findPubspecYamlFile(parameters.getPosition().getProject(), parameters.getOriginalFile().getVirtualFile());
//               Find all assets that match this prefix
               List<Asset> assets = assetsForPrefix(prefix, pubspec);
//               We need to create a CompletionResultSet with the new PrefixMatcher, because the default PrefixMatcher
//               may be different from what we want to handle.
               result = result.withPrefixMatcher(createPrefixMatcher(prefix)).caseInsensitive();
               for (Asset asset : assets) {
                   ProgressManager.checkCanceled();
                   LookupElementBuilder elementBuilder = LookupElementBuilder.create(asset.lookupString());
                   Icon icon = asset.icon();
                   if (icon != null) {
                       elementBuilder = elementBuilder.withIcon(icon);
                   }
                   String typeStr = asset.typeText();
                   if (typeStr != null) {
                       elementBuilder = elementBuilder.withTypeText(typeStr);
                   }
                   result.addElement(elementBuilder);
               }

           }
       });
    }

    @NotNull
    private static List<Asset> assetsForPrefix(@NotNull String prefix, VirtualFile pubspec) {
        if (prefix.isEmpty()) {
            LOG.error("dart string is empty");
            return Collections.emptyList();
        }
        List<Asset> assets = AssetFinder.findAllAsset(pubspec);

        if (assets == null) {
            LOG.error("all asset list is null");
            return Collections.emptyList();
        }
        List<Asset> filteredAssets = filterAssets(assets, prefix);
        if (filteredAssets == null) {
            LOG.error("filtered path list is null");
            return Collections.emptyList();
        }
        if (filteredAssets.isEmpty()) {
            LOG.error("filtered path list is empty");
            return Collections.emptyList();
        }

        return filteredAssets;
    }

    private static PrefixMatcher createPrefixMatcher(@NotNull String prefix) {
        return new PlainPrefixMatcher(prefix, false);
    }

    private static  List<Asset> filterAssets(@NotNull List<Asset> paths, @NotNull String prefix) {
        if (paths.isEmpty()) {
            return null;
        }
        PrefixMatcher matcher = createPrefixMatcher(prefix);
        List<Asset> ret = new ArrayList<Asset>(paths);
        ret.removeIf(new Predicate<Asset>() {
            @Override
            public boolean test(Asset s) {
                ProgressManager.checkCanceled();
                return !matcher.prefixMatches(s.lookupString());
            }
        });
        return ret;
    }
}
