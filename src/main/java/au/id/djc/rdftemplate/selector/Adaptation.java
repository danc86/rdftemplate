package au.id.djc.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;

public interface Adaptation<T> {

    Class<T> getDestinationType();
    
    Class<?>[] getArgTypes();
    
    void setArgs(Object[] args);
    
    T adapt(RDFNode node);

}
