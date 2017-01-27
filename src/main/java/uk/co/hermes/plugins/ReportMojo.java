package uk.co.hermes.plugins;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import uk.co.hermes.plugins.renderer.GitlogRenderer;

import java.io.IOException;
import java.util.Locale;

@Mojo( name = "report")
public class ReportMojo extends AbstractMavenReport
{
    @Parameter( property = "fileName", defaultValue = "gitlog" )
    private String fileName;

    protected void executeReport(Locale locale) throws MavenReportException {

        GitlogRenderer renderer = new GitlogRenderer(getSink());

        Generator generator = new Generator(renderer, null, null);
        try {
            generator.openRepository();
            generator.generate("GIT RELEASE NOTES");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoGitRepositoryException e) {
            e.printStackTrace();
        }

    }

    public String getOutputName() {
        return fileName;
    }

    public String getName(Locale locale) {
        return "en";
    }

    public String getDescription(Locale locale) {
        return "git logs";
    }
}