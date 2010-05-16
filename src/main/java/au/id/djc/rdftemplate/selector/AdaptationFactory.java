package au.id.djc.rdftemplate.selector;

public interface AdaptationFactory {
    
    boolean hasName(String name);
    
    Adaptation<?> getByName(String name);

}
