package com.ixigua.completion.contributor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PatternConditionPlus;
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

import static com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER;

public class AssetLiteralCompletionContributor extends CompletionContributor {

    private static final Logger LOG = Logger.getInstance(AssetLiteralCompletionContributor.class);

    public AssetLiteralCompletionContributor() {
        SVGActivator svgActivator = new SVGActivator();
        svgActivator.activate();
        //We will not process strings in import statements
        extend(CompletionType.BASIC, PlatformPatterns.and(PlatformPatterns.psiElement(DartTokenTypes.REGULAR_STRING_PART),PlatformPatterns.not(PlatformPatterns.psiElement(DartTokenTypes.REGULAR_STRING_PART).inside(PlatformPatterns.psiElement(DartTokenTypes.IMPORT_STATEMENT)))) , new CompletionProvider<CompletionParameters>() {

           @Override
           protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
               ProgressManager.checkCanceled();
               int caretPosition =  parameters.getPosition().getText().indexOf(DUMMY_IDENTIFIER);
               String prefix = parameters.getPosition().getText().substring(0, caretPosition);
               LOG.info("asset literal prefix string " + prefix);
               // Find the pubspec file
               VirtualFile pubspec =  PubspecUtil.findPubspecYamlFile(parameters.getPosition().getProject(), parameters.getOriginalFile().getVirtualFile());
               CompletionContext completionContext = new CompletionContext(pubspec, prefix);
               String packageName = completionContext.getPackageName();
//               Find all assets that match this prefix
               List<Asset> assets = assetsForPrefix(completionContext);
//               We need to create a CompletionResultSet with the new PrefixMatcher, because the default PrefixMatcher
//               may be different from what we want to handle.
               result = result.withPrefixMatcher(createPrefixMatcher(prefix)).caseInsensitive();
               for (Asset asset : assets) {
                   ProgressManager.checkCanceled();
                   LookupElementBuilder elementBuilder = LookupElementBuilder.create(asset.lookupStringForPackage(packageName));
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
    private static List<Asset> assetsForPrefix(@NotNull CompletionContext context) {
        String prefix = context.getPrefix();
        if (prefix.isEmpty()) {
            LOG.error("dart string is empty");
            return Collections.emptyList();
        }
        List<Asset> assets = AssetFinder.findAllAsset(context);
        List<Asset> filteredAssets = filterAssets(assets, context);
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

    private static  List<Asset> filterAssets(@NotNull List<Asset> paths, @NotNull CompletionContext context) {
        if (paths.isEmpty()) {
            return null;
        }
        PrefixMatcher matcher = createPrefixMatcher(context.getPrefix());
        List<Asset> ret = new ArrayList<>(paths);
        ret.removeIf(s -> {
            ProgressManager.checkCanceled();
            return !matcher.prefixMatches(s.lookupStringForPackage(context.getPackageName()));
        });
        return ret;
    }
}
