package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Resource;

public class UriSliceAdaptation extends AbstractAdaptation<String, Resource> {
    
    private Integer startIndex;
    
    public UriSliceAdaptation() {
        super(String.class, new Class<?>[] { Integer.class }, Resource.class);
    }
    
    public Integer getStartIndex() {
        return startIndex;
    }
    
    @Override
    protected void setCheckedArgs(Object[] args) {
        this.startIndex = (Integer) args[0];
    }
    
    @Override
    protected String doAdapt(Resource node) {
        String uri = node.getURI();
        return uri.substring(startIndex);
    }

}
