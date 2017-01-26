package uk.co.hermes.plugins.renderer;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

public class GitlogRenderer {

    public void renderBody(Sink sink) {
        sink.head();

        sink.title();
        sink.text( "some report" );
        sink.title_();

        sink.head_();

        sink.body();
        sink.section1();
        sink.sectionTitle1();
        sink.text( "section text" );
        sink.sectionTitle1_();
        sink.body_();

        sink.flush();

        sink.close();
    }
}
