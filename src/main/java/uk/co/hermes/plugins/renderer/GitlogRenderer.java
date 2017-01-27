package uk.co.hermes.plugins.renderer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.maven.doxia.sink.Sink;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitlogRenderer {

    private Sink sink;

    public GitlogRenderer(Sink sink) {
        this.sink = sink;
    }

    public void renderBody() {
        sink.head();

        sink.title();
        sink.text("some report");
        sink.title_();

        sink.head_();

        sink.body();
        sink.section1();
        sink.sectionTitle1();
        sink.text("section text");
        sink.sectionTitle1_();
        sink.body_();

        sink.flush();

        sink.close();
    }

    public void renderHeader(String header) {
        sink.title();
        sink.text(header);
        sink.title_();
    }

    public void renderTag(RevTag tag) throws IOException {
        sink.tableRow();
        sinkCell(sink, htmlEncode(tag.getTagName()));
        sink.tableRow_();

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

    private void sinkCell( Sink sink, String text )
    {
        sink.tableCell();
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

    public void renderCommit(RevCommit commit) {
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(commit.getCommitTime()));

        String message = null;

            message = messageConverter.formatCommitMessage(htmlEncode(commit.getShortMessage()));
    }

    public void renderFooter() {

    }

    public void close() {

    }
}
