/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.nfsinterfaces.NfsInterface;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.entries.ClientReqInfo;
import server.entries.NfsExportEntry;
import server.entries.NfsMountEntry;
import xdr.Xdr.AttrStat;
import xdr.Xdr.CreateArgs;
import xdr.Xdr.DirOpRes;
import xdr.Xdr.Entry;
import xdr.Xdr.FHandle;
import xdr.Xdr.FType;
import xdr.Xdr.FileAttr;
import xdr.Xdr.ReadArgs;
import xdr.Xdr.ReadDirArgs;
import xdr.Xdr.ReadDirRes;
import xdr.Xdr.ReadRes;
import xdr.Xdr.SetAttr;
import xdr.Xdr.SetAttrArgs;
import xdr.Xdr.Stat;
import xdr.Xdr.WriteArgs;

/**
 *
 * @author diptam
 */
public class NfsServer implements NfsInterface  {

    ArrayList<NfsMountEntry> mountRecord;
    ArrayList<NfsExportEntry> nfsExportEntry;
    public NfsServer(ArrayList<NfsMountEntry> mountEntrys, ArrayList<NfsExportEntry> nfsExportEntry) {
        this.mountRecord = mountEntrys;
        this.nfsExportEntry = nfsExportEntry;
    }

    /**
     * 
     * @param clinetIp
     * @param clientDir
     * @return NfsMountEntry: that consist corresponding client mount details 
     */
    private NfsMountEntry lookupMountRecord(String clinetIp, String clientDir)
    {
//        System.out.println(clinetIp+" "+clientDir);
        for(NfsMountEntry entry : mountRecord)
        {
            if(entry.getClientIp().equals(clinetIp))
            {
                if(entry.getLocalDir().equals(clientDir))
                    return entry;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param info
     * @return Client Request Information
     */
    private ClientReqInfo getClientReqInfo(String info)
    {
        String[] infoArray= info.split("@");
        String clientIp= infoArray[0];
        String clientDir= infoArray[1];
        if(!clientDir.endsWith("/")) clientDir= clientDir.concat("/");
        String target= infoArray[2];
        
//        System.out.println("[DEBUG] getClientReqInfo "+clientDir+" "+target);
        if(target.length()>clientDir.length())
            target= target.substring(clientDir.length());
        else if(target.equals("null")) target="";
        else target="";
        ClientReqInfo clientReq= new ClientReqInfo();
        NfsMountEntry nfsMount = lookupMountRecord(clientIp, clientDir);
        if(nfsMount==null)
            return null;
        clientReq.setFileOrDirName(target);
        clientReq.setIp(clientIp);
        clientReq.setLocalMountDir(clientDir);
        clientReq.setRemoteMountDir(nfsMount.getRemoteDir());
        clientReq.setRead(nfsMount.isRead());
        clientReq.setWrite(nfsMount.isWrite());
        return clientReq;
    }
    
    
    
    @Override
    /*
    If the reply status is NFS_OK, then the reply attributes contains the
    attributes for the file given by the input fhandle.
    */
    public AttrStat nfsProcGetAttr(FHandle fhandle) throws RemoteException{
        char[] accessmode = new char[3];
        for(int i=0;i<3;i++) accessmode[i]='0';
        AttrStat.Builder attrstat = AttrStat.newBuilder();
        FileAttr.Builder fileattr = FileAttr.newBuilder();
        String filename = fhandle.getFilename();
        File file = new File(filename);
        if(file.exists()) {attrstat.setStatus(Stat.NF_ERR);
        return attrstat.build();}
        if(file.canRead()) accessmode[0]='1';
        if(file.canWrite()) accessmode[1]='1';
        if(file.canExecute()) accessmode[2]='1';
        fileattr.setMode(Integer.parseInt(new String(accessmode)));
        fileattr.setSize(file.length());
        if(file.isFile()) fileattr.setType(FType.NFREG);
        if(file.isDirectory()) fileattr.setType(FType.NFDIR);
        attrstat.setStatus(Stat.NF_OK);
        attrstat.setFileattr(fileattr.build());
        return attrstat.build();
    }

    /*
    The "attributes" argument contains fields which are either -1 or are
    the new value for the attributes of "file".  If the reply status is
    NFS_OK, then the reply attributes have the attributes of the file
    after the "SETATTR" operation has completed.
    */
    @Override
    public AttrStat nfsProcSetAttr(SetAttrArgs setattrargs) throws RemoteException {
        String filename = setattrargs.getFhandle().getFilename();
        File file = new File(filename);
        SetAttr setattr= setattrargs.getSetattr();
        int mode= setattr.getMode();
        int size= setattr.getSize();
        if(mode>=0)
        {
            int per;
            per=mode%10; mode=mode/10;
            if(per==1)file.setExecutable(true); else file.setExecutable(false);
            per=mode%10; mode=mode/10;
            if(per==1)file.setWritable(true); else file.setWritable(false);
            per=mode%10; mode=mode/10;
            if(per==1)file.setReadable(true); else file.setReadable(false);
        }
        if(size!=-1 && size==0){
            file.delete();
        }
        return nfsProcGetAttr(setattrargs.getFhandle());
    }
    
    /*
    Returns up to "count" bytes of "data" from the file given by "file",
    starting at "offset" bytes from the beginning of the file.  The first
    byte of the file is at offset zero.  The file attributes after the
    read takes place are returned in "attributes".
    */
    @Override
    public ReadRes nfsProcRead(ReadArgs readargs) throws RemoteException {
        ClientReqInfo clientReq = getClientReqInfo(readargs.getFhandle()
                .getFilename());
        FileAttr.Builder fileattr = FileAttr.newBuilder(); // this is unused so far
        ReadRes.Builder res =ReadRes.newBuilder();
        
        if(clientReq==null)
        {
            res.setStatus(Stat.NF_ERR);
            return res.build();
        }
        String target;
        if(clientReq.getFileOrDirName()!=null)
            target=clientReq.getRemoteMountDir()+clientReq.getFileOrDirName();
        else
            target=clientReq.getRemoteMountDir();
        File file = new File(target);
        RandomAccessFile randfile;
        
        try {
            randfile = new RandomAccessFile(file,"r");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE,ex.getMessage());
            res.setStatus(Stat.NF_ERR);
            return res.build();
        }
        int count= readargs.getCount();
        int offset= readargs.getOffset();
        
        byte[] data = new byte[count];
        try {
            randfile.read(data, offset, count);
        } catch (IOException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE, ex.getMessage());
            res.setStatus(Stat.NF_ERR);
            return res.build();
        }
        try {
            randfile.close();
        } catch (IOException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE, ex.getMessage());
            res.setStatus(Stat.NF_ERR);
            return res.build();
        }
        res.setData(ByteString.copyFrom(data));
        res.setStatus(Stat.NF_OK);
        res.setFileattr(fileattr.build()); // fileattr is vacant now
     
        return res.build();
    }
    
    /*
    Writes "data" beginning "offset" bytes from the beginning of "file".
    The first byte of the file is at offset zero.  If the reply "status"
    is NFS_OK, then the reply "attributes" contains the attributes of the
    file after the write has completed.  The write operation is atomic.
    Data from this "WRITE" will not be mixed with data from another
    client's "WRITE"
    */
    @Override
    public AttrStat nfsProcWrite(WriteArgs writeargs) throws RemoteException {
        ClientReqInfo clientReq = getClientReqInfo(writeargs.getFhandle()
                .getFilename());
        AttrStat.Builder attrstat =AttrStat.newBuilder(); 
        
        if(clientReq==null)
        {
            attrstat.setStatus(Stat.NF_ERR);
            return attrstat.build();
        }
        
        String target;
        if(clientReq.getFileOrDirName()!=null)
            target=clientReq.getRemoteMountDir()+clientReq.getFileOrDirName();
        else
            target=clientReq.getRemoteMountDir();
        File file = new File(target);
        if(!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE,ex.getMessage());
                attrstat.setStatus(Stat.NF_ERR);
                return attrstat.build();
            }
        }
        RandomAccessFile randfile;
        try {
            randfile = new RandomAccessFile(file,"rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE, ex.getMessage());
            attrstat.setStatus(Stat.NF_ERR);
            return attrstat.build();
        }
        byte[] data_to_write = writeargs.getData().toByteArray();
        int beginoffset= writeargs.getOffset();
        int count=writeargs.getCount();
        
        try {
            randfile.write(data_to_write, beginoffset, count);
        } catch (IOException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE, ex.getMessage());
            attrstat.setStatus(Stat.NF_ERR);
            return attrstat.build();
        }
        try {
            randfile.close();
        } catch (IOException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE, ex.getMessage());
            attrstat.setStatus(Stat.NF_ERR);
            return attrstat.build();
        }
        return nfsProcGetAttr(writeargs.getFhandle());
    }

    /*
   Returns a variable number of directory entries, with a total size of
   up to "count" bytes, from the directory given by "dir".  If the
   returned value of "status" is NFS_OK, then it is followed by a
   variable number of "entry"s.
    */
    @Override
    public ReadDirRes nfsProcReadDir(ReadDirArgs readdirargs) throws RemoteException {
        ClientReqInfo clientReq = getClientReqInfo(readdirargs.getFhandle()
                .getFilename());
        ReadDirRes.Builder readDirRes = ReadDirRes.newBuilder();
        if(clientReq==null)
        {
            System.out.println("[DEBUG] Found Null");
            readDirRes.setStatus(Stat.NF_ERR);
            return readDirRes.build();
        }
        String target;
        System.out.println(clientReq.getFileOrDirName());
        if(clientReq.getFileOrDirName().equals("null") ||
                clientReq.getFileOrDirName().length()==0 ||
                clientReq.getFileOrDirName().equals(""))
            target=clientReq.getRemoteMountDir();
        else
            target=clientReq.getRemoteMountDir()+clientReq.getFileOrDirName();
        
        File folder = new File(target);
        System.out.println("[DEBUG] Server Directory name: "+ folder);
        Entry.Builder entry=null;
        FHandle.Builder fhandle=null;
        
        if(!clientReq.isRead())
        {
            System.out.println("[DEBUG] not readable");
            readDirRes.setStatus(Stat.NF_ERR);
            return readDirRes.build();
        }
        if(!folder.exists() || folder.isFile())
        {
            System.out.println("[DEBUG] It is a file or does not exist");
            readDirRes.setStatus(Stat.NF_ERR);
            return readDirRes.build();
        }
        readDirRes.setStatus(Stat.NF_OK);    
        File[] listOfFiles = folder.listFiles();    
        int num = readdirargs.getCount() > 0 ? readdirargs.getCount():listOfFiles.length; 
        for(int i=0;i<num;i++)
        { 
            fhandle = FHandle.newBuilder();
            fhandle.setFilename(listOfFiles[i].getName());
            entry = Entry.newBuilder();
            entry.setFhandle(fhandle.build());
            readDirRes.addEntries(entry.build());
        }
        return readDirRes.build();
    }

    /*
   The new directory "where.name" is created in the directory given by
   "where.dir".  The initial attributes of the new directory are given
   by "attributes".  A reply "status" of NFS_OK indicates that the new
   directory was created, and reply "file" and reply "attributes" are
   its file handle and attributes.  Any other reply "status" means that
   the operation failed and no directory was created.
    */
    @Override
    public DirOpRes nfsProcMakeDir(CreateArgs createargs) throws RemoteException {
        ClientReqInfo clientReq = getClientReqInfo(createargs.getDiropargs().getFhandle()
                .getFilename());
        DirOpRes.Builder dirOpRes = DirOpRes.newBuilder();
        
        if(clientReq==null)
        {
//            System.out.println("[DEBUG] Found Null");
            dirOpRes.setStatus(Stat.NF_ERR);
            return dirOpRes.build();
        }
        
        String target;
        if(clientReq.getFileOrDirName()!=null)
            target=clientReq.getRemoteMountDir()+clientReq.getFileOrDirName();
        else
            target=clientReq.getRemoteMountDir();
        File dir = new File(target);
//        System.out.println("[DEBUG] Server Directory name: "+ dir);
        
        if(dir.exists() || dir.isDirectory())
        {
            dirOpRes.setStatus(Stat.NF_ERR);
            return dirOpRes.build();
        }
        boolean status=dir.mkdirs();
        if(status)
        {
            dirOpRes.setStatus(Stat.NF_OK);
            return dirOpRes.build();
        }
        dirOpRes.setStatus(Stat.NF_ERR);
        return dirOpRes.build();
    }

    /*
    The file "name" is created in the directory given by "dir".  The
    initial attributes of the new file are given by "attributes".  A
    reply "status" of NFS_OK indicates that the file was created, and
    reply "file" and reply "attributes" are its file handle and
    attributes.  Any other reply "status" means that the operation failed
    and no file was created.
    */
    @Override
    public DirOpRes nfsProcCreate(CreateArgs createargs) throws RemoteException{
        ClientReqInfo clientReq = getClientReqInfo(createargs.getDiropargs().getFhandle()
                .getFilename());
        DirOpRes.Builder dirOpRes = DirOpRes.newBuilder();
        
        if(clientReq==null)
        {
            dirOpRes.setStatus(Stat.NF_ERR);
            return dirOpRes.build();
        }
        SetAttr setattr=createargs.getSetattr();
        String target;
        if(clientReq.getFileOrDirName()!=null)
            target=clientReq.getRemoteMountDir()+clientReq.getFileOrDirName();
        else
            target=clientReq.getRemoteMountDir();
        File file = new File(target);
        boolean status=false;
        try {
            status=file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NfsServer.class.getName()).log(Level.SEVERE, ex.getMessage());
            dirOpRes.setStatus(Stat.NF_ERR);
            return dirOpRes.build();
        }
        if(!status)
        {
            dirOpRes.setStatus(Stat.NF_ERR);
            return dirOpRes.build();
        }
        SetAttrArgs.Builder setattrargs = SetAttrArgs.newBuilder();
        FHandle.Builder fhandle = FHandle.newBuilder();
        setattrargs.setFhandle(fhandle.setFilename(file.getName()).build());
        setattrargs.setSetattr(setattr);
        AttrStat attrstat = nfsProcSetAttr(setattrargs.build());
        dirOpRes.setFhandle(createargs.getDiropargs().getFhandle());
        dirOpRes.setStatus(Stat.NF_OK);
        dirOpRes.setFileattr(attrstat.getFileattr());
        return dirOpRes.build();
    }  
    
    /*
   If the reply "status" is 0, then the reply "directory" contains the
   file handle for the directory "dirname".  This file handle may be
   used in the NFS protocol.  This procedure also adds a new entry to
   the mount list for this client mounting "dirname".
    */
    
    @Override
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
                mountRecord.add(mountEntry);
                System.out.println(mountRecord);
                return true;
            }
        }
        return false;
    }
}
