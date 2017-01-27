package uk.co.hermes.plugins.renderer;

import org.apache.maven.doxia.sink.Sink;

public interface MessageConverter {

	String formatCommitMessage(String original);

}
