package net.unit8.amagicman.task;

import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A task that adds Maven dependencies to a {@code pom.xml} file.
 *
 * <p>The target POM is parsed as a DOM document. Each dependency registered via
 * {@link #addDependency} is inserted into the {@code /project/dependencies} section
 * only if an identical entry (same {@code groupId}, {@code artifactId}, and
 * {@code version}) does not already exist. The {@code <dependencies>} element is
 * created automatically if absent. The modified document is written back to the
 * destination as UTF-8 XML with 4-space indentation.</p>
 *
 * <pre>{@code
 * new PomTask()
 *     .addDependency("org.slf4j", "slf4j-api", "2.0.17")
 *     .addDependency("org.junit.jupiter", "junit-jupiter", "5.12.0", "test")
 * }</pre>
 *
 * @author kawasima
 */
public class PomTask implements GenTask {
    private String destination = "pom.xml";
    private final List<Dependency> dependencies;
    DocumentBuilder documentBuilder;
    XPathFactory xpath;
    TransformerFactory transformerFactory;
    File pomFile;

    /**
     * Constructs a {@code PomTask} with a custom destination path.
     *
     * @param destination relative path of the POM file to modify
     */
    public PomTask(String destination) {
        this();
        setDestinationPath(destination);
    }

    /**
     * Constructs a {@code PomTask} that targets {@code pom.xml} in the project root.
     */
    public PomTask() {
        dependencies = new ArrayList<>();
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xpath = XPathFactory.newInstance();
            transformerFactory = TransformerFactory.newInstance();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Document loadPom(File pomFile) throws IOException, SAXException {
        try (InputStream is = new FileInputStream(pomFile)) {
            return documentBuilder.parse(is);
        }
    }

    private NodeList selectAll(Node node, String selector) {
        try {
            return (NodeList) xpath.newXPath().evaluate(selector, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void appendTextElement(Document doc, Node parent, String tagName, String textContent) {
        Node el = doc.createElement(tagName);
        el.setTextContent(textContent);
        parent.appendChild(el);
    }

    /**
     * Builds a {@link Set} of {@code groupId:artifactId:version} keys from the existing
     * {@code <dependency>} elements in the given DOM node.
     *
     * @param pomDependencies the {@code <dependencies>} DOM node
     * @return set of composite keys for all existing dependencies
     */
    private Set<String> buildExistingDependencyKeys(Node pomDependencies) {
        Set<String> keys = new HashSet<>();
        NodeList list = selectAll(pomDependencies, "dependency");
        for (int i = 0; i < list.getLength(); i++) {
            Node dep = list.item(i);
            NodeList groupIdNodes = selectAll(dep, "groupId");
            NodeList artifactIdNodes = selectAll(dep, "artifactId");
            NodeList versionNodes = selectAll(dep, "version");
            String g = groupIdNodes.getLength() > 0 ? groupIdNodes.item(0).getTextContent().trim() : "";
            String a = artifactIdNodes.getLength() > 0 ? artifactIdNodes.item(0).getTextContent().trim() : "";
            String v = versionNodes.getLength() > 0 ? versionNodes.item(0).getTextContent().trim() : "";
            keys.add(g + ":" + a + ":" + v);
        }
        return keys;
    }

    /**
     * Loads the POM, inserts any missing dependencies, and writes the result.
     *
     * @param pathResolver resolver for the project directory and destination output
     * @throws Exception if XML parsing, XPath evaluation, or I/O fails
     */
    @Override
    public void execute(PathResolver pathResolver) throws Exception {

        Document doc = loadPom(Optional.ofNullable(pomFile).orElse(new File(pathResolver.project(), "pom.xml")));
        Node pomDependencies = (Node) xpath.newXPath().evaluate("/project/dependencies", doc, XPathConstants.NODE);

        if (pomDependencies == null) {
            pomDependencies = doc.createElement("dependencies");
            Node project = (Node) xpath.newXPath().evaluate("/project", doc, XPathConstants.NODE);
            project.appendChild(pomDependencies);
        }

        Set<String> existingKeys = buildExistingDependencyKeys(pomDependencies);
        for (Dependency dependency : dependencies) {
            String key = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
            if (!existingKeys.contains(key)) {
                Node pomDependency = doc.createElement("dependency");
                appendTextElement(doc, pomDependency, "groupId", dependency.getGroupId());
                appendTextElement(doc, pomDependency, "artifactId", dependency.getArtifactId());
                appendTextElement(doc, pomDependency, "version", dependency.getVersion());
                if (dependency.getScope() != null) {
                    appendTextElement(doc, pomDependency, "scope", dependency.getScope());
                }
                pomDependencies.appendChild(pomDependency);
                existingKeys.add(key);
            }
        }

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        try (Writer writer = new OutputStreamWriter(pathResolver.destinationAsStream("pom.xml"))) {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        }
    }

    /**
     * Overrides the POM file to be modified instead of using the project's default {@code pom.xml}.
     *
     * @param pomFile the POM file to read and modify
     */
    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    /**
     * Registers a dependency to add (without a scope).
     *
     * @param groupId    Maven group ID
     * @param artifactId Maven artifact ID
     * @param version    dependency version
     * @return this task instance for method chaining
     */
    public PomTask addDependency(String groupId, String artifactId, String version) {
        return addDependency(groupId, artifactId, version, null);
    }

    /**
     * Registers a dependency to add with an explicit scope.
     *
     * @param groupId    Maven group ID
     * @param artifactId Maven artifact ID
     * @param version    dependency version
     * @param scope      Maven scope (e.g. {@code "test"}, {@code "provided"}); may be {@code null}
     * @return this task instance for method chaining
     */
    public PomTask addDependency(String groupId, String artifactId, String version, String scope) {
        dependencies.add(new Dependency(groupId, artifactId, version, scope));
        return this;
    }

    /**
     * Immutable value object representing a single Maven dependency.
     */
    public static class Dependency implements Serializable {

        /**
         * Constructs a {@code Dependency}.
         *
         * @param groupId    Maven group ID
         * @param artifactId Maven artifact ID
         * @param version    dependency version
         * @param scope      Maven scope; may be {@code null}
         */
        public Dependency(String groupId, String artifactId, String version, String scope) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.scope = scope;
        }

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String scope;

        /** @return Maven group ID */
        public String getGroupId() { return groupId; }

        /** @return Maven artifact ID */
        public String getArtifactId() { return artifactId; }

        /** @return dependency version */
        public String getVersion() { return version; }

        /** @return Maven scope, or {@code null} if not specified */
        public String getScope() { return scope; }
    }

    /** {@inheritDoc} */
    public String getDestinationPath() {
        return destination;
    }

    /**
     * Sets the relative path of the destination POM file.
     *
     * @param destination relative path of the POM to write (default: {@code "pom.xml"})
     */
    public void setDestinationPath(String destination) {
        this.destination = destination;
    }
}
