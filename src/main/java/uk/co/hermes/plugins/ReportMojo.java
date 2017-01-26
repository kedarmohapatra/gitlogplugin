package uk.co.hermes.plugins;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import uk.co.hermes.plugins.renderer.GitlogRenderer;

import java.io.File;
import java.util.Locale;

@Mojo( name = "report")
public class ReportMojo extends AbstractMavenReport
{
    @Parameter( property = "fileName", defaultValue = "gitlog.html" )
    private String fileName;

    private Sink sink;

    protected void executeReport(Locale locale) throws MavenReportException {

        RenderingContext context = new RenderingContext( outputDirectory, fileName );
        SiteRendererSink sink = new SiteRendererSink( context );
        GitlogRenderer renderer = new GitlogRenderer(sink);
        renderer.render();
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