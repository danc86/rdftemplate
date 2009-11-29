package au.com.miskinhill.rdftemplate;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.hp.hpl.jena.rdf.model.RDFNode;

import au.com.miskinhill.rdftemplate.selector.Selector;

public class JoinAction extends TemplateAction {
    
    public static final String ACTION_NAME = "join";
    public static final QName ACTION_QNAME = new QName(TemplateInterpolator.NS, ACTION_NAME);
    
    private final List<XMLEvent> tree;
    private final Selector<RDFNode> selector;
    private final String separator;
    
    public JoinAction(List<XMLEvent> tree, Selector<RDFNode> selector, String separator) {
        this.tree = tree;
        this.selector = selector;
        this.separator = separator;
    }
    
    public void evaluate(TemplateInterpolator interpolator, RDFNode node, XMLEventDestination writer, XMLEventFactory eventFactory)
            throws XMLStreamException {
        boolean first = true;
        for (RDFNode eachNode: selector.result(node)) {
            if (!first) {
                writer.add(eventFactory.createCharacters(separator));
            }
            interpolator.interpolate(tree.iterator(), eachNode, writer);
            first = false;
        }
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("selector", selector)
                .append("separator", separator)
                .toString();
    }

}
