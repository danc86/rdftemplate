package au.id.djc.rdftemplate.selector;

public interface PredicateResolver {
    
    Class<? extends Predicate> getByName(String name);

}
