package com.mdm.commands;

import picocli.CommandLine.Command;
import com.mdm.util.ConsoleUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "list", description = "Lists all dependencies in the local pom.xml")
public class ListCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        File pom = new File("pom.xml");
        if (!pom.exists()) {
            System.err.println("pom.xml not found.");
            return 1;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pom);
        doc.getDocumentElement().normalize();

        ConsoleUtils.printHeader("Dependency List");
        ConsoleUtils.printDivider();
        ConsoleUtils.printRow("GROUP ID", "ARTIFACT ID", "VERSION", "SCOPE");
        ConsoleUtils.printDivider();
        
        // 1. Parse properties to resolve placeholders
        java.util.Map<String, String> properties = new java.util.HashMap<>();
        NodeList propList = doc.getElementsByTagName("properties");
        if (propList.getLength() > 0) {
            NodeList props = propList.item(0).getChildNodes();
            for (int j = 0; j < props.getLength(); j++) {
                Node pNode = props.item(j);
                if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                    properties.put(pNode.getNodeName(), pNode.getTextContent().trim());
                }
            }
        }

        NodeList nList = doc.getElementsByTagName("dependency");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String g = getTagValue("groupId", eElement);
                String a = getTagValue("artifactId", eElement);
                String v = getTagValue("version", eElement);
                String s = getTagValue("scope", eElement);
                
                // Resolve version if it is a property like ${foo.version}
                if (v != null && v.startsWith("${") && v.endsWith("}")) {
                    String key = v.substring(2, v.length() - 1);
                    if (properties.containsKey(key)) {
                        v = properties.get(key); // Show resolved value!
                    }
                }
                
                ConsoleUtils.printRow(
                        g != null ? g : "-", 
                        a != null ? a : "-", 
                        v != null ? v : "LATEST", 
                        s != null ? s : "compile");
            }
        }
        ConsoleUtils.printDivider();
        return 0;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nlList = element.getElementsByTagName(tag);
        if (nlList != null && nlList.getLength() > 0) {
            Node nValue = nlList.item(0).getFirstChild();
            if (nValue != null) {
                return nValue.getNodeValue().trim();
            }
        }
        return null;
    }
}
