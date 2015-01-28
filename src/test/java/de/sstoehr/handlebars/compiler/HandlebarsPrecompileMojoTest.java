package de.sstoehr.handlebars.compiler;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import de.sstoehr.handlebars.HandlebarsPrecompileMojo;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({HandlebarsPrecompileMojo.class})
public class HandlebarsPrecompileMojoTest {

    private HandlebarsPrecompileMojo mojo;

    @Before
    public void setup() {
        mojo = new HandlebarsPrecompileMojo();

        MavenProject project = mock(MavenProject.class);
        when(project.getBasedir()).thenReturn(new File("."));
        Whitebox.setInternalState(mojo, MavenProject.class, project);
    }

    @Test
    public void test() throws Exception {
        File outputFile = File.createTempFile("handlebars", "test");
        outputFile.deleteOnExit();

        HandlebarsCompiler compiler = mock(HandlebarsCompiler.class);
        when(compiler.compileBatch(anyMap(), anyString())).thenReturn("Compiled result");

        whenNew(HandlebarsCompiler.class).withAnyArguments().thenReturn(compiler);

        Whitebox.setInternalState(mojo, "namespace", "TEST.namespace");
        Whitebox.setInternalState(mojo, "outputFile", outputFile.getAbsolutePath());

        mojo.execute();

        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("testA.hbs", "This is a <strong>{{test}}</strong>.\n"
          + "\n"
          + "<p>It should be compiled correctly.</p>");

        verify(compiler, times(1)).compileBatch(templates, "TEST.namespace");

        World world = new World();
        Node node = world.file(outputFile);

        Assert.assertEquals("Compiled result", node.readString());
    }

}
