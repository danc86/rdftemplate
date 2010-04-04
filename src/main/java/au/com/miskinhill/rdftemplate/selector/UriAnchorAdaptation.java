package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Returns the anchor component of the node's URI (excluding initial #), or the
 * empty string if it has no anchor component.
 */
public class UriAnchorAdaptation extends AbstractAdaptation<String, Resource> {
    
    public UriAnchorAdaptation() {
        super(String.class, new Class<?>[] { }, Resource.class);
    }
    
    @Override
    protected String doAdapt(Resource node) {
        String uri = node.getURI();
        int hashIndex = uri.lastIndexOf('#');
        if (hashIndex < 0)
            return "";
        return uri.substring(hashIndex + 1);
    }

}
