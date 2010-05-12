package au.com.miskinhill.rdftemplate.view;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

import au.com.miskinhill.rdftemplate.selector.SelectorFactory;
import au.id.djc.jena.util.ModelOperations;

public class RDFTemplateViewResolver extends AbstractTemplateViewResolver implements InitializingBean {
    
    private SelectorFactory selectorFactory;
    private ModelOperations modelOperations;
    
    public RDFTemplateViewResolver() {
        super();
        setViewClass(requiredViewClass());
        setExposeRequestAttributes(false);
        setExposeSessionAttributes(false);
        setExposeSpringMacroHelpers(false);
    }
    
    public void setSelectorFactory(SelectorFactory selectorFactory) {
        this.selectorFactory = selectorFactory;
    }

    public void setModelOperations(ModelOperations modelOperations) {
        this.modelOperations = modelOperations;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (selectorFactory == null) {
            throw new IllegalArgumentException("Property 'selectorFactory' is required");
        }
        if (modelOperations == null) {
            throw new IllegalArgumentException("Property 'modelOperations' is required");
        }
    }
    
    @Override
    protected Class<? extends AbstractTemplateView> requiredViewClass() {
        return RDFTemplateView.class;
    }
    
    @Override
    protected RDFTemplateView buildView(String viewName) throws Exception {
        RDFTemplateView view = (RDFTemplateView) super.buildView(viewName);
        view.setSelectorFactory(selectorFactory);
        view.setModelOperations(modelOperations);
        return view;
    }

}
