package uk.co.hermes.plugins.renderer;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

public class GitlogRenderer extends AbstractMavenReportRenderer {

    public GitlogRenderer(Sink sink) {
        super(sink);
    }

    public String getTitle() {
        return "gitlog title";
    }

    protected void renderBody() {
        sink.title();
        sink.text("kedar");
        sink.title_();
    }
}
