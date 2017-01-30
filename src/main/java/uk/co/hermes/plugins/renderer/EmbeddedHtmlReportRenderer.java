package uk.co.hermes.plugins.renderer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.maven.doxia.sink.Sink.JUSTIFY_LEFT;

public class EmbeddedHtmlReportRenderer {

    private Sink sink;
    private final MessageConverter messageConverter;
    private SinkEventAttributeSet dateCssClass;
    private SinkEventAttributeSet authorCssClass;
    private SinkEventAttributeSet tagCssClass;
    private SinkEventAttributeSet titleCssClass;

    public EmbeddedHtmlReportRenderer(Sink sink, MessageConverter messageConverter) {
        this.sink = sink;
        this.messageConverter = messageConverter;
        dateCssClass = new SinkEventAttributeSet("class", "git-commitdate");
        authorCssClass = new SinkEventAttributeSet("class", "git-user");
        tagCssClass = new SinkEventAttributeSet("colspan","3", "class", "git-tag");
        titleCssClass = new SinkEventAttributeSet("class", "gitlog-title");
    }

    public void startSection(){
        sink.head();
        sink.head_();
        sink.body();
        sink.section1();
    }

    public void renderHeader(String header) {
        sink.sectionTitle1();
        sink.text(header);
        sink.sectionTitle1_();
    }

    public void startTable(){
        sink.table();
        sink.tableRows( new int[] {JUSTIFY_LEFT}, false );
    }

    public void renderTableHeader() {
        sink.tableRow();
        sink.tableHeaderCell(dateCssClass);
        sink.text("Date");
        sink.tableHeaderCell_();
        sink.tableHeaderCell(authorCssClass);
        sink.text("User");
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text("Comment");
        sink.tableHeaderCell_();
    }

    public void renderTag(RevTag tag) throws IOException {
        sink.tableRow();
        sink.tableCell(tagCssClass);
        sink.text(htmlEncode(tag.getTagName().toUpperCase()));
        sink.tableCell_();
        sink.tableRow_();

    }

    public void renderCommit(RevCommit commit) {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date((long) commit.getCommitTime() *1000));
        String author = htmlEncode(commit.getCommitterIdent().getName());

        sink.tableRow();
        sinkCell(sink, dateCssClass,  date);
        sinkCell(sink, authorCssClass, author);
        sink.tableCell();
        sink.rawText(messageConverter.formatCommitMessage(htmlEncode(commit.getShortMessage())));
        sink.tableCell_();
        sink.tableRow_();

    }
    public void renderFooter() {

    }

    public void close() {

    }

    public void endTable(){
        sink.tableRows_();
        sink.table_();
    }

    public void endSection(){
        sink.section1_();
        sink.body_();

        sink.flush();

        sink.close();
    }

    private void sinkLineBreak( Sink sink )
    {
        sink.lineBreak();
    }

    private void sinkIcon( String type, Sink sink )
    {
        sink.figure();

        if ( type.startsWith( "junit.framework" ) || "skipped".equals( type ) )
        {
            sink.figureGraphics( "images/icon_warning_sml.gif" );
        }
        else if ( type.startsWith( "success" ) )
        {
            sink.figureGraphics( "images/icon_success_sml.gif" );
        }
        else
        {
            sink.figureGraphics( "images/icon_error_sml.gif" );
        }

        sink.figure_();
    }

    private void sinkHeader( Sink sink, String header )
    {
        sink.tableHeaderCell();
        sink.text( header );
        sink.tableHeaderCell_();
    }

    private void sinkCell( Sink sink, SinkEventAttributes attributes, String text )
    {
        sink.tableCell(attributes);
        sink.text( text );
        sink.tableCell_();
    }

    private void sinkLink( Sink sink, String text, String link )
    {
        sink.link( link );
        sink.text( text );
        sink.link_();
    }

    private void sinkCellLink( Sink sink, String text, String link )
    {
        sink.tableCell();
        sinkLink( sink, text, link );
        sink.tableCell_();
    }

    private void sinkCellAnchor( Sink sink, String text, String anchor )
    {
        sink.tableCell();
        sinkAnchor( sink, anchor );
        sink.text( text );
        sink.tableCell_();
    }

    private void sinkAnchor( Sink sink, String anchor )
    {
        sink.anchor( anchor );
        sink.anchor_();
    }

    protected static String htmlEncode(String input) {
        input = StringEscapeUtils.escapeHtml(input);
        return input.replaceAll(System.getProperty("line.separator"), "<br/>");
    }
}
