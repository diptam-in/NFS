/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author diptam
 */
public class Connection<T> {
    Registry registry;
    private T t;
    private String ip;
    private String service;
    public Connection(String ip, String service){
        this.ip = ip;
        this.service = service;
    }
    public T getStub() throws RemoteException, NotBoundException{
        T stub = null;
        Registry registry = LocateRegistry.getRegistry(this.ip);
        stub = (T) registry.lookup(this.service);
        return stub;
    }
}