package uk.co.hermes.plugins;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import uk.co.hermes.plugins.renderer.EmbeddedHtmlReportRenderer;
import uk.co.hermes.plugins.renderer.JiraIssueLinkConverter;
import uk.co.hermes.plugins.renderer.MessageConverter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Goal which generates a changelog based on commits made to the current git repo.
 * This requires the issue management system and the system url defined the pom for jira links to work
 */
@Mojo(name = "report")
public class EmbeddedReportMojo extends AbstractMavenReport {

    /**
     * Name of the report file. Defaults to gitlog.xml
     */
    @Parameter(property = "fileName", defaultValue = "gitlog-embedded")
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

    /**
     * Report name "Release Notes"
     */
    @Parameter(property = "reportName", defaultValue = "Release Notes")
    public String reportName;
    /**
     * Report description defaults to "Generate changelog from git SCM as an internal report"
     */
    @Parameter(property = "reportDescription", defaultValue = "Generate changelog from git SCM")
    public String reportDescription;

    @Parameter(property = "project.issueManagement.system")
    private String issueManagementSystem;

    @Parameter(property = "project.issueManagement.url")
    private String issueManagementUrl;

    protected void executeReport(Locale locale) throws MavenReportException {

        try {
            EmbeddedHtmlReportRenderer renderer = new EmbeddedHtmlReportRenderer(getSink(), getCommitMessageConverter());

            EmbeddedReportGenerator embeddedReportGenerator = new EmbeddedReportGenerator(renderer, null, getLog());
            embeddedReportGenerator.openRepository();
            if (daysToGoBack != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -daysToGoBack);
                embeddedReportGenerator.generate(reportTitle, calendar.getTime());
            } else if (commitsAfterDate != null) {
                embeddedReportGenerator.generate(reportTitle, commitsAfterDate);
            } else {
                embeddedReportGenerator.generate(reportTitle);
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
        return this.reportName;
    }

    public String getDescription(Locale locale) {
        return reportDescription;
    }

    private MessageConverter getCommitMessageConverter() {
        getLog().debug("Trying to load issue tracking info: " + issueManagementSystem + " / " + issueManagementUrl);
        MessageConverter converter = null;
        try {
            if (issueManagementUrl != null && issueManagementUrl.contains("://")) {
                converter = new JiraIssueLinkConverter(getLog(), issueManagementUrl);
                getLog().debug("Using tracker " + converter.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            getLog().warn("Could not load issue management system information; no HTML links will be generated.", ex);
        }
        return converter;
    }
}