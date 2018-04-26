/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.entries.NfsExportEntry;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import misc.Global;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author diptam
 */
public class NfsExportManager {

    public NfsExportManager() {
    }
    
    ArrayList<NfsExportEntry> getExportList()
    {
        File exportlist = new File(this.getClass().
                getResource(Global.EXPORT_LIST_FILE).getFile());
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder=null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NfsExportManager.class.getName()).log(Level.SEVERE,
                    ex.getMessage());
            return null;
        }
        Document doc=null;
        try {
            doc = dBuilder.parse(exportlist);
        } catch (SAXException ex) {
            Logger.getLogger(NfsExportManager.class.getName()).log(Level.SEVERE,
                    ex.getMessage());
            return null;
        } catch (IOException ex) {
            Logger.getLogger(NfsExportManager.class.getName()).log(Level.SEVERE,
                    ex.getMessage());
            return null;
        }
        NodeList nodelist = doc.getDocumentElement().getElementsByTagName("entry");
        ArrayList<NfsExportEntry> exportList = new ArrayList<>();
        Element node=null;
        for(int i=0; i<nodelist.getLength();i++)
        {
            node = (Element)nodelist.item(i);
            NfsExportEntry nfsexportentry = new NfsExportEntry();
            nfsexportentry.setReadable(false);
            nfsexportentry.setWritable(false);
            nfsexportentry.setDirname(node.getElementsByTagName("dir").item(0)
            .getTextContent().trim());
            nfsexportentry.setPermission(node.getElementsByTagName("exportTo")
            .item(0).getTextContent().trim());
            node = (Element)node.getElementsByTagName("permission").item(0);
            if(node.getElementsByTagName("read").item(0).getTextContent().trim()
                    .equalsIgnoreCase("true"))
                nfsexportentry.setReadable(true);
            if(node.getElementsByTagName("write").item(0).getTextContent().trim()
                    .equalsIgnoreCase("true"))
                nfsexportentry.setWritable(true);
            exportList.add(nfsexportentry);
        }
        System.out.println(exportList);
        return exportList;
    }
}
