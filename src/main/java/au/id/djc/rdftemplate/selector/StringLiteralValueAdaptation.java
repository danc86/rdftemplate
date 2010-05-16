package au.id.djc.rdftemplate.selector;

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Literal;

public class StringLiteralValueAdaptation extends AbstractAdaptation<String, Literal> {
    
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    public StringLiteralValueAdaptation() {
        super(String.class, new Class<?>[] { }, Literal.class);
    }
    
    @Override
    protected String doAdapt(Literal literal) {
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
