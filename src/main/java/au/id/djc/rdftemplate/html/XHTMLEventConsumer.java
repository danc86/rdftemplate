package au.id.djc.rdftemplate.html;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

/**
 * {@link XMLEventConsumer} implementation which indirects to another event
 * consumer, but munges the stream on the way through to adhere to the vagaries
 * of (X)HTML.
 * <p>
 * Only elements in the XHTML namespace are modified, elements from other
 * namespaces are passed through untouched.
 * <p>
 * The HTML-specific things currently handled by this class are:
 * <ul>
 * <li>Nested anchors are removed (they are
 * <a href="http://www.w3.org/TR/html401/struct/links.html#h-12.2.2">illegal</a>)</li>
 * </ul>
 */
// XXX do self-closing tags here too
public class XHTMLEventConsumer implements XMLEventConsumer {
    
    private static final QName XHTML_A_QNAME = new QName("http://www.w3.org/1999/xhtml", "a");
    
    private final XMLEventConsumer delegate;
    private int anchorNestDepth = 0;

    public XHTMLEventConsumer(XMLEventConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT: {
                StartElement start = event.asStartElement();
                if (XHTML_A_QNAME.equals(start.getName())) {
                    anchorNestDepth ++;
                    if (anchorNestDepth == 1)
                        delegate.add(event);
                } else {
                    delegate.add(event);
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT: {
                EndElement end = event.asEndElement();
                if (XHTML_A_QNAME.equals(end.getName())) {
                    if (anchorNestDepth == 1)
                        delegate.add(event);
                    anchorNestDepth --;
                } else {
                    delegate.add(event);
                }
                break;
            }
            default:
                delegate.add(event);
        }
    }

}
