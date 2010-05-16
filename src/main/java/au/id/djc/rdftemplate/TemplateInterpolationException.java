package au.id.djc.rdftemplate;

import javax.xml.stream.Location;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class TemplateInterpolationException extends RuntimeException {
    
    private static final long serialVersionUID = -1472104970210074672L;

    public TemplateInterpolationException(Location location, TemplateAction action, RDFNode node, Throwable cause) {
        super("Exception evaluating action [" + action + "] " +
                "at location [" + location.getLineNumber() + "," + location.getColumnNumber() + "] " +
                "with context node " + node, cause);
    }
    
    public TemplateInterpolationException(Location location, String selectorExpression, RDFNode node, Throwable cause) {
        super("Exception evaluating selector expression [" + selectorExpression + "] " +
                "at location [" + location.getLineNumber() + "," + location.getColumnNumber() + "] " +
                "with context node " + node, cause);
    }

}
