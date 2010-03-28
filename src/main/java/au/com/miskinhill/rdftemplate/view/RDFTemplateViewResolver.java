package au.com.miskinhill.rdftemplate.view;

import org.deadlit.rdf.util.SdbTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

import au.com.miskinhill.rdftemplate.selector.SelectorFactory;

public class RDFTemplateViewResolver extends AbstractTemplateViewResolver implements InitializingBean {
    
    private SelectorFactory selectorFactory;
    private SdbTemplate sdbTemplate;
    
    public RDFTemplateViewResolver() {
        super();
        setViewClass(requiredViewClass());
    }
    
    public void setSelectorFactory(SelectorFactory selectorFactory) {
        this.selectorFactory = selectorFactory;
    }
    
    public void setSdbTemplate(SdbTemplate sdbTemplate) {
        this.sdbTemplate = sdbTemplate;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (selectorFactory == null) {
            throw new IllegalArgumentException("Property 'selectorFactory' is required");
        }
        if (sdbTemplate == null) {
            throw new IllegalArgumentException("Property 'sdbTemplate' is required");
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
        view.setSdbTemplate(sdbTemplate);
        return view;
    }

}
