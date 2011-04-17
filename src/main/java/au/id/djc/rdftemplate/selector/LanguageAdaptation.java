package au.id.djc.rdftemplate.selector;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Literal;

// XXX use a better result type than just String
public class LanguageAdaptation extends AbstractAdaptation<String, Literal> {
    
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    public LanguageAdaptation() {
        super(String.class, new Class<?>[] { }, Literal.class);
    }
    
    @Override
    protected String doAdapt(Literal literal) {
        if (literal.isWellFormedXML()) {
            try {
                return getXMLLang(literal.getLexicalForm());
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        } else {
            return literal.getLanguage();
        }
    }
    
    private String getXMLLang(String literal) throws XMLStreamException {
        XMLEventReader reader = inputFactory.createXMLEventReader(new StringReader(literal));
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                return getElementLang(event.asStartElement());
            }
        }
        return null;
    }
    
    // ughhhhhh
    public static String getElementLang(StartElement se) {
        // xml:lang takes precedence
        QName xmlLangQName = new QName(
                se.getNamespaceURI("") == XMLConstants.XML_NS_URI ? "" : XMLConstants.XML_NS_URI, "lang");
        Attribute xmlLang = se.getAttributeByName(xmlLangQName);
        if (xmlLang != null)
            return xmlLang.getValue();

        QName xhtmlLangQName = new QName(se.getNamespaceURI("") == XHTML_NS_URI ? "" : XHTML_NS_URI, "lang");
        Attribute xhtmlLang = se.getAttributeByName(xhtmlLangQName);
        if (xhtmlLang != null)
            return xhtmlLang.getValue();

        return null;
    }

}
