package mb.nabl2.benchmark;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.constraint.SingleFileConstraintAnalyzer;
import org.metaborg.spoofax.core.shell.CLIUtils;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.concurrent.IClosableLock;

public class AnalysisRun {
    public static void main(String[] args) throws Throwable {
        try(Spoofax spoofax = new Spoofax()) {
            CLIUtils cli = new CLIUtils(spoofax);
            FileObject languageDir = spoofax.resourceService.resolve(args[0]);
            IProject project = cli.getOrCreateProject(languageDir);
            ILanguageImpl language = spoofax.languageDiscoveryService.languageFromDirectory(languageDir);
            FileObject fileToParse = spoofax.resourceService.resolve(args[1]);
            String text = spoofax.sourceTextService.text(fileToParse);
            ISpoofaxInputUnit input = spoofax.unitService.inputUnit(fileToParse, text, language, language);
            ISpoofaxParseUnit parseUnit = spoofax.syntaxService.parse(input);
            IContext context = spoofax.contextService.get(fileToParse, project, language);

            AnalyzerFacet<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> facet = language.facet(AnalyzerFacet.class);
            SingleFileConstraintAnalyzer analyzer = (SingleFileConstraintAnalyzer) facet.analyzer;
            FacetContribution<AnalysisFacet> facetContribution = language.facetContribution(AnalysisFacet.class);

            System.out.println(facetContribution.facet.strategyName);

            ISpoofaxAnalyzeResult result;
            try(IClosableLock lock = context.write()) {
                result = spoofax.analysisService.analyze(parseUnit, context);
            }
            ISpoofaxAnalyzeUnit analyzeUnit = result.result();

            System.out.println(analyzeUnit.ast().toString());
        }
    }
}
