package com.nao.collectibles.view;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.StringWriter;
import java.util.Map;

/**
 * Renderiza un parcial (p.ej. items.mustache) a String para
 * inyectarlo dentro de un layout.mustache como {{{content}}}.
 */
public class MustacheRenderer {
    private static final MustacheTemplateEngine engine = new MustacheTemplateEngine();

    public static String partial(String template, Map<String, Object> model) {
        StringWriter writer = new StringWriter();
        // Renderizamos SOLO el parcial a un string…
        writer.write(engine.render(new ModelAndView(model, template)));
        return writer.toString();
    }
}
