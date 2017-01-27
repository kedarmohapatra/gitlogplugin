package uk.co.hermes.plugins;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import uk.co.hermes.plugins.renderer.GitlogRenderer;
import uk.co.hermes.plugins.renderer.JiraIssueLinkConverter;
import uk.co.hermes.plugins.renderer.MessageConverter;

import java.io.IOException;
import java.util.Locale;

@Mojo( name = "report")
public class ReportMojo extends AbstractMavenReport
{
    @Parameter( property = "fileName", defaultValue = "gitlog" )
    private String fileName;

    @Parameter(property = "project.issueManagement.system")
    private String issueManagementSystem;

    /**
     * Used to create links to your issue tracking system for HTML reports. If unspecified, it will try to use the value
     * specified in the issueManagement section of your project's POM.
     */
    @Parameter(property = "project.issueManagement.url")
    private String issueManagementUrl;

    protected void executeReport(Locale locale) throws MavenReportException {

        GitlogRenderer renderer = new GitlogRenderer(getSink(), getCommitMessageConverter());

        Generator generator = new Generator(renderer, null, getLog());
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
        return "GitLog";
    }

    public String getDescription(Locale locale) {
        return "Generate changelog from git SCM.";
    }

    private MessageConverter getCommitMessageConverter() {
        getLog().debug("Trying to load issue tracking info: " + issueManagementSystem + " / " + issueManagementUrl);
        MessageConverter converter = null;
        try {
            if (issueManagementUrl != null && issueManagementUrl.contains("://")) {
                String system = ("" + issueManagementSystem).toLowerCase();
                converter = new JiraIssueLinkConverter(getLog(), issueManagementUrl);
                getLog().debug("Using tracker " + converter.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            getLog().warn("Could not load issue management system information; no HTML links will be generated.", ex);
        }
        return converter;
    }
}