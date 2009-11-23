package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Returns the anchor component of the node's URI (excluding initial #), or the
 * empty string if it has no anchor component.
 */
public class UriAnchorAdaptation implements Adaptation<String> {
    
    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }

    @Override
    public String adapt(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to apply #uri-anchor to non-resource node " + node);
        }
        String uri = ((Resource) node).getURI();
        int hashIndex = uri.lastIndexOf('#');
        if (hashIndex < 0)
            return "";
        return uri.substring(hashIndex + 1);
    }

}
