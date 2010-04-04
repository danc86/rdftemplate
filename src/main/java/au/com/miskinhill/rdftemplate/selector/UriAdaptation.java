package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Resource;

public class UriAdaptation extends AbstractAdaptation<String, Resource> {
    
    public UriAdaptation() {
        super(String.class, new Class<?>[] { }, Resource.class);
    }
    
    @Override
    protected String doAdapt(Resource node) {
        return node.getURI();
    }

}
