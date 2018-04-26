/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.IOException;
import server.entries.NfsExportEntry;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import misc.Global;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import rmi.Server;
import server.entries.NfsMountEntry;
import server.nfsinterfaces.NfsInterface;

/**
 *
 * @author diptam
 */
public class NfsDriver {
    
    
    public String getServerIp()
    {
        File config = new File(this.getClass().
                getResource(Global.SERVER_CONFIG_FILE).getFile());
        
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
            doc = dBuilder.parse(config);
        } catch (SAXException ex) {
            Logger.getLogger(NfsExportManager.class.getName()).log(Level.SEVERE,
                    ex.getMessage());
            return null;
        } catch (IOException ex) {
            Logger.getLogger(NfsExportManager.class.getName()).log(Level.SEVERE,
                    ex.getMessage());
            return null;
        }
        
        NodeList nodelist = doc.getDocumentElement().getElementsByTagName("ip");
        Element node = (Element)nodelist.item(0);
        return node.getTextContent();
    }
    
    public static void main(String[] args)
    {   
        /* look for IP */
        NfsDriver driver = new NfsDriver();
        String ip = driver.getServerIp();
        
        Logger.getLogger(driver.getClass().getName()).info("Server Ip: "+ip);
        
        /* export dir */
        NfsExportManager nfsexmanager = new NfsExportManager();
        ArrayList<NfsExportEntry> exportEntrys= nfsexmanager.getExportList();
        ArrayList<NfsMountEntry> nfsMountEntrys = new ArrayList<>();
        
        /* prepare NFS server */
        Server<NfsInterface> server = new Server<>(ip,Global.RMI_KEY);
     
        /* start nfs server */
        server.listen(new NfsServer(nfsMountEntrys,exportEntrys));
        
    }
    
}
