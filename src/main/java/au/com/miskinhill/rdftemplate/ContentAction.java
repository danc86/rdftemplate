package au.com.miskinhill.rdftemplate;

import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.util.XMLEventConsumer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import au.com.miskinhill.rdftemplate.selector.Selector;

public class ContentAction extends TemplateAction {
    
    public static final String ACTION_NAME = "content";
    public static final QName ACTION_QNAME = new QName(TemplateInterpolator.NS, ACTION_NAME);
    private static final QName XML_LANG_QNAME = new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX);
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    
    private final StartElement start;
    private final Selector<?> selector;
    
    public ContentAction(StartElement start, Selector<?> selector) {
        this.start = start;
        this.selector = selector;
    }
    
    public void evaluate(TemplateInterpolator interpolator, RDFNode node, XMLEventConsumer writer, XMLEventFactory eventFactory)
            throws XMLStreamException {
        Object replacement = selector.singleResult(node);
        StartElement start = interpolator.interpolateAttributes(this.start, node);
        Set<Attribute> attributes = interpolator.cloneAttributesWithout(start, ACTION_QNAME);
        if (replacement instanceof Literal) {
            Literal literal = (Literal) replacement;
            if (!StringUtils.isEmpty(literal.getLanguage())) {
                attributes.add(eventFactory.createAttribute(XML_LANG_QNAME, ((Literal) replacement).getLanguage()));
                if (start.getName().getNamespaceURI().equals(XHTML_NS_URI)) {
                    String xhtmlPrefixInContext = start.getNamespaceContext().getPrefix(XHTML_NS_URI);
                    QName xhtmlLangQNameForContext; // ugh
                    if (xhtmlPrefixInContext.isEmpty())
                        xhtmlLangQNameForContext = new QName("lang");
                    else
                        xhtmlLangQNameForContext = new QName(XHTML_NS_URI, "lang", xhtmlPrefixInContext);
                    attributes.add(eventFactory.createAttribute(xhtmlLangQNameForContext, literal.getLanguage()));
                }
            }
        }
        writer.add(eventFactory.createStartElement(start.getName(), attributes.iterator(), start.getNamespaces()));
        interpolator.writeTreeForContent(writer, replacement);
        writer.add(eventFactory.createEndElement(start.getName(), start.getNamespaces()));
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("start", start)
                .append("selector", selector)
                .toString();
    }

}
