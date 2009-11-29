package au.com.miskinhill.rdftemplate;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.builder.ToStringBuilder;

import au.com.miskinhill.rdftemplate.selector.Selector;

public class ForAction extends TemplateAction {
    
    public static final String ACTION_NAME = "for";
    public static final QName ACTION_QNAME = new QName(TemplateInterpolator.NS, ACTION_NAME);
    
    private final List<XMLEvent> tree;
    private final Selector<RDFNode> selector;
    
    public ForAction(List<XMLEvent> tree, Selector<RDFNode> selector) {
        this.tree = tree;
        this.selector = selector;
    }
    
    public void evaluate(TemplateInterpolator interpolator, RDFNode node, XMLEventDestination writer)
            throws XMLStreamException {
        for (RDFNode eachNode: selector.result(node)) {
            interpolator.interpolate(tree.iterator(), eachNode, writer);
        }
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("selector", selector)
                .toString();
    }

}
