package au.com.miskinhill.rdftemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

public class XMLStream implements Iterable<XMLEvent> {
    
    public static XMLStream collect(Iterator<XMLEvent> it) {
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        while (it.hasNext()) {
            XMLEvent event = it.next();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.END_DOCUMENT:
                    break; // discard
                default:
                    events.add(event);
            }
        }
        return new XMLStream(events);
    }
    
    private final List<XMLEvent> events;
    
    public XMLStream(XMLEvent... events) {
        this.events = Arrays.asList(events);
    }
    
    public XMLStream(List<XMLEvent> events) {
        this.events = events;
    }
    
    @Override
    public Iterator<XMLEvent> iterator() {
        return events.iterator();
    }

}
