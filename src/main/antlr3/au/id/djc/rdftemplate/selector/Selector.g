grammar Selector;

@parser::header {
package au.id.djc.rdftemplate.selector;
import java.util.Map;
}

@parser::members {
    
    @Override
    public void reportError(RecognitionException e) {
        throw new InvalidSelectorSyntaxException(e);
    }
    
    private AdaptationFactory adaptationFactory;
    private PredicateResolver predicateResolver;
    private Map<String, String> namespacePrefixMap;
    
    public void setAdaptationFactory(AdaptationFactory adaptationFactory) {
        this.adaptationFactory = adaptationFactory;
    }
    
    public void setPredicateResolver(PredicateResolver predicateResolver) {
        this.predicateResolver = predicateResolver;
    }
    
    public void setNamespacePrefixMap(Map<String, String> map) {
        this.namespacePrefixMap = map;
    }
    
    private String ns(String prefix) {
        String ns = namespacePrefixMap.get(prefix);
        if (ns == null)
            throw new InvalidSelectorSyntaxException("Unbound namespace prefix " + prefix);
        return ns;
    }
    
}

@lexer::header {
package au.id.djc.rdftemplate.selector;
}

@lexer::members {
    @Override
    public void reportError(RecognitionException e) {
        throw new InvalidSelectorSyntaxException(e);
    }
}

start : unionSelector ;

unionSelector returns [Selector<?> result]
@init {
    List<Selector<?>> selectors = new ArrayList<Selector<?>>();
}
    : WHITESPACE*
      s=selector { selectors.add(s); }
      ( WHITESPACE*
        '|'
        WHITESPACE*
        s=selector { selectors.add(s); }
      )*
      {
        if (selectors.size() > 1)
            result = new UnionSelector(selectors);
        else
            result = selectors.get(0);
      }
    ;

selector returns [Selector<?> result]
@init {
    Adaptation<?> adaptation = null;
}
    : ( ts=traversingSelector { result = ts; }
      | { result = new NoopSelector(); }
      )
      ( '#'
        adaptationName=XMLTOKEN
            {
                adaptation = adaptationFactory.getByName($adaptationName.text);
            }
        ( '('
          ( startIndex=INTEGER {
                                adaptation.setArgs(new Object[] { Integer.parseInt($startIndex.text) });
                             }
          | sq=SINGLE_QUOTED {
                                adaptation.setArgs(new Object[] { $sq.text });
                             }
          )
          ')'
        | 
        )
        { $result = new SelectorWithAdaptation(result, adaptation); }
      |
      )
      WHITESPACE*
    ;

traversingSelector returns [TraversingSelector result]
@init {
    result = new TraversingSelector();
}
    : t=traversal { $result.addTraversal(t); }
      ( WHITESPACE*
        '/'
        WHITESPACE*
        t=traversal { $result.addTraversal(t); }
      ) *
    ;
    
traversal returns [Traversal result]
@init {
    result = new Traversal();
}
    : ( '!' { $result.setInverse(true); }
        WHITESPACE*
      | // optional
      )
      nsprefix=XMLTOKEN { $result.setPropertyNamespace(ns($nsprefix.text)); }
      ':'
      localname=XMLTOKEN { $result.setPropertyLocalName($localname.text); }
      ( '['
        WHITESPACE*
        p=booleanPredicate { $result.setPredicate(p); }
        WHITESPACE*
        ']'
      | // optional
      )
      ( '('
        WHITESPACE*
        so=sortOrder { $result.addSortOrderComparator(so); }
        ( WHITESPACE*
          ','
          WHITESPACE*
          so=sortOrder { $result.addSortOrderComparator(so); }
        )*
        WHITESPACE*
        ')'
      | // optional
      )
      ( '['
        WHITESPACE*
        subscript=INTEGER { $result.setSubscript(Integer.parseInt($subscript.text)); }
        WHITESPACE*
        ']'
      | // optional
      )
    ;

sortOrder returns [SelectorComparator<? extends Comparable<?>> result]
@init {
    result = new SelectorComparator();
}
    : ( '~' { $result.setReversed(true); }
        WHITESPACE*
      | // optional
      )
      s=selector { $result.setSelector((Selector) s.withResultType(Comparable.class)); }
    ;

booleanPredicate returns [Predicate result]
    : ( p=predicate { result = p; }
      | left=predicate
        WHITESPACE+
        'and'
        WHITESPACE+
        right=booleanPredicate
        { result = new BooleanAndPredicate(left, right); }
      )
    ;
    
predicate returns [Predicate result]
@init {
    Class<? extends Predicate> predicateClass;
}
    : predicateName=XMLTOKEN
            {
                predicateClass = predicateResolver.getByName($predicateName.text);
                if (predicateClass == null)
                    throw new InvalidSelectorSyntaxException("No predicate named " + $predicateName.text);
            }
      WHITESPACE*
      '='
      WHITESPACE*
      ( sq=SINGLE_QUOTED {
                try {
                    result = predicateClass.getConstructor(String.class).newInstance($sq.text);
                } catch (Exception e) {
                    throw new InvalidSelectorSyntaxException(e);
                }
             }
      | nsprefix=XMLTOKEN
        ':'
        localname=XMLTOKEN
        {
            try {
                result = predicateClass.getConstructor(String.class, String.class).newInstance(ns($nsprefix.text), $localname.text);
            } catch (Exception e) {
                throw new InvalidSelectorSyntaxException(e);
            }
        }
      )
    ;

XMLTOKEN : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')* ;
INTEGER : ('0'..'9')+ ;
SINGLE_QUOTED : '\'' ( options {greedy=false;} : . )* '\''
    {
        // strip quotes
        String txt = getText();
        setText(txt.substring(1, txt.length() -1));
    };
WHITESPACE : (' '|'\n'|'\r'|'\t') ;