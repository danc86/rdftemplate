Integrating with Spring
=======================

Evaluating selector expressions
-------------------------------

You can define a :java:class:`SelectorFactory 
<au.id.djc.rdftemplate.selector.SelectorFactory>` bean in your application 
context:

.. code-block:: xml

    <bean id="selectorFactory" class="au.id.djc.rdftemplate.selector.EternallyCachingSelectorFactory">
        <constructor-arg>
            <bean class="au.id.djc.rdftemplate.selector.AntlrSelectorFactory">
                <property name="adaptationFactory">
                    <bean class="com.example.MyAdaptationFactory" />
                </property>
                <property name="predicateResolver">
                    <bean class="com.example.MyPredicateResolver" />
                </property>
                <property name="namespacePrefixMap">
                    <bean class="com.example.MyNamespacePrefixMapper" />
                </property>
            </bean>
        </constructor-arg>
    </bean>

Rendering templates
-------------------

Similarly, a :java:class:`TemplateInterpolator` bean can be defined:

.. code-block:: xml

    <bean class="au.id.djc.rdftemplate.TemplateInterpolator">
        <constructor-arg ref="selectorFactory" />
    </bean>

Views in Spring Web MVC
-----------------------

.. todo:: RDFTemplateViewResolver
