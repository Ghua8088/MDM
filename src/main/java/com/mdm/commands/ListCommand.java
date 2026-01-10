package com.mdm.commands;

import picocli.CommandLine.Command;
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

        System.out.printf("%-30s | %-30s | %-15s | %-10s%n", "Group ID", "Artifact ID", "Version", "Scope");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        NodeList nList = doc.getElementsByTagName("dependency");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String g = getTagValue("groupId", eElement);
                String a = getTagValue("artifactId", eElement);
                String v = getTagValue("version", eElement);
                String s = getTagValue("scope", eElement);
                
                // version might be a property reference ${...}, removing that logic for simplicity unless requested
                
                System.out.printf("%-30s | %-30s | %-15s | %-10s%n", 
                        g != null ? g : "-", 
                        a != null ? a : "-", 
                        v != null ? v : "LATEST", 
                        s != null ? s : "compile");
            }
        }
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
