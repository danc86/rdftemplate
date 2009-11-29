package au.com.miskinhill.rdftemplate;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Seq;
import org.apache.commons.lang.builder.ToStringBuilder;

import au.com.miskinhill.rdftemplate.selector.Selector;

public class JoinAction extends TemplateAction {
    
    public static final String ACTION_NAME = "join";
    public static final QName ACTION_QNAME = new QName(TemplateInterpolator.NS, ACTION_NAME);
    private static final Logger LOG = Logger.getLogger(JoinAction.class.getName());
    
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
        List<RDFNode> result = selector.result(node);
        if (result.size() == 1 && result.get(0).canAs(Resource.class)) {
            if (result.get(0).as(Resource.class).hasProperty(RDF.type, RDF.Seq)) {
                LOG.fine("Apply rdf:Seq special case for " + result.get(0));
                result = result.get(0).as(Seq.class).iterator().toList();
                LOG.fine("Resulting sequence is " + result);
            }
        }
        boolean first = true;
        for (RDFNode eachNode: result) {
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
