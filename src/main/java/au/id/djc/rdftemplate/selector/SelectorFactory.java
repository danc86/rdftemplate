package au.id.djc.rdftemplate.selector;

public interface SelectorFactory {
    
    Selector<?> get(String expression);

}
