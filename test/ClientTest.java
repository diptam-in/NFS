
import client.NfsClient;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import misc.Global;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author diptam
 */
public class ClientTest {
    public static void main(String[] args)
    {
        NfsClient nfsClient = new NfsClient();
        String command=args[0];
        
        if(command!=null)
        {
            System.out.println("[Command] "+command);
            if(command.equals("mount"))
            {
                boolean read=false;
                boolean write=false;
                String localDir=args[1];
                String remoteDir=args[2];
                String perms=args[3];
                if(perms.contains("r")) read=true;
                if(perms.contains("w")) write=true;
                boolean status = nfsClient.nfsMount(localDir, remoteDir, read, write);
                if(status)
                    System.out.println("[Mount] Succesfull");
                else System.out.println("[Mount] Failed");
            }
            
            if(command.equals("ls"))
            {
                String localDir=args[1];
                String target=null;
                if(args.length>=3)
                    target=args[2];
            
                ArrayList<String> resp= nfsClient.readDir(localDir,target);
                if(resp.size()>0)
                {
                    for(String file: resp)
                        System.out.println(file);
                    
                }
                else System.out.println("[Read] Failed or Folder is Empty");
            }
            
            if(command.equals("mkdir"))
            {
                String localDir=args[1];
                String target=null;
                if(args.length>=3)
                    target=args[2];
                boolean resp= nfsClient.makeDir(localDir,target);
                if(resp)
                    System.out.println("[Make] Successfull");
                else System.out.println("[Make] Failed");
            }
            
            if(command.equals("touch"))
            {
                String localDir=args[1];
                String target=null;
                if(args.length>=3)
                    target=args[2];
                boolean resp= nfsClient.nfsCreate(localDir,target);
                if(resp)
                    System.out.println("[Create] Successfull");
                else System.out.println("[Create] Failed");
            }
            
            if(command.equals("write"))
            {
                String localDir=args[1];
                String target=null;
                String inputfile=null;
                if(args.length<4)
                {
                    System.err.println("Please provide all information");
                    System.exit(0);
                }
                target=args[2];
                inputfile=args[3];
                
                FileInputStream inputStream=null;
                try {
                    inputStream = new FileInputStream(inputfile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE,ex.getMessage());
                    System.exit(0);
                }
                byte[] buffer = new byte[Global.CHUNK_SIZE];
                try {
                    inputStream.read(buffer);
                } catch (IOException ex) {
                    Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE,ex.getMessage());
                    System.exit(0);
                }
                
                
                boolean resp= nfsClient.nfsWrite(buffer,0,localDir,target);
                if(resp)
                    System.out.println("[Write] Successfull");
                else System.out.println("[Write] Failed");
            }
            
            if(command.equals("read"))
            {
                String localDir=args[1];
                String target=null;
                String inputfile=null;
                if(args.length<3)
                {
                    System.err.println("Please provide all information");
                    System.exit(0);
                }
                target=args[2];
                
                byte[] data=nfsClient.nfsRead(localDir, target, 0);
                if(data!=null)
                    System.out.println(new String(data));
                else System.out.println("[Write] Failed");
            }
        }
    }
}
