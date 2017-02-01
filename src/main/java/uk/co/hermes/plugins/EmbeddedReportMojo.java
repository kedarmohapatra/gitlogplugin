package uk.co.hermes.plugins;

import org.apache.maven.doxia.module.confluence.ConfluenceSinkFactory;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReportException;
import org.bsc.confluence.ConfluenceUtils;
import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.Page;
import uk.co.hermes.plugins.model.Site;
import uk.co.hermes.plugins.renderer.EmbeddedHtmlReportRenderer;
import uk.co.hermes.plugins.renderer.JiraIssueLinkConverter;
import uk.co.hermes.plugins.renderer.MessageConverter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.lang.String.format;

/**
 * Goal which generates a changelog based on commits made to the current git repo.
 * This requires the issue management system and the system url defined the pom for jira links to work
 */
@Mojo(name = "report")
public class EmbeddedReportMojo extends AbstractConfluenceMojo {

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
    private String reportName;
    /**
     * Report description defaults to "Generate changelog from git SCM as an internal report"
     */
    @Parameter(property = "reportDescription", defaultValue = "Generate changelog from git SCM")
    private String reportDescription;

    /**
     * Publish to confluence
     */
    @Parameter(property = "publishToConfluence", defaultValue = "true")
    private boolean publishToConfluence;

    @Parameter(property = "project.issueManagement.system")
    private String issueManagementSystem;

    @Parameter(property = "project.issueManagement.url")
    private String issueManagementUrl;

    protected void executeReport(Locale locale) throws MavenReportException {
        try {
            Sink sink = null;
            if(publishToConfluence){
                sink = getConfluenceSink();
            }else{
                getSink();
            }

            EmbeddedHtmlReportRenderer renderer = new EmbeddedHtmlReportRenderer(sink, getCommitMessageConverter());
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

            generateProjectReport();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoGitRepositoryException e) {
            e.printStackTrace();
        } catch (MojoExecutionException e) {
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

    private Sink getConfluenceSink() throws IOException {
        File outputDirectory = new File(getOutputDirectory());
        String filename = getOutputName() + ".confluence";
        ConfluenceSinkFactory factory = new ConfluenceSinkFactory();
        return factory.createSink(outputDirectory, filename);
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

    private void generateProjectReport() throws MojoExecutionException
    {

        /*
        just testing...
         */
        final Site site = createFromFolder();
        final Locale locale = Locale.getDefault();



        super.confluenceExecute(new ConfluenceTask() {

            public void execute(Confluence confluence) throws Exception {

                final Page parentPage = loadParentPage(confluence);

                //
                // Issue 32
                //
                final String title = getTitle();

                if (!isSnapshot() && isRemoveSnapshots()) {
                    final String snapshot = title.concat("-SNAPSHOT");
                    getLog().info(format("removing page [%s]!", snapshot));
                    boolean deleted = ConfluenceUtils.removePage(confluence, parentPage, snapshot);

                    if (deleted) {
                        getLog().info(format("Page [%s] has been removed!", snapshot));
                    }
                }

                final String titlePrefix = title;

                final String wiki = createProjectHome(site, locale);

                Page confluenceHomePage = ConfluenceUtils.getOrCreatePage(confluence, parentPage, title);

                confluenceHomePage.setContent(wiki);

                confluenceHomePage = confluence.storePage(confluenceHomePage);

                for( String label : site.getHome().getComputedLabels() ) {

                    confluence.addLabelByName(label, Long.parseLong(confluenceHomePage.getId()) );
                }

                generateChildren( confluence, site.getHome(), confluenceHomePage, title, titlePrefix);
            }

        });


    }

    private Site createFromFolder() {
        final Site result = new Site();
        //todo add labels
        final Site.Page home = new Site.Page();
        home.setName(getTitle());
        super.setPageUriFormFile(home, templateWiki);
        result.setHome( home );

        return result;
    }
}