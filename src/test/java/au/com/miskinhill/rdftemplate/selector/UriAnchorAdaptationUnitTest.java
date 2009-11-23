package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.ResourceFactory;
import org.junit.Test;

public class UriAnchorAdaptationUnitTest {

    @Test
    public void shouldReturnAnchor() {
        String result = new UriAnchorAdaptation().adapt(ResourceFactory.createResource("http://example.com/#asdf"));
        assertThat(result, equalTo("asdf"));
    }
    
    @Test
    public void shouldReturnEmptyStringForUriWithoutAnchor() {
        String result = new UriAnchorAdaptation().adapt(ResourceFactory.createResource("http://example.com/"));
        assertThat(result, equalTo(""));
    }

}
