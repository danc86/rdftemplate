package au.id.djc.rdftemplate.selector;

import java.util.Arrays;

import com.hp.hpl.jena.rdf.model.RDFNode;

public abstract class AbstractAdaptation<DestType, NodeType extends RDFNode> implements Adaptation<DestType> {
    
    private final Class<DestType> destinationType;
    private final Class<?>[] argTypes;
    private final Class<NodeType> nodeType;
    
    protected AbstractAdaptation(Class<DestType> destinationType, Class<?>[] argTypes, Class<NodeType> nodeType) {
        this.destinationType = destinationType;
        this.argTypes = argTypes;
        this.nodeType = nodeType;
    }
    
    @Override
    public Class<DestType> getDestinationType() {
        return destinationType;
    }
    
    @Override
    public Class<?>[] getArgTypes() {
        return argTypes;
    }
    
    @Override
    public void setArgs(Object[] args) {
        if (args.length != argTypes.length)
            throw new SelectorEvaluationException("Expected args of types " + Arrays.toString(argTypes) +
                    " but invoked with " + Arrays.toString(args));
        for (int i = 0; i < args.length; i ++) {
            if (!argTypes[i].isAssignableFrom(args[i].getClass()))
                throw new SelectorEvaluationException("Arg " + i + ": expected type " + argTypes[i] +
                        " but was " + args[i].getClass());
        }
        setCheckedArgs(args);
    }
    
    protected void setCheckedArgs(Object[] args) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public DestType adapt(RDFNode node) {
        if (!nodeType.equals(RDFNode.class)) {
            if (!node.canAs(nodeType))
                throw new SelectorEvaluationException("Adaptation can only be applied to " + nodeType +
                        " but was applied to " + node);
        }
        return doAdapt(node.as(nodeType));
    }
    
    protected abstract DestType doAdapt(NodeType node);

}
