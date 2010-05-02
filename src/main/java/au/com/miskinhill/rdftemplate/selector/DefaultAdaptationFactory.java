package au.com.miskinhill.rdftemplate.selector;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;

public class DefaultAdaptationFactory implements AdaptationFactory {
    
    private static final Map<String, Class<? extends Adaptation<?>>> ADAPTATIONS = new HashMap<String, Class<? extends Adaptation<?>>>();
    static {
        ADAPTATIONS.put("uri", UriAdaptation.class);
        ADAPTATIONS.put("uri-slice", UriSliceAdaptation.class);
        ADAPTATIONS.put("uri-anchor", UriAnchorAdaptation.class);
        ADAPTATIONS.put("lv", LiteralValueAdaptation.class);
        ADAPTATIONS.put("comparable-lv", ComparableLiteralValueAdaptation.class);
        ADAPTATIONS.put("string-lv", StringLiteralValueAdaptation.class);
        ADAPTATIONS.put("formatted-dt", FormattedDateTimeAdaptation.class);
    }
    
    @Override
    public boolean hasName(String name) {
        return ADAPTATIONS.containsKey(name);
    }
    
    @Override
    public Adaptation<?> getByName(String name) {
        Class<? extends Adaptation<?>> adaptationClass = ADAPTATIONS.get(name);
        if (adaptationClass == null) {
            throw new InvalidSelectorSyntaxException("No adaptation named " + name);
        }
        return BeanUtils.instantiate(adaptationClass);
    }

}
