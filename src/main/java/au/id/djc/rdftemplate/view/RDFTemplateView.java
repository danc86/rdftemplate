package au.id.djc.rdftemplate.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractTemplateView;

import au.id.djc.rdftemplate.TemplateInterpolator;
import au.id.djc.rdftemplate.selector.SelectorFactory;
import au.id.djc.jena.util.ModelOperations;
import au.id.djc.jena.util.ModelOperations.ModelExecutionCallbackWithoutResult;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class RDFTemplateView extends AbstractTemplateView {
    
    public static final String NODE_URI_KEY = "nodeUri";
    
    private TemplateInterpolator templateInterpolator;
    private SelectorFactory selectorFactory;
    private ModelOperations modelOperations;
    
    public void setSelectorFactory(SelectorFactory selectorFactory) {
        this.selectorFactory = selectorFactory;
    }
    
    public void setModelOperations(ModelOperations modelOperations) {
        this.modelOperations = modelOperations;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (selectorFactory == null) {
            throw new IllegalArgumentException("Property 'selectorFactory' is required");
        }
        if (modelOperations == null) {
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
            modelOperations.withModel(new ModelExecutionCallbackWithoutResult() {
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
