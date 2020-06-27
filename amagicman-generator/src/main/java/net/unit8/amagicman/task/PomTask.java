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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author kawasima
 */
public class PomTask implements GenTask {
    private String destination = "pom.xml";
    private final List<Dependency> dependencies;
    DocumentBuilder documentBuilder;
    XPathFactory xpath;
    TransformerFactory transformerFactory;
    File pomFile;

    public PomTask(String destination) {
        this();
        setDestinationPath(destination);
    }

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

    private Node select(Node node, String selector) {
        NodeList nodelist = selectAll(node, selector);
        if (nodelist.getLength() > 0) {
            return nodelist.item(0);
        } else {
            return null;
        }
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {

        Document doc = loadPom(Optional.ofNullable(pomFile).orElse(new File(pathResolver.project(), "pom.xml")));
        Node pomDependencies = (Node) xpath.newXPath().evaluate("/project/dependencies", doc, XPathConstants.NODE);

        if (pomDependencies == null) {
            pomDependencies = doc.createElement("dependencies");
            Node project = (Node) xpath.newXPath().evaluate("/project", doc, XPathConstants.NODE);
            project.appendChild(pomDependencies);
        }

        for (Dependency dependency : dependencies) {
            NodeList pomDependencyList = selectAll(pomDependencies, "dependency");
            if (IntStream.range(0, pomDependencyList.getLength()).mapToObj(pomDependencyList::item)
                    .noneMatch(pomDependency -> Optional.ofNullable(select(pomDependency, "groupId"))
                            .map(node -> node.getTextContent().trim())
                            .orElse("")
                            .equals(dependency.getGroupId())
                            && Optional.ofNullable(select(pomDependency, "artifactId"))
                            .map(node -> node.getTextContent().trim())
                            .orElse("")
                            .equals(dependency.getArtifactId())
                            && Optional.ofNullable(select(pomDependency, "version"))
                            .map(node -> node.getTextContent().trim())
                            .orElse("")
                            .equals(dependency.getVersion()))) {
                Node pomDependency = doc.createElement("dependency");
                pomDependency.appendChild(Optional.of(doc.createElement("groupId")).map(el -> { el.setTextContent(dependency.getGroupId()); return el; }).get());
                pomDependency.appendChild(Optional.of(doc.createElement("artifactId")).map(el -> { el.setTextContent(dependency.getArtifactId()); return el; }).get());
                pomDependency.appendChild(Optional.of(doc.createElement("version")).map(el -> { el.setTextContent(dependency.getVersion()); return el; }).get());
                Optional.ofNullable(dependency.getScope())
                        .ifPresent(scope -> pomDependency
                                .appendChild(Optional.of(doc.createElement("scope")).map(el -> { el.setTextContent(scope); return el; }).get()));
                pomDependencies.appendChild(pomDependency);
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


    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    public PomTask addDependency(String groupId, String artifactId, String version) {
        return addDependency(groupId, artifactId, version, null);
    }
    public PomTask addDependency(String groupId, String artifactId, String version, String scope) {
        dependencies.add(new Dependency(groupId, artifactId, version, scope));
        return this;
    }

    public static class Dependency implements Serializable {
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

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getScope() {
            return scope;
        }
    }

    public String getDestinationPath() {
        return destination;
    }

    public void setDestinationPath(String destination) {
        this.destination = destination;
    }
}
