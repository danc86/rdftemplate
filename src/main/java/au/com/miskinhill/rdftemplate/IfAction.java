package au.com.miskinhill.rdftemplate;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.hp.hpl.jena.rdf.model.RDFNode;

import au.com.miskinhill.rdftemplate.selector.Selector;

public class IfAction extends TemplateAction {
    
    public static final String ACTION_NAME = "if";
    public static final QName ACTION_QNAME = new QName(TemplateInterpolator.NS, ACTION_NAME);
    
    private final List<XMLEvent> tree;
    private final Selector<?> condition;
    private final boolean negate;
    
    public IfAction(List<XMLEvent> tree, Selector<?> condition, boolean negate) {
        this.tree = tree;
        this.condition = condition;
        this.negate = negate;
    }
    
    public void evaluate(TemplateInterpolator interpolator, RDFNode node, XMLEventDestination writer)
            throws XMLStreamException {
        List<?> selectorResult = condition.result(node);
        if (negate ? selectorResult.isEmpty() : !selectorResult.isEmpty()) {
            interpolator.interpolate(tree.iterator(), node, writer);
        }
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("condition", condition)
                .append("negate", negate)
                .toString();
    }

}
