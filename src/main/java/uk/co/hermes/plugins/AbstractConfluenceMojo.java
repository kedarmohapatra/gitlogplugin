package uk.co.hermes.plugins;

import biz.source_code.miniTemplator.MiniTemplator;
import org.apache.maven.doxia.module.confluence.ConfluenceSink;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.settings.Proxy;
import org.bsc.confluence.ConfluenceUtils;
import org.codehaus.swizzle.confluence.*;
import uk.co.hermes.plugins.model.Site;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

public abstract class AbstractConfluenceMojo extends AbstractMavenReport {

    protected interface ConfluenceTask {

        void execute(Confluence confluence) throws Exception;
    }

    /**
     * additional properties pass to template processor
     */
    @Parameter()
    private java.util.Map properties;
    /**
     * Confluence end point url
     */
    @Parameter(property = "confluence.endPoint", defaultValue = "http://localhost:10090/rpc/xmlrpc")
    private String endPoint;

    /**
     * Home page template source. Template name will be used also as template source for children
     */
    @Parameter(defaultValue = "${basedir}/src/site/confluence/template.wiki")
    protected java.io.File templateWiki;

    /**
     * Confluence target confluence spaceKey
     */
    @Parameter(property = "confluence.spaceKey", defaultValue = "MD")
    private String spaceKey;

    /**
     * Confluence parent page title
     */
    @Parameter(property = "confluence.parentPage", defaultValue = "Momentum")
    private String parentPageTitle;
    /**
     * Confluence parent page id.
     * If set it is possible to avoid specifying parameters spaceKey and parentPageTitle
     *
     * @since 4.10
     */
    @Parameter(property = "confluence.parentPageId")
    private String parentPageId;

    /**
     * Confluence username
     */
    @Parameter(property = "confluence.userName", defaultValue = "kedar")
    private String username;
    /**
     * Confluence password
     */
    @Parameter(property = "confluence.password", defaultValue = "Hello123")
    private String password;

    @Parameter(alias="title", property = "project.build.finalName", required = false)
    private String title;

    /**
     * During publish of documentation related to a new release, if it's true, the pages related to SNAPSHOT will be removed
     */
    @Parameter(property = "confluence.removeSnapshots", required = false,  defaultValue = "false")
    private boolean removeSnapshots = false;


    public Map getProperties() {
        return properties;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public String getParentPageTitle() {
        return parentPageTitle;
    }

    public String getParentPageId() {
        return parentPageId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRemoveSnapshots() {
        return removeSnapshots;
    }

    public boolean isSnapshot() {
        final String version = project.getVersion();

        return (version != null && version.endsWith("-SNAPSHOT"));

    }

    protected final String getTitle() {
        return title;
    }

    /**
     * @param task
     * @throws MojoExecutionException
     */
    protected void confluenceExecute(ConfluenceTask task) throws MojoExecutionException {
        Confluence confluence = null;
        try {
            confluence = ConfluenceFactory.createInstanceDetectingVersion(getEndPoint(), null, getUsername(), getPassword());
            getLog().info(ConfluenceUtils.getVersion(confluence));
            task.execute(confluence);
        } catch (Exception e) {

            getLog().error("has been imposssible connect to confluence due exception", e);

            throw new MojoExecutionException("has been imposssible connect to confluence due exception", e);
        } finally {
            confluenceLogout(confluence);
        }
    }

    /**
     *
     * @param confluence
     * @return
     * @throws MojoExecutionException
     */
    protected Page loadParentPage(Confluence confluence) throws MojoExecutionException {

        Page result = null;
        if( parentPageId != null ) {

            try {
                result = confluence.getPage( parentPageId );

                if( result==null ) {
                    getLog().warn( format( "parentPageId [%s] not found! Try with parentPageTitle [%s] in space [%s]",
                            parentPageId, parentPageTitle, spaceKey));
                }
            } catch (SwizzleException ex) {
                getLog().warn( format( "cannot get page with parentPageId [%s]! Try with parentPageTitle [%s] in space [%s]\n%s",
                        parentPageId, parentPageTitle, spaceKey, ex.getMessage()) );

            }
        }

        if( result == null  ) {
            if( spaceKey == null ) {
                throw new MojoExecutionException( "spaceKey is not set!");
            }
            try {
                result = confluence.getPage(spaceKey, parentPageTitle);

                if( result==null ) {
                    throw new MojoExecutionException( format( "parentPageTitle [%s] not found in space [%s]!",
                            parentPageTitle, spaceKey));
                }
            } catch (SwizzleException ex) {
                throw new MojoExecutionException( format( "cannot get page with parentPageTitle [%s] in space [%s]!",
                        parentPageTitle, spaceKey), ex);
            }
        }
//        getProperties().put("parentPageTitle", result.getTitle());

        return result;

    }

    protected String createProjectHome( final Site site, final Locale locale) throws MojoExecutionException {

        try {
            final java.io.InputStream is = Site.processUri(site.getHome().getUri(), this.getTitle()) ;

            final MiniTemplator t = new MiniTemplator.Builder()
                    .setSkipUndefinedVars(true)
                    .build( is, Charset.forName("UTF-8") );

            return t.generateOutput();

        } catch (Exception e) {
            final String msg = "error loading template";
            getLog().error(msg, e);
            throw new MojoExecutionException(msg, e);
        }

    }

    protected void generateChildren(    final Confluence confluence,
                                        final Site.Page parentPage,
                                        final Page confluenceParentPage,
                                        final String parentPageTitle,
                                        final String titlePrefix)
    {

        getLog().info(format("generateChildren # [%d]", parentPage.getChildren().size()));


        generateAttachments(parentPage, confluence, confluenceParentPage);

        for( Site.Page child : parentPage.getChildren() ) {

            final Page confluencePage = generateChild(confluence, child, confluenceParentPage.getSpace(), parentPage.getName(), titlePrefix);

            if( confluencePage != null  ) {

                generateChildren(confluence, child, confluencePage, child.getName(), titlePrefix );
            }

        }

    }

    /**
     *
     * @param page
     * @param confluence
     * @param confluencePage
     */
    private void generateAttachments( Site.Page page,  Confluence confluence, Page confluencePage) {

        getLog().info(format("generateAttachments pageId [%s] title [%s]", confluencePage.getId(), confluencePage.getTitle()));

        for( Site.Attachment attachment : page.getAttachments() ) {

            Attachment confluenceAttachment = null;

            try {
                confluenceAttachment = confluence.getAttachment(confluencePage.getId(), attachment.getName(), attachment.getVersion());
            } catch (Exception e) {
                getLog().debug(format("Error getting attachment [%s] from confluence: [%s]", attachment.getName(), e.getMessage()));
            }

            if (confluenceAttachment != null) {

                java.util.Date date = confluenceAttachment.getCreated();

                if (date == null) {
                    getLog().warn(format("creation date of attachments [%s] is undefined. It will be replaced! ", confluenceAttachment.getFileName()));
                } else {
                    if (attachment.hasBeenUpdatedFrom(date)) {
                        getLog().info(format("attachment [%s] is more recent than the remote one. It will be replaced! ", confluenceAttachment.getFileName()));
                    } else {
                        getLog().info(format("attachment [%s] skipped! no updated detected", confluenceAttachment.getFileName()));
                        continue;

                    }
                }
            } else {
                getLog().info(format("Creating new attachment for [%s]", attachment.getName()));
                confluenceAttachment = new Attachment();
                confluenceAttachment.setFileName(attachment.getName());
                confluenceAttachment.setContentType(attachment.getContentType());

            }

            confluenceAttachment.setComment( attachment.getComment());

            try {
                ConfluenceUtils.addAttchment(confluence, confluencePage, confluenceAttachment, attachment.getUri().toURL() );
            } catch (Exception e) {
                getLog().error(format("Error uploading attachment [%s] ", attachment.getName()), e);
            }

        }

    }

    protected <T extends Site.Page> Page  generateChild(Confluence confluence,  T child, String spaceKey, String parentPageTitle, String titlePrefix) {

        java.net.URI source = child.getUri(getProject(), ".confluence");

        getLog().info( String.format("generateChild spacekey=[%s] parentPageTtile=[%s]\n%s", spaceKey, parentPageTitle, child.toString()));

        try {

            if (!isSnapshot() && isRemoveSnapshots()) {
                final String snapshot = titlePrefix.concat("-SNAPSHOT");
                boolean deleted = ConfluenceUtils.removePage(confluence, spaceKey, parentPageTitle, snapshot);

                if (deleted) {
                    getLog().info(String.format("Page [%s] has been removed!", snapshot));
                }
            }

            final String pageName = String.format("%s - %s", titlePrefix, child.getName());

            Page p = ConfluenceUtils.getOrCreatePage(confluence, spaceKey, parentPageTitle, pageName);

            if ( source != null ) {

                final java.io.InputStream is = Site.processUri(source, this.getTitle()) ;

                final MiniTemplator t = new MiniTemplator.Builder()
                        .setSkipUndefinedVars(true)
                        .build( is, Charset.forName("UTF-8") );

                if( !child.isIgnoreVariables() ) {

                    addStdProperties(t);

                    t.setVariableOpt("childTitle", pageName);
                }


                p.setContent(t.generateOutput());
            }


            p = confluence.storePage(p);

            for( String label : child.getComputedLabels() ) {

                confluence.addLabelByName(label, Long.parseLong(p.getId()) );
            }

            child.setName( pageName );

            return p;

        } catch (Exception e) {
            final String msg = "error loading template";
            getLog().error(msg, e);

            return null;
        }

    }

    private void addStdProperties(MiniTemplator t) {
        java.util.Map<String, String> props = getProperties();

        if (props == null || props.isEmpty()) {
            getLog().info("no properties set!");
        } else {
            for (java.util.Map.Entry<String, String> e : props.entrySet()) {

                try {
                    t.setVariable(e.getKey(), e.getValue(), true /* isOptional */);
                } catch (MiniTemplator.VariableNotDefinedException e1) {
                    getLog().warn(String.format("variable %s not defined in template", e.getKey()));
                }
            }
        }

    }

    /**
     * @param confluence
     */
    private void confluenceLogout(Confluence confluence) {
        if (null == confluence) {
            return;
        }
        try {
            if (!confluence.logout()) {
                getLog().warn("confluence logout has failed!");
            }
        } catch (Exception e) {
            getLog().warn("confluence logout has failed due exception ", e);
        }
    }

    protected void setPageUriFormFile( Site.Page page, java.io.File source ) {
        if( page == null ) {
            throw new IllegalArgumentException( "page is null!");
        }

        if (source != null && source.exists() && source.isFile() && source.canRead() ) {
            page.setUri(source.toURI());
        }
        else {
            try {
                java.net.URL sourceUrl = getClass().getClassLoader().getResource("defaultTemplate.confluence");
                page.setUri( sourceUrl.toURI() );
            } catch (URISyntaxException ex) {
                // TODO log
            }
        }

    }
}
