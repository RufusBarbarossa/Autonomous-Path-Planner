/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.ftp;

import autonomouspathplanner.FileIO;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Utility class for connecting to an FTP server
 * @author Alex
 */
public class FTP {
    public String IP = "0.0.0.0";
    public int port = 21;
    public String user = "anonymous";
    public String pass = "";
    private FTPClient client = null;
    
    /**
     * Creates an FTP class with default hostname and settings
     */
    public FTP()
    {
        
    }
    
    /**
     * Creates an FTP client to connect with the server at <code>hostname</code>
     * using port <code>port_num</code>, username <code>user_name</code> and password <code>password</code>
     * @param hostname the hostname of the server
     * @param port_num the Port number to access the server at
     * @param user_name the username to access the server
     * @param password the password to access the server with
     */
    public FTP(String hostname, int port_num, String user_name, String password)
    {
        changeIP(hostname);
        port = port_num;
        user = user_name;
        pass = password;
    }
    
    /**
     * Creates an FTP client to connect with the server at <code>hostname</code>
     * using port 21, username <code>user_name</code> and password <code>password</code>
     * @param hostname the hostname of the server
     * @param user_name the username to access the server
     * @param password the password to access the server with
     */
    public FTP(String hostname, String user_name, String password)
    {
        changeIP(hostname);
        port = 21;
        user = user_name;
        pass = password;
    }
    
    /**
     * Downloads a file from the server
     * @param downloadFile the location to download to
     * @param initialFile the file location on the server
     * @return the File that was downloaded, null if operation fails
     */
    public File downloadFromServer(File downloadFile, String initialFile)
    {
        
        try {
            if(client == null)
            {
                if(!connectToServer()) return null;
            }
            
            BufferedOutputStream st = new BufferedOutputStream(new FileOutputStream(downloadFile));
            boolean sc = client.retrieveFile(initialFile, st);
            if(sc){ System.out.println("Download Completed Successfully"); }
            else System.out.println("Download Failed");
            st.close();
            
        } catch (IOException ex) {
            Logger.getLogger(FTP.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                    client = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return downloadFile;
    }
    
    /**
     * Attempts to connect to the server
     * @return true if connection is established, and false if the connection is failed
     */
    public boolean connectToServer()
    {
        client = new FTPClient();
        try {
            client.connect(IP, port);
            int replyCode = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Operation failed. Server reply code: " + replyCode);
                return false;
            }
            boolean success = client.login(user, pass);
            if (!success) {
                System.out.println("Could not login to the server");
                return false;
            } else {
                System.out.println("LOGGED IN SERVER");
            }
            
            client.enterLocalPassiveMode();
            client.setFileType(FTPClient.BINARY_FILE_TYPE); //We need to check what type to do
        }
        catch(IOException ex)
        {
            return false;
        }
        return true;
    }
    
    /**
     * Uploads a file to the server
     * @param locationToUpload the remote location on the server to upload the file
     * @param f the file on the computer to upload
     * @return true if operation completes successfully, false otherwise
     */
    public boolean uploadToServer(String locationToUpload, File f)
    {
        try {
            if(client == null)
            {
                if(!connectToServer()) return false;
            }
            
            OutputStream s = client.storeFileStream(locationToUpload);
            
            BufferedWriter buff = new BufferedWriter(new OutputStreamWriter(s));
            
            for(String str : FileIO.getAllLines(f))
            {
                buff.write(str);
                buff.newLine();
            }
            
            buff.close();
        } catch (IOException ex) {
            Logger.getLogger(FTP.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                    client = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
    
    /**
     * Attempts to connect to server to check if it is possible
     * @return true if the client can connect to the server and false otherwise.
     */
    public boolean canConnect()
    {
        try{
            FTPClient c = new FTPClient();
            c.connect(IP, port);
            
            c.login(user, pass);
            if(c.isConnected())
            {
                c.logout();
                c.disconnect();
                return true;
            }
            else return false;
        }
        catch(UnknownHostException x)
        {
            return false;
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
        
    }
    
    /**
     * Method to change the hostname to connect to
     * @param newIP the new IP to change to
     * @return true if IP is changed successfully; false otherwise
     */
    public boolean changeIP(String newIP)
    {
        int i = 0;
        
        //UNSURE IF WE WANT TO CHECK FOR RAW IP
        /*for(int j = 0; j<4; j++)
        {
            String s = "";
            while(newIP.charAt(i) != '.')
            {
                s.concat("" + newIP.charAt(i));
                i++;
            }
            try{
                int n = Integer.parseInt(s);
                if(n<0 || n>255)
                {
                return false;
                }
            }
            catch(NumberFormatException ex)
            {
                return false;
            }
        }*/
        IP = newIP;
        return true;
    }
}
