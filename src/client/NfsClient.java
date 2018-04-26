/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import misc.Global;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import rmi.Connection;
import server.NfsExportManager;
import server.nfsinterfaces.NfsInterface;
import xdr.Xdr;
import xdr.Xdr.AttrStat;
import xdr.Xdr.CreateArgs;
import xdr.Xdr.DirOpArgs;
import xdr.Xdr.DirOpRes;
import xdr.Xdr.Entry;
import xdr.Xdr.FHandle;
import xdr.Xdr.ReadArgs;
import xdr.Xdr.ReadDirArgs;
import xdr.Xdr.ReadDirRes;
import xdr.Xdr.ReadRes;
import xdr.Xdr.SetAttr;
import xdr.Xdr.Stat;
import xdr.Xdr.WriteArgs;


/**
 *
 * @author diptam
 */
public class NfsClient{
    Connection<NfsInterface> conn = null;
//    Connection<NfsMountInterface> mount = null;
    NfsInterface nfsinterface = null;
    String serverIp;
    String myIp;
    
    
    public NfsClient() {
        serverIp = getServerIp();
        myIp = getMyIp();
        Logger.getLogger(this.getClass().getName()).info("[Server IP] "+ serverIp);
        Logger.getLogger(this.getClass().getName()).info("[Own IP] "+myIp);
        System.setProperty("java.rmi.server.hostname",myIp);
        System.setProperty("java.security.policy","./security.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
}
    }
    
    private Document getXmlDoc(String filename)
    {
        File config = new File(this.getClass().
                getResource(filename).getFile());
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
        return doc;
    }
    
    private boolean addMountEntry(String localDir, String remoteDir)
    {
        Document doc = getXmlDoc(Global.CLIENT_MOUNT_FILE);
        if(doc==null) return false;
        Node node = doc.getElementsByTagName("MountList").item(0);
        Element mountRecord = doc.createElement("record");
        Element localdir = doc.createElement("local_dir");
        Element remotedir = doc.createElement("remote_dir");
        localdir.appendChild(doc.createTextNode(localDir));
        remotedir.appendChild(doc.createTextNode(remoteDir));
        mountRecord.appendChild(localdir);
        mountRecord.appendChild(remotedir);
        node.appendChild(mountRecord);
        doc.getDocumentElement().normalize();;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer=null;
         try {
             transformer = transformerFactory.newTransformer();
         } catch (TransformerConfigurationException ex) {
             Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,
                     ex.getMessage());
             return false;
         }
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(this.getClass().
                getResource(Global.CLIENT_MOUNT_FILE).getFile()));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         try {
             transformer.transform(source, result);
         } catch (TransformerException ex) {
             Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
             return false;
         }
        return true;
    }
    
    private String getServerIp()
    {
        Document doc= getXmlDoc(Global.CLIENT_CONFIG_FILE);
        NodeList nodelist = doc.getDocumentElement().getElementsByTagName("server_ip");
        Element node = (Element)nodelist.item(0);
        return node.getTextContent();
    }
    
    private String getMyIp()
    {
        Document doc = getXmlDoc(Global.CLIENT_CONFIG_FILE);
        NodeList nodelist = doc.getDocumentElement().getElementsByTagName("my_ip");
        Element node = (Element)nodelist.item(0);
        return node.getTextContent();
    }
    
    public boolean nfsMount(String localDir, String remoteDir, boolean read, boolean write)
    {
        try {
            conn  = new Connection<>(serverIp,Global.RMI_KEY);
            try {
                nfsinterface = conn.getStub();
            } catch (RemoteException ex) {
                Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,null,ex);
                return false;
            } catch (NotBoundException ex) {
                Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,null,ex);
                return false;
            }
            boolean status = nfsinterface.mntProcMnt(myIp, remoteDir, localDir, read, write);
            boolean update = false;
            if(status)
                update=addMountEntry(localDir,remoteDir);
            if(update) Logger.getLogger(this.getClass().getName()).info("MOUNT RECORD ADDED");
            return status;
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return false;
        }
    }
    
    public boolean nfsWrite(byte[] data,int offset,String localdirname, String target)
    {
        conn  = new Connection<>(serverIp,Global.RMI_KEY);
        try {
            nfsinterface = conn.getStub();
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, null,ex.getMessage());
            return false;
        } catch (NotBoundException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return false;
        }
        
        if(!localdirname.endsWith("/")) localdirname = localdirname.concat("/");
        if(target!=null && target.startsWith("/")) target = target.substring(1);
        String name= this.myIp+"@"+localdirname+"@"+localdirname;
        if(target!="" || target!=null) name=name+target;
        
        WriteArgs.Builder writeArgs = WriteArgs.newBuilder();
        FHandle.Builder fhandle = FHandle.newBuilder();
        fhandle.setFilename(name);
        writeArgs.setFhandle(fhandle.build());
        
        ByteString writedata = ByteString.copyFrom(data);
        writeArgs.setData(writedata);
        writeArgs.setCount(writedata.size());
        writeArgs.setOffset(offset);
        
        AttrStat attrstat=null;
        try {
            attrstat = nfsinterface.nfsProcWrite(writeArgs.build());
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return false;
        }
        
        if(attrstat.getStatus()==Stat.NF_OK)
            return true;
        return false;
    }
    
    public byte[] nfsRead(String localdirname, String target,int offset)
    {
        conn  = new Connection<>(serverIp,Global.RMI_KEY);
        try {
            nfsinterface = conn.getStub();
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, null,ex.getMessage());
            return null;
        } catch (NotBoundException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return null;
        }
        
        if(!localdirname.endsWith("/")) localdirname = localdirname.concat("/");
        if(target!=null && target.startsWith("/")) target = target.substring(1);
        String name= this.myIp+"@"+localdirname+"@"+localdirname;
        if(target!="" || target!=null) name=name+target;
        
        FHandle.Builder fhandle = FHandle.newBuilder();
        fhandle.setFilename(name);
        ReadArgs.Builder readArgs = ReadArgs.newBuilder();
        readArgs.setFhandle(fhandle.build());
        readArgs.setCount(Global.CHUNK_SIZE);
        readArgs.setOffset(0);
        
        ReadRes readRes=null;   
        try {
            readRes = nfsinterface.nfsProcRead(readArgs.build());
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
        ByteString data=readRes.getData();
        if(readRes.getStatus()==Stat.NF_OK)
            return data.toByteArray();
        return null;
    }   
    
    public boolean nfsCreate(String localdirname, String target)
    {
        conn  = new Connection<>(serverIp,Global.RMI_KEY);
        try {
            nfsinterface = conn.getStub();
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, null,ex.getMessage());
            return false;
        } catch (NotBoundException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return false;
        }
        
        if(!localdirname.endsWith("/")) localdirname = localdirname.concat("/");
        if(target!=null && target.startsWith("/")) target = target.substring(1);
        String name= this.myIp+"@"+localdirname+"@"+localdirname;
        if(target!="" || target!=null) name=name+target;
        
        CreateArgs.Builder createargs = CreateArgs.newBuilder();
        DirOpArgs.Builder diropargs = DirOpArgs.newBuilder();
        FHandle.Builder fhandle = FHandle.newBuilder();
        SetAttr.Builder setattr = SetAttr.newBuilder();
       
        fhandle.setFilename(name);
        diropargs.setFhandle(fhandle.build());
        setattr.setMode(-1);
        setattr.setSize(-1);
        createargs.setDiropargs(diropargs.build());
        createargs.setSetattr(setattr.build());
        
        DirOpRes response;
        try {
            response = nfsinterface.nfsProcCreate(createargs.build());
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,null,ex);
            return false;
        }
        if(response.getStatus()==Stat.NF_OK)
            return true;
        return false;
    }
    
    public ArrayList<String> readDir(String localdirname, String target)
    {
        conn  = new Connection<>(serverIp,Global.RMI_KEY);
        try {
            nfsinterface = conn.getStub();
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, null,ex.getMessage());
            return null;
        } catch (NotBoundException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return null;
        }
        ReadDirArgs.Builder readArgs= ReadDirArgs.newBuilder();
        FHandle.Builder fhandle = FHandle.newBuilder();
        if(!localdirname.endsWith("/")) localdirname = localdirname.concat("/");
        if(target!=null && target.startsWith("/")) target = target.substring(1);
        String name= this.myIp+"@"+localdirname+"@"+localdirname;
        if(target!="" || target!=null) name=name+target;
        System.out.println("[DEBUG] Sending Request: "+name);
        fhandle.setFilename(name);
        readArgs.setFhandle(fhandle.build());
        ReadDirRes res=null;
        try {
            res = nfsinterface.nfsProcReadDir(readArgs.build());
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return null;
        }
        ArrayList<String> response = new ArrayList<>();
        if(res.getStatus()==Stat.NF_OK)
        {
            List<Entry> entries =res.getEntriesList();
            for(Entry e: entries)
            {
                response.add(e.getFhandle().getFilename());
            }
        }
        return response;
    }
    
    public boolean makeDir(String localdirname, String target)
    {
        conn  = new Connection<>(serverIp,Global.RMI_KEY);
        try {
            nfsinterface = conn.getStub();
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, null,ex.getMessage());
            return false;
        } catch (NotBoundException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE,ex.getMessage());
            return false;
        }
        
        if(!localdirname.endsWith("/")) localdirname = localdirname.concat("/");
        if(target!=null && target.startsWith("/")) target = target.substring(1);
        String name= this.myIp+"@"+localdirname+"@"+localdirname;
        if(target!="" || target!=null) name=name+target;
        
//        System.out.println("[DEBUG] Sending Request: "+name);
        CreateArgs.Builder createArgs = CreateArgs.newBuilder();
        DirOpArgs.Builder dirOpArgs = DirOpArgs.newBuilder();
        FHandle.Builder fhandle = FHandle.newBuilder();
        fhandle.setFilename(name);
        dirOpArgs.setFhandle(fhandle.build());
        createArgs.setDiropargs(dirOpArgs.build());
        DirOpRes dirOpRes=null;
        try {
            dirOpRes = nfsinterface.nfsProcMakeDir(createArgs.build());
        } catch (RemoteException ex) {
            Logger.getLogger(NfsClient.class.getName()).log(Level.SEVERE, ex.getMessage());
            return false;
        }
        if(dirOpRes.getStatus()==Stat.NF_OK)
            return true;
        return false;
        
    }
    
    public boolean setAttributes(FileAttributes fileattributes, String filename){
        return true;
    }
    
    public FileAttributes getAttributes(String filename)
    {
        return null;
    }
}
