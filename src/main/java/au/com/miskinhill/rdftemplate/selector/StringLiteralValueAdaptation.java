package au.com.miskinhill.rdftemplate.selector;

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class StringLiteralValueAdaptation implements Adaptation<String> {
    
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }

    @Override
    public String adapt(RDFNode node) {
        if (!node.isLiteral()) {
            throw new SelectorEvaluationException("Attempted to apply #lv to non-literal node " + node);
        }
        Literal literal = node.as(Literal.class);
        if (literal.isWellFormedXML()) {
            try {
                return stripTags(literal.getLexicalForm());
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        } else {
            return literal.getValue().toString();
        }
    }
    
    private String stripTags(String literal) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        XMLEventReader reader = inputFactory.createXMLEventReader(new StringReader(literal));
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isCharacters()) {
                sb.append(event.asCharacters().getData());
            }
        }
        return sb.toString();
    }

}
