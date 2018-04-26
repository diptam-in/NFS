/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.entries;

/**
 *
 * @author diptam
 */
public class ClientReqInfo {
    private String ip;
    private String localMountDir;
    private String remoteMountDir;
    private String fileOrDirName;
    private boolean read;
    private boolean write;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLocalMountDir() {
        return localMountDir;
    }

    public void setLocalMountDir(String localMountDir) {
        this.localMountDir = localMountDir;
    }

    public String getRemoteMountDir() {
        return remoteMountDir;
    }

    public void setRemoteMountDir(String remoteMountDir) {
        this.remoteMountDir = remoteMountDir;
    }

    public String getFileOrDirName() {
        return fileOrDirName;
    }

    public void setFileOrDirName(String fileOrDirName) {
        this.fileOrDirName = fileOrDirName;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }
    
    
}
