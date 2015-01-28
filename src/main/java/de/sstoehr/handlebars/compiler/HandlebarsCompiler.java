package de.sstoehr.handlebars.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import net.oneandone.sushi.util.Strings;

public class HandlebarsCompiler {

    private static final String HANDLEBARS_JS = "js/handlebars-v2.0.0.js";

    private String handlebarsJs;

    public HandlebarsCompiler() {
        try (InputStream hbsJs = HandlebarsCompiler.class.getClassLoader().getResourceAsStream(HANDLEBARS_JS)) {
            handlebarsJs = IOUtils.toString(hbsJs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String compileBatch(Map<String, String> templates, String namespace) {
        if (templates == null || namespace == null || namespace.length() == 0) {
            return "";
        }

        StringBuilder output = new StringBuilder();

        output.append(namespace).append(" = ").append(namespace).append(" || {};").append("\n");

        output.append("(function (namespace, Handlebars) {\n");

        for (Map.Entry<String, String> template : templates.entrySet()) {
            String name = getCanonicalName(template.getKey());
            String compiled = this.compile(template.getValue());

            output.append("namespace['").append(name).append("']").append(" = ");
            output.append("Handlebars.template(").append(compiled).append(");\n");
        }

        output.append("}(").append(namespace).append(", Handlebars));\n");

        return output.toString();
    }

    public String compile(String template) {

        String compiled;

        try {
            Context context = Context.enter();

            Scriptable scope = context.initStandardObjects();

            context.evaluateString(scope, handlebarsJs, "<cmd>", 1, null);

            Scriptable handlebars = (Scriptable) scope.get("Handlebars", scope);
            Function precompile = (Function) handlebars.get("precompile", scope);

            Object result = precompile.call(context, scope, scope, new Object[]{template});

            compiled = Context.toString(result);

        } finally {
            Context.exit();
        }

        return compiled;
    }

    public String getCanonicalName(final String fileName) {
        String name = fileName;
        name = Strings.removeRightOpt(name, ".hbs");
        name = Strings.removeRightOpt(name, ".handlebars");
        return name;
    }
}

