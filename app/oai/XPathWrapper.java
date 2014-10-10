package oai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * Created by Ott Konstantin on 25.08.2014.
 */
/**
 * Helper class that eases the job of handling XPath queries and
 * getting the different results. It wraps a {@link org.dom4j.Node}
 * and then performs the XPath queries on that.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public class XPathWrapper {
    private Node node;
    private Map<String, String> namespaces;
    /**
     * Wraps a <code>Node</code> for easier XPath handling.
     *
     * @param node the <code>Node</code> to wrap
     */
    public XPathWrapper(Node node) {
        this(node, new HashMap<String, String>());
    }
    /**
     * Creates a <code>XPathWrapper</code> that should be aware of all
     * the namespaces in the Map when handling XPath queries.
     *
     * @param node the <code>Node</code> to wrap
     * @param namespaces the namespaces
     */
    public XPathWrapper(Node node, Map<String, String> namespaces) {
        this.node = node;
        this.namespaces = namespaces;
    }
    /**
     * Adds a namespace that the wrapper should be aware of when performing
     * XPath queries.
     *
     * @param prefix the prefix of the namespace
     * @param uri the uri of the namespace
     */
    public void addNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }
    /**
     * Selects a single node that matches the given XPath.
     *
     * @param xpathExpression the xpath
     *
     * @return a single <code>Node</code> or <code>null</code> if no match
     */
    public Node selectSingleNode(String xpathExpression) {
        XPath xpath = createXPath(xpathExpression);
        return xpath.selectSingleNode(node);
    }
    /**
     * Selects all <code>Nodes</code> that matches the given XPath.
     *
     * @param xpathExpression the xpath
     *
     * @return a list of nodes (can be empty if no matches)
     */
    @SuppressWarnings("unchecked")
    public List<Node> selectNodes(String xpathExpression) {
        XPath xpath = createXPath(xpathExpression);
        return xpath.selectNodes(node);
    }
    /**
     * Selects a single result that matches the given XPath and
     * then casts it into a <code>Element</code>.
     *
     * @param xpathExpression the xpath
     *
     * @return a single <code>Element</code> or <code>null</code> if no match
     */
    public Element selectSingleElement(String xpathExpression) {
        return (Element) selectSingleNode(xpathExpression);
    }
    /**
     * Selects the value of the given XPath.
     *
     * @param xpathExpression the xpath
     *
     * @return the value or <code>null</code> if no match
     */
    public String valueOf(String xpathExpression) {
        XPath xpath = createXPath(xpathExpression);
        return xpath.valueOf(node);
    }
    private XPath createXPath(String xpathExpression) {
        XPath xpath = DocumentHelper.createXPath(xpathExpression);
        xpath.setNamespaceURIs(namespaces);
        return xpath;
    }
}
