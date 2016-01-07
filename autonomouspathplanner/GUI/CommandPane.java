/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

import autonomouspathplanner.GUI.objects.Command;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The CommandPane is a JPanel that represents a single command
 * in a list of commands.
 * @author Alex
 */
class CommandPane extends JPanel{
    /**
     * List of possible commands the robot can execute
     */
    public JComboBox commandList = new JComboBox(Command.cmdOptions);
    /**
     * Will this command be executed simultaneously with the next command?
     */
    public JCheckBox withNext = new JCheckBox();
    /**
     * 1st parameter
     */
    public JTextField param1 = new JTextField();
    /**
     * 1st parameter
     */
    public JTextField param2 = new JTextField();
    
    /**
     * Set up a command pane that matches command c.
     * @param c 
     */
    CommandPane(Command c) {
        BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layout);
        
        
        param1.setSize(new Dimension(40,20));
        
        commandList.setSelectedIndex(c.command_int);
        this.add(commandList, Box.LEFT_ALIGNMENT);
        //this.add(Box.createRigidArea(new Dimension(30,20)));
        this.add(Box.createHorizontalStrut(20));
        
        param1.setText("" + c.param1);
        this.add(param1, Box.LEFT_ALIGNMENT);
        this.add(Box.createHorizontalStrut(20));
        
        param2.setText("" + c.param2);
        this.add(param2, Box.LEFT_ALIGNMENT);
        this.add(Box.createHorizontalStrut(20));
        
        withNext.setSelected(c.concurrent_with_next);
        withNext.setText("");
        this.add(withNext, Box.LEFT_ALIGNMENT);
    }
    
}
