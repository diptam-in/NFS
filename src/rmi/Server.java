/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diptam
 */
public class Server<T extends Remote> {
    Registry register;
    private T t;
    String ip;
    String key;
    int port= Registry.REGISTRY_PORT;
    
    public Server(String ip, String key){
        this.ip=ip;
        this.key=key;
        try {
            register = LocateRegistry.createRegistry(port);
        } catch (RemoteException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE,ex.getMessage());
        }
    }
    
    public Server(String ip, String key, int port){
        this.ip=ip;
        this.key=key;
        this.port=port;
        try {
            register = LocateRegistry.createRegistry(port);
        } catch (RemoteException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE,ex.getMessage());
        }
    }
    
    public void listen(Remote objRemote) {
        System.setProperty("java.security.policy","./security.policy");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        System.setProperty("java.rmi.server.hostname", this.ip);

        try {
            t = (T) UnicastRemoteObject.exportObject(objRemote, 
                    this.port);
            
            try {
                register.rebind(key, t);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                        "Registry bind"
                        + " done successfully.");
            } catch (Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Registry "
                        + "bind FAILED.");
                e.printStackTrace();
            }

        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,e.getMessage());
            e.printStackTrace();
        }
    }
}
