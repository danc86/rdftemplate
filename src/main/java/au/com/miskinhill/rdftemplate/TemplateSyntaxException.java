package au.com.miskinhill.rdftemplate;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public class TemplateSyntaxException extends RuntimeException {

    private static final long serialVersionUID = 6518982504570154030L;
    
    public TemplateSyntaxException(Location location, String message) {
        super("[location " + location.getLineNumber() + "," + location.getColumnNumber() + "] " + message);
    }
    
    public TemplateSyntaxException(Location location, Throwable cause) {
        super("[location " + location.getLineNumber() + "," + location.getColumnNumber() + "]", cause);
    }
    
    public TemplateSyntaxException(XMLStreamException e) {
        super(e);
    }
    
}
