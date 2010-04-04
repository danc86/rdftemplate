package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Literal;

public class LiteralValueAdaptation extends AbstractAdaptation<Object, Literal> {
    
    public LiteralValueAdaptation() {
        super(Object.class, new Class<?>[] { }, Literal.class);
    }
    
    @Override
    protected Object doAdapt(Literal node) {
        return node.getValue();
    }

}
