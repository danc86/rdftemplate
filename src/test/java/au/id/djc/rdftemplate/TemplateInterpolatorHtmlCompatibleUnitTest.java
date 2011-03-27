package au.id.djc.rdftemplate;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.InputStreamReader;

import javax.xml.stream.XMLOutputFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.junit.Before;
import org.junit.Test;

import au.id.djc.rdftemplate.selector.AntlrSelectorFactory;

public class TemplateInterpolatorHtmlCompatibleUnitTest {
    
    private XMLOutputFactory outputFactory;
    private Model model;
    private TemplateInterpolator templateInterpolator;
    
    @Before
    public void setUp() {
        outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        model = ModelFactory.createDefaultModel();
        AntlrSelectorFactory selectorFactory = new AntlrSelectorFactory();
        selectorFactory.setNamespacePrefixMap(TestNamespacePrefixMap.getInstance());
        templateInterpolator = new TemplateInterpolator(selectorFactory, /* htmlCompatible */ true);
    }
    
    @Test
    public void should_strip_nested_anchors() {
        Resource book = model.createResource("http://miskinhill.com.au/cited/books/lermontov-1899");
        String title = "<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">" +
                "this has a <a href=\"http://elsewhere.invalid\">nested anchor</a></span>";
        model.add(model.createLiteralStatement(book, DCTerms.title, model.createLiteral(title, true)));
        InputStreamReader templateReader = new InputStreamReader(this.getClass().getResourceAsStream("nested-anchors.xml"));
        String result = templateInterpolator.interpolate(templateReader, book);
        assertThat(result, containsString(
                "<a href=\"http://nowhere.invalid/\"><span lang=\"en\">this has a nested anchor</span></a>"));
    }

}
