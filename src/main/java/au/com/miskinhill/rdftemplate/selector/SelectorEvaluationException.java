package au.com.miskinhill.rdftemplate.selector;

public class SelectorEvaluationException extends RuntimeException {

    private static final long serialVersionUID = -398277800899471326L;
    
    public SelectorEvaluationException(String message) {
        super(message);
    }
    
    public SelectorEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

}
