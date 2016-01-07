/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bundle of classes that make reading files easier. This is not necessary it simply is useful.
 * @author Alex
 */
public class FileIO {
    
    /**
     * Reads all of the lines in the file and returns the lines as an array of strings where each string is one line
     * This method assumes the default escape character for a new line.
     * @param f the file to be read
     * @return a String array with all of the lines in the program, each string representing one line.
     */
    public static String[] getAllLines(File f)
    {
        try {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            ArrayList<String> lst = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                lst.add(line);
            }
            
            String[] toReturn = new String[lst.size()];
            for(int i = 0; i<lst.size(); i++)
            {
                toReturn[i] = lst.get(i);
            }
            
           
                br.close();
           
            return toReturn;
        } catch (IOException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Overwrites the file <code>fl</code> to contain the ArrayList <code>lines</code>; 
     * where each element of the ArrayList is one line in the new file. If the file does not exist, the method will create it
     * @param fl the file location to write the lines to
     * @param lines the content the file is to have, each element representing one line.
     * @throws IOException if the output fails in any way - for instance if the program does not have write access to <code>fl</code>
     */
    public static void overwrite(File fl, ArrayList<String> lines) throws IOException {
        FileWriter write = new FileWriter(fl, false);
        BufferedWriter out = new BufferedWriter(write);
        for(String s : lines)
        {
            //System.out.println(s);
            out.write(s);
            out.newLine();
        }
        out.close();
    }
    
    /**
     * <a color=RED>WARNING: NOT A GENERALIZED PROGRAM. DO NOT USE UNLESS THE FILE EXTENSION IS EXACTLY THREE CHARACTERS LONG</a><br>
     * Determines the title of the file. The title is defined as what is left once the folder location is stripped and the file extension is also stripped. <br>
     * One example is File "C:/Users/&username&/Documents/foo.bar" whose title would be "foo". 
     * Note that in the current inplementation if the extension would have been something like foo.bars the program would report the title as "foo."
     * @param f the file that you wish to ascertain the title of
     * @return the title of the file
     */
    public static String getTitle(File f)
    {
        String title = f.getName();
        title = title.substring(0, title.length() - 4);
        return title;
    }
}
