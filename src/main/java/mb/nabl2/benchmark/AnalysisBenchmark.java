package mb.nabl2.benchmark;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.shell.CLIUtils;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 1)
@State(Scope.Thread)
public class AnalysisBenchmark {
    @Param({}) public String languagePath;
    @Param({}) public String programPath;

    @Setup(Level.Trial)
    public void prepareAnalysis() throws Throwable {
        // TODO: perform steps preceding the analysis such as parsing here.
    }

    @Benchmark
    public void runBenchmark(Blackhole blackhole) throws Exception {
        try(Spoofax spoofax = new Spoofax()) {
            CLIUtils cli = new CLIUtils(spoofax);
            FileObject languageDir = spoofax.resourceService.resolve(this.languagePath);
            IProject project = cli.getOrCreateProject(languageDir);
            ILanguageImpl language = spoofax.languageDiscoveryService.languageFromDirectory(languageDir);
            FileObject fileToParse = spoofax.resourceService.resolve(this.programPath);
            String text = spoofax.sourceTextService.text(fileToParse);

            ISpoofaxInputUnit input = spoofax.unitService.inputUnit(fileToParse, text, language, language);
            ISpoofaxParseUnit parseUnit = spoofax.syntaxService.parse(input);

            IContext context = spoofax.contextService.get(fileToParse, project, language);

            ISpoofaxAnalyzeResult result;
            try(IClosableLock lock = context.write()) {
                result = spoofax.analysisService.analyze(parseUnit, context);
            }
            blackhole.consume(result);
        }
    }
}
