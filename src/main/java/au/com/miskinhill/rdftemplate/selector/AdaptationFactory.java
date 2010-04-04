package au.com.miskinhill.rdftemplate.selector;

public interface AdaptationFactory {
    
    Adaptation<?> getByName(String name);

}
