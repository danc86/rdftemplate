package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Literal;

@SuppressWarnings("unchecked")
public class ComparableLiteralValueAdaptation extends AbstractAdaptation<Comparable, Literal> {
    
    public ComparableLiteralValueAdaptation() {
        super(Comparable.class, new Class<?>[] { }, Literal.class);
    }
    
    @Override
    protected Comparable<?> doAdapt(Literal node) {
        Object literalValue = node.getValue();
        if (!(literalValue instanceof Comparable<?>)) {
            throw new SelectorEvaluationException("Attempted to apply #comparable-lv to non-Comparable node " + node +
                    " with literal value of type " + literalValue.getClass());
        }
        return (Comparable<?>) literalValue;
    }

}
