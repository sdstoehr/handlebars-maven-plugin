package de.sstoehr.handlebars;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sstoehr.handlebars.compiler.HandlebarsCompiler;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.filter.Filter;

@Mojo(name = "precompile", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = true)
public class HandlebarsPrecompileMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory.getLogger(HandlebarsPrecompileMojo.class);

    private static final String DELIMITER = ",";

    @Parameter
    private String templatesFilter = "**/*.hbs";

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}/template.js")
    private String outputFile;

    @Parameter
    private String fileHeader = "var Handlebars = Handlebars || {};";

    @Parameter
    private String namespace = "Handlebars.templates";

    @Component
    private MavenProject project;

    public HandlebarsPrecompileMojo() {
        super();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        HandlebarsCompiler compiler = new HandlebarsCompiler();

        StringBuilder output = new StringBuilder();
        output.append(fileHeader);
        if (fileHeader.length() > 0) {
            output.append("\n");
        }

        World world = new World();
        Node base = world.file(project.getBasedir());

        Filter filter = getFilter(templatesFilter);

        Map<String, String> templates = new LinkedHashMap<>();

        try {
            for (Node template : base.find(filter)) {
                String name = template.getName();
                String templateContent = template.readString();

                templates.put(name, templateContent);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading templates", e);
        }

        String compiled = compiler.compileBatch(templates, namespace);
        output.append(compiled);

        Node outputNode = world.file(outputFile);
        try {
            outputNode.getParent().mkdirsOpt();
            outputNode.writeString(output.toString());
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing output file.", e);
        }
    }

    private Filter getFilter(String filter) {
        Filter f = new Filter();
        String[] includes = filter.split(DELIMITER);
        for (String include : includes) {
            f.include(include);
        }
        return f;
    }
}
