package au.com.miskinhill.rdftemplate;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/** Common superinterface which can be implemented by XMLEventWriter or List&lt;XMLEvent&gt;. */
public interface XMLEventDestination {
    
    void add(XMLEvent event) throws XMLStreamException;

}
