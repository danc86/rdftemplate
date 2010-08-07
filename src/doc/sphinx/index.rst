rdftemplate
===========

Rdftemplate is a library for generating XML documents from RDF data using 
templates.

The library supports evaluation of “:doc:`selector expressions <selector>`”, which use an 
XPath-inspired syntax for selecting an ordered set of nodes from an arbitrary 
starting context node.

The library also supports rendering XML from :doc:`templates <xml-template>`. The template 
interpolator recognises a number of Genshi-inspired template directives, which 
are used to insert the result of a selector expression into the generated 
XML.

The library uses the Jena RDF model API.
It also includes optional support for :doc:`integrating with Spring 
<spring>`, allowing templates to be used as Spring Web MVC views.

Rdftemplate was developed for the `Miskin Hill`_ web site, where it is used to 
generate output in various XML-based formats. You can view the `templates used 
for Miskin Hill <http://code.miskinhill.com.au/hg/miskinhill-master/file/tip/web/src/main/resources/au/com/miskinhill/rdf/template/>`_ 
to see some examples of how rdftemplate works.

.. _Miskin Hill: http://miskinhill.com.au/

Development
-----------

* `Javadoc <http://code.djc.id.au/rdftemplate/javadoc/latest/>`_
* `Mercurial repository <http://code.djc.id.au/hg/rdftemplate/>`_
* `Hudson build <http://hudson.miskinhill.com.au/job/rdftemplate/>`_
* Send bugs and suggestions to `Dan C <mailto:djc@djc.id.au>`_

Quick start with Maven
----------------------

Add the following to your pom.xml:

.. code-block:: xml

    <repositories>
        <repository>
            <id>code.djc.id.au</id>
            <url>http://code.djc.id.au/maven2/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>au.id.djc</groupId>
            <artifactId>rdftemplate</artifactId>
            <version>1.2</version>
        </dependency>
    </dependencies>

.. todo:: more examples, like calling TemplateInterpolator or using Spring views
