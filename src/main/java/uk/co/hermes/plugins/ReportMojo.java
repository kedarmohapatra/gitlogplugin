package uk.co.hermes.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "report")
public class ReportMojo extends AbstractMojo
{
    @Parameter( property = "fileName", defaultValue = "gitlog" )
    private String fileName;

    public void execute() throws MojoExecutionException
    {
        getLog().info( fileName );
    }
}