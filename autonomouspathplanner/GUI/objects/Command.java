/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI.objects;

/**
 * This is a data storage class for Commands. Commands are things that the robot does.
 * @author Alex
 */
public class Command{
    public static final int BOGUS_COMMAND_1 = 0;
    public static final int BOGUS_COMMAND_2 = 1;
    public static final int BOGUS_COMMAND_3 = 2;
    
    public static final String[] cmdOptions = {"Bogus Command 1", "Bogus Command 2", "Bogus Command 3"};

    /**
     * parse a command from a line of format "*Command_int*;*param1*;*param2*;*concurrent*"
     * @param line the line from which to parse the command
     * @return the command
     */
    public static Command parseCommand(String line) {
        char[] chs = line.toCharArray();
        Command c = new Command();
        int index = 0;
        while(chs[index] != ';')
        {
            index ++;
        }
        c.command_int = Integer.parseInt(line.substring(0,index));
        index ++;
        int index2 = index;
        while(chs[index2] != ';')
        {
            index2 ++;
        }
        c.param1 = Integer.parseInt(line.substring(index, index2));
        index2 ++;
        index = index2;
        while(chs[index2] != ';')
        {
            index2++;
        }
        c.param2 = Integer.parseInt(line.substring(index, index2));
        c.concurrent_with_next = Boolean.parseBoolean(line.substring(index2 +1));
        return c;
    }
    
    public int command_int;
    public boolean concurrent_with_next;
    public int param1;
    public int param2;
    
    
}
