package de.sstoehr.handlebars.compiler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
public class HandlebarsCompilerTest {

    @Test
    public void testName() {
        HandlebarsCompiler compiler = new HandlebarsCompiler();

        Assert.assertEquals("template", compiler.getCanonicalName("template.hbs"));
        Assert.assertEquals("template", compiler.getCanonicalName("template.handlebars"));
        Assert.assertEquals("test", compiler.getCanonicalName("test"));
    }

    @Test
    public void testCompile() {
        HandlebarsCompiler compiler = new HandlebarsCompiler();

        Assert.assertEquals("{\"compiler\":[6,\">= 2.0.0-beta.1\"],\"main\":function(depth0,helpers,partials,data) {\n"
          + "  var helper, functionType=\"function\", helperMissing=helpers.helperMissing, escapeExpression=this.escapeExpression;\n"
          + "  return \"<p>\"\n"
          + "    + escapeExpression(((helper = (helper = helpers.test || (depth0 != null ? depth0.test : depth0)) != null ? helper : helperMissing),(typeof helper === functionType ? helper.call(depth0, {\"name\":\"test\",\"hash\":{},\"data\":data}) : helper)))\n"
          + "    + \"</p>\";\n"
          + "},\"useData\":true}", compiler.compile("<p>{{test}}</p>"));
    }

    @Test
    public void testBatchCompile() {
        HandlebarsCompiler compiler = new HandlebarsCompiler();

        Assert.assertEquals("", compiler.compileBatch(null, "test"));
        Assert.assertEquals("", compiler.compileBatch(null, null));
        Assert.assertEquals("", compiler.compileBatch(null, ""));

        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("main.hbs", "<p>{{main}}</p>");
        templates.put("test.hbs", "abc");

        Assert.assertEquals("TEMPLATES = TEMPLATES || {};\n"
          + "(function (namespace, Handlebars) {\n"
          + "namespace['main'] = Handlebars.template({\"compiler\":[6,\">= 2.0.0-beta.1\"],\"main\":function(depth0,helpers,partials,data) {\n"
          + "  var helper, functionType=\"function\", helperMissing=helpers.helperMissing, escapeExpression=this.escapeExpression;\n"
          + "  return \"<p>\"\n"
          + "    + escapeExpression(((helper = (helper = helpers.main || (depth0 != null ? depth0.main : depth0)) != null ? helper : helperMissing),(typeof helper === functionType ? helper.call(depth0, {\"name\":\"main\",\"hash\":{},\"data\":data}) : helper)))\n"
          + "    + \"</p>\";\n"
          + "},\"useData\":true});\n"
          + "namespace['test'] = Handlebars.template({\"compiler\":[6,\">= 2.0.0-beta.1\"],\"main\":function(depth0,helpers,partials,data) {\n"
          + "  return \"abc\";\n"
          + "  },\"useData\":true});\n"
          + "}(TEMPLATES, Handlebars));\n", compiler.compileBatch(templates, "TEMPLATES"));
    }
}
