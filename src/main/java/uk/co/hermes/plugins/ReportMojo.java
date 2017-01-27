package uk.co.hermes.plugins;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import uk.co.hermes.plugins.renderer.GitlogRenderer;
import uk.co.hermes.plugins.renderer.JiraIssueLinkConverter;
import uk.co.hermes.plugins.renderer.MessageConverter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Goal which generates a changelog based on commits made to the current git repo.
 */
@Mojo(name = "report")
public class ReportMojo extends AbstractMavenReport {

    /**
     * Name of the report file. Defaults to gitlog.xml
     */
    @Parameter(property = "fileName", defaultValue = "gitlog")
    private String fileName;

    /**
     * Optional parameter. Will display logs since the provided date.
     */
    @Parameter(property = "commitsAfterDate")
    private Date commitsAfterDate;

    /**
     * Optional parameter. Only logs from the last "X" number of days will be reported
     * If both "commitsAfterDate" and "daysToGoBack" parameter are provided then "commitsAfterDate" will be ignored
     */
    @Parameter(property = "daysToGoBack")
    private Integer daysToGoBack;

    /**
     * Title of the report defaults to "GIT RELEASE NOTES"
     */
    @Parameter(property = "reportTitle", defaultValue = "GIT RELEASE NOTES")
    private String reportTitle;

    @Parameter(property = "project.issueManagement.system")
    private String issueManagementSystem;
    @Parameter(property = "project.issueManagement.url")
    private String issueManagementUrl;

    protected void executeReport(Locale locale) throws MavenReportException {

        GitlogRenderer renderer = new GitlogRenderer(getSink(), getCommitMessageConverter());

        Generator generator = new Generator(renderer, null, getLog());
        try {
            generator.openRepository();
            if (daysToGoBack != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -daysToGoBack);
                generator.generate(reportTitle, calendar.getTime());
            } else if (commitsAfterDate != null) {
                generator.generate(reportTitle, commitsAfterDate);
            } else {
                generator.generate(reportTitle);
            }
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