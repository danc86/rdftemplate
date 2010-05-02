package au.com.miskinhill.rdftemplate.selector;

public interface AdaptationFactory {
    
    boolean hasName(String name);
    
    Adaptation<?> getByName(String name);

}
