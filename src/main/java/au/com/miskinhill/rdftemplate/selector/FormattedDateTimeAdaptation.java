package au.com.miskinhill.rdftemplate.selector;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.hp.hpl.jena.rdf.model.Literal;

public class FormattedDateTimeAdaptation extends AbstractAdaptation<String, Literal> {
    
    private String pattern;
    private DateTimeFormatter formatter;
    
    public FormattedDateTimeAdaptation() {
        super(String.class, new Class<?>[] { String.class }, Literal.class);
    }
    
    @Override
    protected void setCheckedArgs(Object[] args) {
        this.pattern = (String) args[0];
        this.formatter = DateTimeFormat.forPattern(pattern.replace("\"", "'")); // for convenience in XML
    }

    public String getPattern() {
        return pattern;
    }
    
    @Override
    protected String doAdapt(Literal node) {
        Object lv = node.getValue();
        if (lv instanceof ReadableInstant) {
            ReadableInstant instant = (ReadableInstant) lv;
            return formatter.print(instant);
        } else if (lv instanceof ReadablePartial) {
            ReadablePartial instant = (ReadablePartial) lv;
            return formatter.print(instant);
        } else {
            throw new SelectorEvaluationException("Attempted to apply #formatted-dt to non-datetime literal " + lv);
        }
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pattern", pattern).toString();
    }

}
