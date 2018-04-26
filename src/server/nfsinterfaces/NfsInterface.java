/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.nfsinterfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;
import xdr.Xdr.AttrStat;
import xdr.Xdr.CreateArgs;
import xdr.Xdr.DirOpRes;
import xdr.Xdr.FHandle;
import xdr.Xdr.ReadArgs;
import xdr.Xdr.ReadDirArgs;
import xdr.Xdr.ReadDirRes;
import xdr.Xdr.ReadRes;
import xdr.Xdr.SetAttrArgs;
import xdr.Xdr.WriteArgs;

/**
 *
 * @author diptam
 */
public interface NfsInterface extends Remote {
    public AttrStat nfsProcGetAttr(FHandle fhandle) throws RemoteException;;
    public AttrStat nfsProcSetAttr(SetAttrArgs setattrargs) throws RemoteException;;
    public ReadRes nfsProcRead(ReadArgs readargs) throws RemoteException;;
    public AttrStat nfsProcWrite(WriteArgs writeargs) throws RemoteException;;
    public ReadDirRes nfsProcReadDir(ReadDirArgs readdirargs) throws RemoteException;;
    public DirOpRes nfsProcMakeDir(CreateArgs createargs) throws RemoteException;;
    public DirOpRes nfsProcCreate(CreateArgs createargs) throws RemoteException;;
    public boolean mntProcMnt(String ip, String remoteDirName, String localDirName,
            boolean read, boolean write)
            throws RemoteException;
}
