/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.entries.NfsMountEntry;
import java.rmi.RemoteException;
import java.util.ArrayList;
import server.entries.NfsExportEntry;

/**
 *
 * @author diptam
 */
public class NfsMountManager {

    ArrayList<NfsMountEntry> nfsMountEntry;
    ArrayList<NfsExportEntry> nfsExportEntry;

    public NfsMountManager(ArrayList<NfsExportEntry> nfsExportEntry) {
        this.nfsExportEntry=nfsExportEntry;
    }
    
    
    
    public ArrayList<NfsMountEntry> initMountRecord()
    {
        nfsMountEntry = new ArrayList<>();
        return nfsMountEntry;
    }
    
    public boolean mntProcMnt(String ip, String remoteDirName, String localDirName,
            boolean read, boolean write) throws RemoteException {
        String exportDirName;
        if(!remoteDirName.endsWith("/")) remoteDirName=remoteDirName.concat("/");
        if(!localDirName.endsWith("/")) localDirName=localDirName.concat("/");
        
        for(NfsExportEntry entry: nfsExportEntry)
        {
            exportDirName = entry.getDirname();
            if(!exportDirName.endsWith("/"))
                exportDirName=exportDirName.concat("/");
            if(remoteDirName.equals(exportDirName))
            {
                if(entry.isReadable()==false && read==true) return false;
                if(entry.isWritable()==false && write==true) return false;
                if(!entry.getPermission().equals("everyone")) return false;
                NfsMountEntry mountEntry = new NfsMountEntry();
                mountEntry.setClientIp(ip);
                mountEntry.setLocalDir(localDirName);
                mountEntry.setRemoteDir(remoteDirName);
                mountEntry.setRead(read);
                mountEntry.setWrite(write);
                nfsMountEntry.add(mountEntry);
                System.out.println(nfsMountEntry);
                return true;
            }
        }
        System.out.println(nfsMountEntry);
        return false;
    }
    
}
