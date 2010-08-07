Selector expressions
====================

Selector expressions are a concise way of selecting some set of nodes in an RDF 
graph, given a particular starting node. For example, given the graph:

.. include:: example-graph.rst.inc

evaluating the selector expression ``foaf:knows/foaf:name`` with a starting node of 
``<bob>`` would yield two literal nodes: ``"Alice"`` and ``"Carol"``.

The syntax for selector expressions was inspired by `XPath`_ syntax, so if you 
are familiar with XPath you will notice many similarities.

.. todo:: make graphviz output prettier

.. _XPath: http://www.w3.org/TR/xpath/

Syntax
------

Traversing
~~~~~~~~~~

At the heart of selector expressions is the notion of traversing a path through 
the RDF graph along properties, in the same way XPath can traverse a document 
tree. Selector expressions are always evaluated with respect to a context node 
in the graph, which is the starting point for traversals.

The simplest traversal consists of a single RDF property, such as 
``foaf:knows``. This expression selects all nodes which are the object of 
a foaf:knows predicate where the starting node is the subject. In other words, 
it can be considered equivalent to the SPARQL query:

.. code-block:: none

   SELECT ?o
   WHERE { ?start foaf:knows ?o . }

(Actually the *simplest* traversal is the empty string, which always evaluates 
to the starting node. This is really only meaningful when used with other 
syntax elements described below.)

If we used ``<bob>`` in the example graph above as our starting node, the 
expression ``foaf:knows`` would evaluate to two resource nodes: ``<alice>`` and 
``<carol>``. In general a selector expression may yield zero or more results. 
For example, if we used ``<alice>`` as a starting node, the result would be 
empty.

Multiple traversals may be chained together using ``/`` as a separator, as in 
``foaf:knows/foaf:name``.

Note that property URIs are always given in their prefixed form. In order to 
keep the syntax simple, there is no way to specify a complete URI reference in 
a selector expression.

Inverse traversal
~~~~~~~~~~~~~~~~~

The direction of a property traversal can be inverted by prepending ``!`` to 
the property name. For example, given some article as a starting node, the 
expression ``dc:creator/!dc:creator/dc:title`` might be used to select the 
title of all articles written by the authors of the starting node.

.. _predicates:

Predicates
~~~~~~~~~~

The set of nodes resulting from a traversal can be filtered with a predicate. 
The predicate is given in square brackets (``[]``) following the property name. 
Predicates may appear at any point in the chain of traversals.

The following predicates are supported:

``type``
    Includes only nodes of the given type. Use it like this: 
    ``!dc:creator[type=bibo:Article]``.

``uri-prefix``
    Includes only resource nodes whose URI begins with the given string. Use it 
    like this: ``dc:identifier[uri-prefix='urn:issn:']``.

Multiple predicates may be applied by joining them together with the ``and`` 
keyword, as in ``!dc:creator[type=bibo:Article and uri-prefix='http://example.com/']``.

Custom predicates may be defined at runtime by supplying a custom 
:java:class:`PredicateResolver` implementation.

.. _adaptations:

Adapting the result
~~~~~~~~~~~~~~~~~~~

The result of evaluating a traversal is zero or more RDF nodes (in Java, 
implementations of Jena’s :java:class:`RDFNode 
<com.hp.hpl.jena.rdf.model.RDFNode>` interface). However, it is often necessary 
to convert these RDF nodes into a more useful data type, or to perform some 
post-processing on them.

An adaptation is a function which takes an RDF node and “adapts” it in some 
way. An adaptation can be specified at the end of a selector expression, 
preceded by ``#`` and optionally followed by an argument list. For example, the 
expression ``foaf:knows#uri`` would evaluate to the URIs of the people known to 
the starting node. The distinction here is important: whereas ``foaf:knows`` 
evaluates to zero or more :java:class:`RDFNodes 
<com.hp.hpl.jena.rdf.model.RDFNode>`, ``foaf:knows#uri`` evaluates to zero or 
more :java:class:`Strings <java.lang.String>` giving the URI of each node.

The following adaptations are supported:

``uri``
    Returns the URI of the RDF node as a :java:class:`String 
    <java.lang.String>`. Throws an exception if applied to a node which is not 
    a resource.

``uri-slice``
    Returns a substring of the URI. This adaptation takes a single integer 
    argument specifying the number of characters to be removed. Use it like 
    this: ``dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)``.

``uri-anchor``
    Returns the anchor part of the URI, excluding the # character. Returns 
    empty string if there is no anchor part.

``lv``
    Short for “literal value”. Returns the value of the literal RDF node, 
    converted to a Java object using Jena’s type conversion facilities (see 
    :java:method:`Literal#getValue() 
    <com.hp.hpl.jena.rdf.model.Literal#getValue()>`). Throws an exception if 
    applied to a node which is not a literal.

``comparable-lv``
    Essentially the same as ``lv``, but with a runtime check to ensure the 
    literal value implements :java:class:`Comparable <java.lang.Comparable>`. 
    Only exists for type-safety reasons.

``string-lv``
    Like ``lv``, but additionally calls toString() on the resulting object to 
    ensure it is always a String. This adaptation also strips all tags from XML 
    literals.

``formatted-dt``
    Short for “formatted date-time”. This adaptation can only be applied to 
    literal nodes whose values are represented as Joda datetime types. It takes 
    a single string argument, specifying the date-time format to apply. Use it 
    like this: ``dc:created#formatted-dt('d MMMM yyyy')``.

    .. todo:: hacks for Joda are not in stock Jena

Custom adaptations may be defined at runtime by supplying a custom 
:java:class:`AdaptationFactory` implementation.

Sorting the result
~~~~~~~~~~~~~~~~~~

RDF graphs by their nature do not define any ordering, so a selector expression 
like ``foaf:knows`` will return its results in arbitrary order. When we expect 
the result to contain more than one node, it is often useful to ensure 
a predictable (repeatable) ordering of the resulting nodes.

Sorting can be applied at any point in the chain of traversals, by giving 
a sort expression enclosed in parentheses (``()``). The sort expression can be 
a complete selector expression (including multiple traversals, nested sorts, 
and any other selector features). The set of nodes in the traversal are then 
sorted by evaluating the sort expression for each node, and sorting with these 
values as keys. The sort expression may optionally be prepended with ``~`` to 
indicate a reverse sort.

For example, given an author as a starting node, 
``!dc:creator(dc:title#comparable-lv)`` would evaluate to the works created by 
that author, ordered by the title of each work.

Note that the sort expression must always evaluate to a Java object which 
implements :java:class:`Comparable <java.lang.Comparable>`, so it is typically 
necessary to apply the ``comparable-lv`` adaptation in the sort expression.

If one expression is not enough to uniquely sort each item in the result, 
multiple sort expressions can be specified using ``,`` to separate them.

Selecting from many results
~~~~~~~~~~~~~~~~~~~~~~~~~~~

A sort expression may optionally be followed by a subscript ``[n]``, indicating 
that only the *n*-th node in the result should be selected. For example, 
``!dc:creator(~dc:date)[0]/dc:title`` might be used to select the title of an 
author’s most recent work.

Combining multiple expressions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Selector expressions can be chained together using ``|``. The result of the 
expression will be the result of each sub-expression chained together in 
sequence. For example: ``!dc:creator | !bibo:translator``.

Evaluating expressions
----------------------

The following classes in the au.id.djc.rdftemplate.selector package are 
relevant for compiling and evaluating selector expressions:

.. java:class:: au.id.djc.rdftemplate.selector.Selector<T>

   This interface represents the compiled version of a selector expression. It 
   is parametrised on the result type of the expression.

   .. java:method:: java.lang.Class<T> getResultType()

      Returns the result type of this selector expression. (This is the runtime 
      class of the type parameter T.) For a simple traversal this will be 
      :java:class:`RDFNode <com.hp.hpl.jena.rdf.model.RDFNode>`, or if an 
      adaptation is applied to the selector expression it will be the result 
      type of the adaptation (such as :java:class:`String <java.lang.String>` 
      or :java:class:`Object <java.lang.Object>`).

   .. java:method:: Selector<Other> withResultType(java.lang.Class<Other> otherType)

      A convenience method to cast the type parameter of this Selector. Always 
      returns this instance. Just a dumb hack to keep Java’s static type 
      checking happy.

   .. java:method:: java.util.List<T> result(com.hp.hpl.jena.rdf.model.RDFNode node)

      Evaluates this selector expression with respect to the given starting 
      node, and returns the result.

   .. java:method:: T singleResult(com.hp.hpl.jena.rdf.model.RDFNode node)

      Evaluates this selector expression with respect to the given starting 
      node, and returns the result. If the selector does not evaluate to 
      exactly one node, an exception is thrown.

.. java:class:: au.id.djc.rdftemplate.selector.AntlrSelectorFactory

   Use this class to compile selector expressions into :java:class:`Selector` 
   instances. Instances of this class can safely be shared across threads (for 
   example, as singleton beans in Spring).

   .. java:method:: au.id.djc.rdftemplate.selector.Selector<?> get(java.lang.String expression)

      Compiles the given selector expression into a :java:class:`Selector` 
      instance.

      .. code-block:: java

         Selector<RDFNode> s1 = factory.get("foaf:knows").withResultType(RDFNode.class);
         Selector<String> s2 = factory.get("foaf:knows/foaf:name#string-lv").withResultType(String.class);

   .. java:method:: void setAdaptationFactory(au.id.djc.rdftemplate.selector.AdaptationFactory adaptationFactory)

      Configures a custom :java:class:`AdaptationFactory` implementation for 
      selectors created by this factory. If this setter is not called, an 
      instance of :java:class:`DefaultAdaptationFactory 
      <au.id.djc.rdftemplate.selector.DefaultAdaptationFactory>` will be used.

   .. java:method:: void setPredicateResolver(au.id.djc.rdftemplate.selector.PredicateResolver predicateResolver)

      Configures a custom :java:class:`PredicateResolver` implementation for 
      selectors created by this factory. If this setter is not called, an 
      instance of :java:class:`DefaultPredicateResolver 
      <au.id.djc.rdftemplate.selector.DefaultPredicateResolver>` will be used.

   .. java:method:: void setNamespacePrefixMap(java.util.Map<String, String> namespacePrefixMap)

      Configure namespace prefix mappings for selectors created by this 
      factory. If this setter is not called, no namespace prefixes will be 
      defined.

.. java:class:: au.id.djc.rdftemplate.selector.AdaptationFactory

   Implement this interface if you would like to use custom adaptations in your 
   selector expressions.

   Your implementation should fall back to 
   a :java:class:`DefaultAdaptationFactory 
   <au.id.djc.rdftemplate.selector.DefaultAdaptationFactory>` instance, so that 
   selector expressions have access to the builtin adaptations in addition to 
   your custom ones.

.. java:class:: au.id.djc.rdftemplate.selector.PredicateResolver

   Implement this interface if you would like to use custom predicates in your 
   selector expressions.

   Your implementation should fall back to 
   a :java:class:`DefaultPredicateResolver 
   <au.id.djc.rdftemplate.selector.DefaultPredicateResolver>` instance, so that 
   selector expressions have access to the builtin predicates in addition to 
   your custom ones.

.. java:class:: au.id.djc.rdftemplate.selector.EternallyCachingSelectorFactory

   Wrap an :java:class:`AntlrSelectorFactory` with this class if you want to 
   avoid compiling selectors anew every time. Do not use this class if the 
   number of different selector expressions is unbounded, as it will cause heap 
   exhaustion.
