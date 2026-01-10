package com.mdm.commands;

import picocli.CommandLine.Command;
import com.mdm.util.ConsoleUtils;
import com.mdm.core.OsvClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import picocli.CommandLine.Command;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "audit", description = "Scans dependencies for known security vulnerabilities (CVEs)")
public class AuditCommand implements Callable<Integer> {

    private final OsvClient osvClient;

    public AuditCommand() {
        this.osvClient = new OsvClient();
    }

    @Override
    public Integer call() throws Exception {
        File pom = new File("pom.xml");
        if (!pom.exists()) {
            System.err.println("pom.xml not found.");
            return 1;
        }

        ConsoleUtils.printHeader("Security Audit");
        ConsoleUtils.info("Scanning project dependencies via OSV.dev...");
        System.out.println(); // spacer

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pom);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("dependency");
        int vulnCount = 0;
        int checkedCount = 0;

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String g = getTagValue("groupId", eElement);
                String a = getTagValue("artifactId", eElement);
                String v = getTagValue("version", eElement);

                if (g != null && a != null && v != null && !v.startsWith("${")) {
                    checkedCount++;
                    // Basic status line
                    System.out.printf("Checking %-50s ... ", g + ":" + a + ":" + v);
                    
                    List<String> issues = osvClient.checkVulnerabilities(g, a, v);
                    if (!issues.isEmpty()) {
                        System.out.println(picocli.CommandLine.Help.Ansi.AUTO.string("@|red VULNERABLE|@"));
                        for (String issue : issues) {
                            System.out.println("   -> " + issue);
                        }
                        vulnCount++;
                    } else {
                        System.out.println(picocli.CommandLine.Help.Ansi.AUTO.string("@|green OK|@"));
                    }
                }
            }
        }

        System.out.println();
        ConsoleUtils.printDivider();
        ConsoleUtils.info("Scan Complete. Dependencies Checked: " + checkedCount);
        if (vulnCount > 0) {
            ConsoleUtils.error("Found " + vulnCount + " vulnerable dependencies.");
            return 1;
        } else {
            ConsoleUtils.success("No known vulnerabilities found.");
            return 0;
        }
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
