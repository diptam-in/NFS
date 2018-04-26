/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author diptam
 */
public class FileAttributes {
    private boolean isExecutable;
    private boolean isReadable;
    private boolean isWritable;
    private String type;
    private long size;

    public void setIsExecutable(boolean isExecutable) {
        this.isExecutable = isExecutable;
    }

    public void setIsReadable(boolean isReadable) {
        this.isReadable = isReadable;
    }

    public void setIsWritable(boolean isWritable) {
        this.isWritable = isWritable;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isIsExecutable() {
        return isExecutable;
    }

    public boolean isIsReadable() {
        return isReadable;
    }

    public boolean isIsWritable() {
        return isWritable;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }
    
}
