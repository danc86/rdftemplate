package au.com.miskinhill.rdftemplate.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deadlit.rdf.util.SdbTemplate;
import org.deadlit.rdf.util.SdbTemplate.ModelExecutionCallbackWithoutResult;
import org.springframework.web.servlet.view.AbstractTemplateView;

import au.com.miskinhill.rdftemplate.TemplateInterpolator;
import au.com.miskinhill.rdftemplate.selector.SelectorFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class RDFTemplateView extends AbstractTemplateView {
    
    public static final String NODE_URI_KEY = "nodeUri";
    
    private TemplateInterpolator templateInterpolator;
    private SelectorFactory selectorFactory;
    private SdbTemplate sdbTemplate;
    
    public void setSelectorFactory(SelectorFactory selectorFactory) {
        this.selectorFactory = selectorFactory;
    }
    
    public void setSdbTemplate(SdbTemplate sdbTemplate) {
        this.sdbTemplate = sdbTemplate;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (selectorFactory == null) {
            throw new IllegalArgumentException("Property 'selectorFactory' is required");
        }
        if (sdbTemplate == null) {
            throw new IllegalArgumentException("Property 'sdbTemplate' is required");
        }
        this.templateInterpolator = new TemplateInterpolator(selectorFactory);
    }

    @Override
    protected void renderMergedTemplateModel(final Map<String, Object> model,
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final InputStream inputStream = getApplicationContext().getResource(getUrl()).getInputStream();
        try {
            sdbTemplate.withModel(new ModelExecutionCallbackWithoutResult() {
                @Override
                protected void executeWithoutResult(Model rdfModel) {
                    Resource node = rdfModel.getResource((String) model.get(NODE_URI_KEY));
                    try {
                        templateInterpolator.interpolate(inputStream, node, response.getWriter());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } finally {
            inputStream.close();
        }
    }
    
    @Override
    public boolean checkResource(Locale locale) throws Exception {
        return getApplicationContext().getResource(getUrl()).exists();
    }

}
