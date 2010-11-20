XML templates
=============

Templates are pure XML, with special directives embedded in the document as 
attributes and elements. Substitutions are also supported inside character data 
and attribute values.

The template syntax is inspired by the `Genshi`_ templating library for Python.

.. _Genshi: http://genshi.edgewall.org/

XML namespace
-------------

The XML elements and attributes handled by rdftemplate are defined in 
a dedicated namespace:

.. code-block:: none

    http://code.miskinhill.com.au/rdftemplate/

By convention this namespace is mapped to the prefix ``rdf``. For example:

.. code-block:: xml

   <html xmlns="http://www.w3.org/1999/xhtml"
         xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
   ...
   </html>    

All declarations of the rdftemplate namespace are stripped from the resulting 
document when a template is rendered.

Substitutions in character data and attribute values
----------------------------------------------------

Templates may contain substitutions embedded in character data or in attribute 
values. Substitutions are delimited by ``${`` and ``}`` and contain 
a :doc:`selector expression <selector>`, which is evaluated with respect to the 
current context node (passed to the :java:class:`TemplateInterpolator` when 
rendering the template).

The selector expression must have exactly one result when evaluated. The type 
of the result affects how substitution is performed:

* If the selector expression evaluates to an
  `XML literal node <http://www.w3.org/TR/rdf-concepts/#section-XMLLiteral>`_ 
  or an instance of :java:class:`XMLStream <au.id.djc.rdftemplate.XMLStream>`, 
  the entire XML tree is inserted into the resulting document. (This 
  substitution is only possible in character data; if it occurs in an attribute 
  value, an exception will be thrown.)

* If the selector expression evaluates to any other type of literal node, it 
  will be converted to a Java type using Jena’s type conversion facilities, and 
  then toString() will be called on the converted object.

* If any other Java object is encountered, toString() will be called on it. 
  This means that when a selector expression evaluates to a :java:class:`String 
  <java.lang.String>`, it will be inserted as-is.

Consider the following example of a template for describing an article in HTML. 
It uses subtitutions in character data and in attribute values:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml"
          xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
        <head>
            <title>${dc:title#string-lv}</title>
            <meta name="DC.creator" content="${dc:creator#string-lv}" />
        </head>
        <body>
            <p>Title: ${dc:title}</p>
        </body>
    </html>

In this example, the object of ``dc:title`` might be an XML literal containing 
an HTML ``<span>`` element. The ``#string-lv`` :ref:`adaptation <adaptations>` 
is used to strip markup from XML literals in the ``<title>`` element and the 
``content`` attribute, where markup is not permitted. On the other hand, the 
title’s complete XML tree will be inserted into the ``<p>`` element with all 
markup preserved.

This example will also work correctly if the object of ``dc:title`` is a plain 
literal rather than an XML literal, since ``#string-lv`` will pass the plain 
literal through untouched.

Substitutions with ``rdf:content``
----------------------------------

The ``rdf:content`` attribute is used to replace an element’s content with the 
result of evaluating a selector expression. The selector expression must have 
exactly one result when evaluated. For example:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml"
          xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
        <body>
            <h1 rdf:content="dc:title" />
        </body>
    </html>

The ``rdf:content`` attribute is stripped from the resulting document. The 
substitution rules described above also apply for ``rdf:content`` (so XML 
literals will be inserted as-is into the resulting document), with one minor 
enhancement: if the selector expression evaluates to a literal node with 
a language tag, that language will be set on the enclosing element in an 
`xml:lang <http://www.w3.org/TR/REC-xml/#sec-lang-tag>`_ attribute (and also an 
XHTML ``lang`` attribute, if the document uses the XHTML namespace).

If the object of the ``dc:title`` property is the literal ``"Война и мир"@ru`` 
in the example above, then the template would be rendered as:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml">
        <body>
            <h1 xml:lang="ru" lang="ru">Война и мир</h1>
        </body>
    </html>

Note that if the element has any content in the template, this will be 
discarded when rendering.

Repetition with ``rdf:for``
---------------------------

The ``rdf:for`` attribute can be used to loop over zero or more results from 
a selector expression. For each node in the results, the element and its 
subtree will be rendered using that node as the context node. All template 
constructs may be nested inside the subtree, including other ``rdf:for`` loops.

For example, given the following RDF graph:

.. include:: example-graph.rst.inc

the following template could be used to produce an HTML listing of the people 
who are known to a particular person:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml"
          xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
        <body>
            <h1>${foaf:name}’s buddies</h1>
            <ul>
                <li rdf:for="foaf:knows(foaf:name)">
                    <a href="#uri" rdf:content="foaf:name" />
                </li>
            </ul>
        </body>
    </html>

If ``<bob>`` is used as the context node, the ``foaf:knows(foaf:name)`` 
expression will select two nodes: ``<alice>`` and ``<carol>``, in that order. 
The ``<li>`` element will therefore be rendered twice, the first time with its 
context node set to ``<alice>``, the second time set to ``<carol>``. The 
resulting XML would be:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml">
        <body>
            <h1>Bob’s buddies</h1>
            <ul>
                <li><a href="alice">Alice</a></li>
                <li><a href="carol">Carol</a></li>
            </ul>
        </body>
    </html>

``rdf:for`` can also be given as an XML element, with its selector expression 
in an attribute named ``each``. This will be rendered in the same way, except 
that the entire ``rdf:for`` element is stripped out. In the example above, 
``<li rdf:for="foaf:knows(foaf:name)">...</li>`` is equivalent to ``<rdf:for 
each="foaf:knows(foaf:name)"><li>...</li></rdf:for>``.

Because of the potential for ambiguity, it is illegal to combine ``rdf:for`` 
with other directives on the same element.

Concatenation with ``rdf:join``
-------------------------------

The ``rdf:join`` element behaves in the same way as the ``rdf:for`` element, 
but it also accepts a ``separator`` attribute, which specifies a string to be 
inserted between each repetition.

For example, here is a briefer version of the template above:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml"
          xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
        <body>
            <p>${foaf:name}’s buddies:
            <rdf:join each="foaf:knows(foaf:name)" separator=", ">
                <a href="#uri" rdf:content="foaf:name" />
            </rdf:join>
            </p>
        </body>
    </html>

Conditionals with ``rdf:if``
----------------------------

The ``rdf:if`` attribute will cause an element and its subtree to be included 
in the resulting document only if the selector expression evaluates to *one or 
more* items. Use a :ref:`predicate <predicates>` in the selector expression to 
express complex conditions. For example:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml"
          xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
        <body>
            <p rdf:if="dc:identifier[uri-prefix='urn:issn:']">
                ISSN: ${dc:identifier#uri-slice(9)}
            </p>
        </body>
    </html>

When rendering this template, the ``<p>`` element will only be included if 
there is some object of the ``dc:identifier`` property which satisfies the 
``uri-prefix='urn:issn:'`` predicate.

There is also an element form of ``rdf:if``. Use the ``test`` attribute to 
specify the selector expression to test against, or the ``not`` attribute to 
apply an inverse test.

A pair of ``rdf:if`` elements can be used to make a choice between two 
alternatives. For example:

.. code-block:: xml

    <html xmlns="http://www.w3.org/1999/xhtml"
          xmlns:rdf="http://code.miskinhill.com.au/rdftemplate/">
        <body>
            <p>
                <rdf:if test="ex:thumbnail"><img src="${ex:thumbnail#uri}" /></rdf:if>
                <rdf:if not="ex:thumbnail">No thumbnail available :-(</rdf:if>
            </p>
        </body>
    </html>

Rendering templates
-------------------

.. java:class:: au.id.djc.rdftemplate.TemplateInterpolator

   Use this class to render templates. Instances of this class can safely be 
   shared across threads (for example, as singleton beans in Spring).

   .. java:method:: TemplateInterpolator(au.id.djc.rdftemplate.selector.SelectorFactory selectorFactory)

      The given :java:class:`SelectorFactory 
      <au.id.djc.rdftemplate.selector.SelectorFactory>` will be used to 
      evaluate selector expressions when rendering templates.

   .. java:method:: java.lang.String interpolate(java.io.Reader reader, com.hp.hpl.jena.rdf.model.RDFNode node)

      Reads a template from the given :java:class:`Reader <java.io.Reader>`, 
      and renders it using the given node as context. The resulting XML 
      document is returned.

      A number of overrides of this method also exist for advanced use cases. 
      Refer to the `Javadoc for TemplateInterpolator 
      <http://code.djc.id.au/rdftemplate/javadoc/latest/au/id/djc/rdftemplate/TemplateInterpolator.html>`_ 
      for more details.
