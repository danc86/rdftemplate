package au.id.djc.rdftemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import au.id.djc.rdftemplate.html.XHTMLEventConsumer;
import au.id.djc.rdftemplate.selector.InvalidSelectorSyntaxException;
import au.id.djc.rdftemplate.selector.Selector;
import au.id.djc.rdftemplate.selector.SelectorFactory;

public class TemplateInterpolator {
    
    public static final String NS = "http://code.miskinhill.com.au/rdftemplate/";
    
    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final SelectorFactory selectorFactory;
    private final boolean htmlCompatible;
    
    public TemplateInterpolator(SelectorFactory selectorFactory) {
        this(selectorFactory, false);
    }
    
    public TemplateInterpolator(SelectorFactory selectorFactory, boolean htmlCompatible) {
        this.selectorFactory = selectorFactory;
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        this.htmlCompatible = htmlCompatible;
    }
    
    public boolean isHtmlCompatible() {
        return htmlCompatible;
    }
    
    public String interpolate(Reader reader, RDFNode node) {
        try {
            StringWriter writer = new StringWriter();
            final XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(writer);
            XMLEventConsumer destination = new XMLEventConsumer() {
                @Override
                public void add(XMLEvent event) throws XMLStreamException {
                    eventWriter.add(event);
                }
            };
            interpolate(reader, node, destination);
            return writer.toString();
        } catch (XMLStreamException e) {
            throw new TemplateSyntaxException(e);            
        }
    }
    
    @SuppressWarnings("unchecked")
    public void interpolate(Reader reader, RDFNode node, XMLEventConsumer writer) {
        try {
            interpolate(inputFactory.createXMLEventReader(reader), node, writer);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void interpolate(InputStream inputStream, RDFNode node, Writer writer) {
        try {
            final XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(writer);
            XMLEventConsumer destination = new XMLEventConsumer() {
                @Override
                public void add(XMLEvent event) throws XMLStreamException {
                    eventWriter.add(event);
                }
            };
            interpolate(inputFactory.createXMLEventReader(inputStream), node, destination);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void interpolate(Reader reader, RDFNode node, final Collection<XMLEvent> destination) {
        interpolate(reader, node, new XMLEventConsumer() {
            @Override
            public void add(XMLEvent event) {
                destination.add(event);
            }
        });
    }
    
    public void interpolate(Iterator<XMLEvent> reader, RDFNode node, XMLEventConsumer writer)
            throws XMLStreamException {
        if (htmlCompatible)
            writer = new XHTMLEventConsumer(writer);
        while (reader.hasNext()) {
            XMLEvent event = reader.next();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT: {
                    StartElement start = (StartElement) event;
                    if (start.getName().equals(IfAction.ACTION_QNAME)) {
                        Attribute testAttribute = start.getAttributeByName(new QName("test"));
                        Attribute notAttribute = start.getAttributeByName(new QName("not"));
                        String condition;
                        boolean negate = false;
                        if (testAttribute != null && notAttribute != null)
                            throw new TemplateSyntaxException(start.getLocation(), "test and not attribute on rdf:if are mutually exclusive");
                        else if (testAttribute != null)
                            condition = testAttribute.getValue();
                        else if (notAttribute != null) {
                            condition = notAttribute.getValue();
                            negate = true;
                        } else
                            throw new TemplateSyntaxException(start.getLocation(), "rdf:if must have a test attribute or a not attribute");
                        Selector<?> conditionSelector;
                        try {
                            conditionSelector = selectorFactory.get(condition);
                        } catch (InvalidSelectorSyntaxException e) {
                            throw new TemplateSyntaxException(start.getLocation(), e);
                        }
                        List<XMLEvent> tree = consumeTree(start, reader);
                        // discard enclosing rdf:if
                        tree.remove(tree.size() - 1);
                        tree.remove(0);
                        IfAction action = new IfAction(tree, conditionSelector, negate);
                        try {
                            action.evaluate(this, node, writer);
                        } catch (Exception e) {
                            throw new TemplateInterpolationException(start.getLocation(), action, node, e);
                        }
                    } else if (start.getName().equals(JoinAction.ACTION_QNAME)) {
                        Attribute eachAttribute = start.getAttributeByName(new QName("each"));
                        if (eachAttribute == null)
                            throw new TemplateSyntaxException(start.getLocation(), "rdf:join must have an each attribute");
                        String separator = "";
                        Attribute separatorAttribute = start.getAttributeByName(new QName("separator"));
                        if (separatorAttribute != null)
                            separator = separatorAttribute.getValue();
                        Selector<RDFNode> selector;
                        try {
                            selector = selectorFactory.get(eachAttribute.getValue()).withResultType(RDFNode.class);
                        } catch (InvalidSelectorSyntaxException e) {
                            throw new TemplateSyntaxException(start.getLocation(), e);
                        }
                        List<XMLEvent> events = consumeTree(start, reader);
                        // discard enclosing rdf:join
                        events.remove(events.size() - 1);
                        events.remove(0);
                        JoinAction action = new JoinAction(events, selector, separator);
                        try {
                            action.evaluate(this, node, writer, eventFactory);
                        } catch (Exception e) {
                            throw new TemplateInterpolationException(start.getLocation(), action, node, e);
                        }
                    } else if (start.getName().equals(ForAction.ACTION_QNAME)) {
                        Attribute eachAttribute = start.getAttributeByName(new QName("each"));
                        if (eachAttribute == null)
                            throw new TemplateSyntaxException(start.getLocation(), "rdf:for must have an each attribute");
                        Selector<RDFNode> selector;
                        try {
                            selector = selectorFactory.get(eachAttribute.getValue()).withResultType(RDFNode.class);
                        } catch (InvalidSelectorSyntaxException e) {
                            throw new TemplateSyntaxException(start.getLocation(), e);
                        }
                        List<XMLEvent> events = consumeTree(start, reader);
                        // discard enclosing rdf:for
                        events.remove(events.size() - 1);
                        events.remove(0);
                        ForAction action = new ForAction(events, selector);
                        try {
                            action.evaluate(this, node, writer);
                        } catch (Exception e) {
                            throw new TemplateInterpolationException(start.getLocation(), action, node, e);
                        }
                    } else {
                        Attribute ifAttribute = start.getAttributeByName(IfAction.ACTION_QNAME);
                        Attribute contentAttribute = start.getAttributeByName(ContentAction.ACTION_QNAME);
                        Attribute forAttribute = start.getAttributeByName(ForAction.ACTION_QNAME);
                        if (ifAttribute != null) {
                            Selector<?> selector;
                            try {
                                selector = selectorFactory.get(ifAttribute.getValue());
                            } catch (InvalidSelectorSyntaxException e) {
                                throw new TemplateSyntaxException(ifAttribute.getLocation(), e);
                            }
                            start = cloneStart(start, cloneAttributesWithout(start, IfAction.ACTION_QNAME), cloneNamespacesWithoutRdf(start));
                            IfAction action = new IfAction(consumeTree(start, reader), selector, false);
                            action.evaluate(this, node, writer);
                        } else if (contentAttribute != null && forAttribute != null) {
                            throw new TemplateSyntaxException(start.getLocation(), "rdf:for and rdf:content cannot both be present on an element");
                        } else if (contentAttribute != null) {
                            consumeTree(start, reader); // discard
                            Selector<?> selector;
                            try {
                                selector = selectorFactory.get(contentAttribute.getValue());
                            } catch (InvalidSelectorSyntaxException e) {
                                throw new TemplateSyntaxException(contentAttribute.getLocation(), e);
                            }
                            ContentAction action = new ContentAction(start, selector);
                            try {
                                action.evaluate(this, node, writer, eventFactory);
                            } catch (Exception e) {
                                throw new TemplateInterpolationException(contentAttribute.getLocation(), action, node, e);
                            }
                        } else if (forAttribute != null) {
                            Selector<RDFNode> selector;
                            try {
                                selector = selectorFactory.get(forAttribute.getValue()).withResultType(RDFNode.class);
                            } catch (InvalidSelectorSyntaxException e) {
                                throw new TemplateSyntaxException(forAttribute.getLocation(), e);
                            }
                            start = cloneStart(start, cloneAttributesWithout(start, ForAction.ACTION_QNAME), cloneNamespacesWithoutRdf(start));
                            List<XMLEvent> tree = consumeTree(start, reader);
                            ForAction action = new ForAction(tree, selector);
                            try {
                                action.evaluate(this, node, writer);
                            } catch (Exception e) {
                                throw new TemplateInterpolationException(forAttribute.getLocation(), action, node, e);
                            }
                        } else {
                            start = interpolateAttributes(start, node);
                            writer.add(start);
                        }
                    }
                    break;
                }
                case XMLStreamConstants.CHARACTERS: {
                    Characters characters = (Characters) event;
                    interpolateCharacters(writer, characters, node);
                    break;
                }
                case XMLStreamConstants.CDATA: {
                    Characters characters = (Characters) event;
                    interpolateCharacters(writer, characters, node);
                    break;
                }
                default:
                    writer.add(event);
            }
        }
    }
    
    private List<XMLEvent> consumeTree(StartElement start, Iterator<XMLEvent> reader) throws XMLStreamException {
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        events.add(start);
        Deque<QName> elementStack = new LinkedList<QName>();
        while (reader.hasNext()) {
            XMLEvent event = reader.next();
            events.add(event);
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    elementStack.addLast(((StartElement) event).getName());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (elementStack.isEmpty()) {
                        return events;
                    } else {
                        if (!elementStack.removeLast().equals(((EndElement) event).getName()))
                            throw new IllegalStateException("End element mismatch");
                    }
                    break;
                default:
            }
        }
        throw new IllegalStateException("Reader exhausted before end element found");
    }
    
    @SuppressWarnings("unchecked")
    protected StartElement interpolateAttributes(StartElement start, RDFNode node) {
        Set<Attribute> replacementAttributes = new LinkedHashSet<Attribute>();
        for (Iterator<Attribute> it = start.getAttributes(); it.hasNext(); ) {
            Attribute attribute = it.next();
            String replacementValue = attribute.getValue();
            if (!attribute.getName().getNamespaceURI().equals(NS)) { // skip rdf: attributes
                try {
                    replacementValue = interpolateString(attribute.getValue(), node);
                } catch (Exception e) {
                    throw new TemplateInterpolationException(attribute.getLocation(), attribute.getValue(), node, e);
                }
            }
            replacementAttributes.add(eventFactory.createAttribute(attribute.getName(),
                    replacementValue));
        }
        return cloneStart(start, replacementAttributes, cloneNamespacesWithoutRdf(start));
    }
    
    private StartElement cloneStart(StartElement start, Iterable<Attribute> attributes, Iterable<Namespace> namespaces) {
        return eventFactory.createStartElement(
                start.getName().getPrefix(),
                start.getName().getNamespaceURI(),
                start.getName().getLocalPart(),
                attributes.iterator(),
                namespaces.iterator(),
                start.getNamespaceContext());
    }
    
    @SuppressWarnings("unchecked")
    private Set<Namespace> cloneNamespacesWithoutRdf(StartElement start) {
        Set<Namespace> clonedNamespaces = new LinkedHashSet<Namespace>();
        for (Iterator<Namespace> it = start.getNamespaces(); it.hasNext(); ) {
            Namespace namespace = it.next();
            if (!namespace.getNamespaceURI().equals(NS))
                clonedNamespaces.add(namespace);
        }
        return clonedNamespaces;
    }
    
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");
    public String interpolateString(String template, RDFNode node) {
        if (!SUBSTITUTION_PATTERN.matcher(template).find()) {
            return template; // fast path
        }
        StringBuffer substituted = new StringBuffer();
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(template);
        while (matcher.find()) {
            String expression = matcher.group(1);
            Object replacement = selectorFactory.get(expression).singleResult(node);
            
            String replacementValue;
            if (replacement instanceof RDFNode) {
                RDFNode replacementNode = (RDFNode) replacement;
                if (replacementNode.isLiteral()) {
                    Literal replacementLiteral = (Literal) replacementNode;
                    replacementValue = replacementLiteral.getValue().toString();
                } else {
                    throw new UnsupportedOperationException("Not a literal: " + replacementNode);
                }
            } else {
                replacementValue = replacement.toString();
            }
            
            matcher.appendReplacement(substituted, replacementValue.replace("$", "\\$"));
        }
        matcher.appendTail(substituted);
        return substituted.toString();
    }
    
    private void interpolateCharacters(XMLEventConsumer writer, Characters characters, RDFNode node) throws XMLStreamException {
        String template = characters.getData();
        if (!SUBSTITUTION_PATTERN.matcher(template).find()) {
            writer.add(characters); // fast path
            return;
        }
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(template);
        int lastAppendedPos = 0;
        while (matcher.find()) {
            writer.add(eventFactory.createCharacters(template.substring(lastAppendedPos, matcher.start())));
            lastAppendedPos = matcher.end();
            String expression = matcher.group(1);
            Selector<?> selector;
            try {
                selector = selectorFactory.get(expression);
            } catch (InvalidSelectorSyntaxException e) {
                throw new TemplateSyntaxException(characters.getLocation(), e);
            }
            try {
                Object replacement = selector.singleResult(node);
                writeTreeForContent(writer, replacement);
            } catch (Exception e) {
                throw new TemplateInterpolationException(characters.getLocation(), expression, node, e);
            }
        }
        writer.add(eventFactory.createCharacters(template.substring(lastAppendedPos)));
    }
    
    protected void writeTreeForContent(XMLEventConsumer writer, Object replacement)
            throws XMLStreamException {
        if (replacement instanceof RDFNode) {
            RDFNode replacementNode = (RDFNode) replacement;
            if (replacementNode.isLiteral()) {
                Literal literal = (Literal) replacementNode;
                if (literal.isWellFormedXML()) {
                    writeXMLLiteral(literal.getLexicalForm(), writer);
                } else {
                    writer.add(eventFactory.createCharacters(literal.getValue().toString()));
                }
            } else {
                throw new UnsupportedOperationException("Not a literal: " + replacementNode);
            }
        } else if (replacement instanceof XMLStream) {
            for (XMLEvent event: (XMLStream) replacement) {
                writer.add(event);
            }
        } else {
            writer.add(eventFactory.createCharacters(replacement.toString()));
        }
    }

    @SuppressWarnings("unchecked")
    protected Set<Attribute> cloneAttributesWithout(StartElement start, QName omit) {
        // clone attributes, but without rdf:content
        Set<Attribute> attributes = new LinkedHashSet<Attribute>();
        for (Iterator<Attribute> it = start.getAttributes(); it.hasNext(); ) {
            Attribute attribute = it.next();
            if (!attribute.getName().equals(omit))
                attributes.add(attribute);
        }
        return attributes;
    }
    
    private void writeXMLLiteral(String literal, XMLEventConsumer writer)
            throws XMLStreamException {
        XMLEventReader reader = inputFactory.createXMLEventReader(new StringReader(literal));
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.END_DOCUMENT:
                    break; // discard
                default:
                    writer.add(event);
            }
        }
    }
    
}
