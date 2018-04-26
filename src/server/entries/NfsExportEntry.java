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
public class NfsExportEntry {
    private String dirname=null;
    private boolean readable=false;
    private boolean writable=false;
    private String permission=null;

    public String getDirname() {
        return dirname;
    }

    public void setDirname(String dirname) {
        this.dirname = dirname;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NfsExportEntry other = (NfsExportEntry) obj;
        if (!Objects.equals(this.dirname, other.dirname)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.dirname);
        hash = 41 * hash + (this.readable ? 1 : 0);
        hash = 41 * hash + (this.writable ? 1 : 0);
        hash = 41 * hash + Objects.hashCode(this.permission);
        return hash;
    }

    @Override
    public String toString() {
        return "NfsExportEntry{" + "dirname=" + dirname + ", readable=" + readable + ", writable=" + writable + ", permission=" + permission + '}';
    }
    
    
}
