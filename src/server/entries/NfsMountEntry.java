/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.entries;

import java.util.Objects;

/**
 *
 * @author diptam
 */
public class NfsMountEntry {
    private String clientIp=null;
    private boolean read =true;
    private boolean write =true;
    private String remoteDir=null;
    private String localDir=null;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
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

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    @Override
    public String toString() {
        return "NfsMountEntry{" + "clientIp=" + clientIp + ", read=" + read + ", write=" + write + ", remoteDir=" + remoteDir + ", localDir=" + localDir + '}';
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }
}
