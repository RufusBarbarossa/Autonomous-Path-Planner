/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

import autonomouspathplanner.GUI.objects.Command;
import autonomouspathplanner.GUI.objects.PositionCommandGroup;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * The Position Command Group Creator (PCGcreator) class provides a UI for the modification and creation
 * of position command groups
 * @author Alex
 */
public class PCGcreator extends JFrame{
    /**
     * current PCGcreator so that we can access it later
     * *NOTE I DO KNOW THERE ARE BETTER WAYS TO DO THIS I'M TOO LAZY TO ACTUALLY FIX IT*
     */
    public static PCGcreator currentCreator;
    /**
     * The canvas that is the parent to the PositionCommandGroup
     */
    public AutoPathCanvas canv;
    
    /**
     * The index where the PositionCommandGroup is to be placed
     */
    private int loc;
    /**
     * Stop robot during execution?
     */
    private JCheckBox stop;
    /**
     * ScrollPane for the commands
     */
    private JScrollPane scroller;
    /**
     * Button to add a command
     */
    private JButton addCmd;
    /**
     * Save Button
     */
    private JButton save;
    
    /**
     * the commands that are to exist in the PositionCommandGroup
     */
    private ArrayList<Command> cmds = new ArrayList<>();
    
    /**
     * Create a new PositionCommandGroup at the index location
     * @param location the index where it is to be added
     */
    public PCGcreator(int location)
    {
        currentCreator = this;
        //setLocation(600,800); //center of the screen
        setTitle("Create new Position Command Group");
        initComponents();
        this.loc = location;
    }
    
    /**
     * Modify an existing PositionCommandGroup at the index location
     * @param location the index where it is to be added
     * @param grp the PositionCommandGroup to be modified
     */
    public PCGcreator(int location, PositionCommandGroup grp)
    {
        currentCreator = this;
        setTitle("Modify Existing PositionCommandGroup");
        cmds = grp.cmds;
        initComponents();
        stop.setSelected(grp.stop_robot);
        
        this.loc = location;
    }
    
    /**
     * Initialize all the components
     */
    private void initComponents() {
        this.setAlwaysOnTop(true);
        int x = this.getX();
        int y  = this.getY();
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        this.setContentPane(contentPane);
        
        setSize(400,500);
        //setLocation((x-300),(y+300));
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        stop = new JCheckBox("Stop Robot During Execution?");
        scroller  = new JScrollPane();
        save = new JButton("Save Position Command Group");
        addCmd = new JButton("Add New Command");
        addCmd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PCGcreator.currentCreator.saveCommands();
                PCGcreator.currentCreator.cmds.add(new Command());
                PCGcreator.currentCreator.refreshCommands();
            }
        });
        
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PCGcreator.currentCreator.saveCommands();
                System.out.println("" + loc);
                PositionCommandGroup g = (PositionCommandGroup) PCGcreator.currentCreator.canv.objs.get(loc);
                g.cmds = PCGcreator.currentCreator.cmds;
                g.stop_robot = PCGcreator.currentCreator.stop.isSelected();
                PCGcreator.currentCreator.setVisible(false);
                PCGcreator.currentCreator.dispose();
            }
        });
        
        refreshCommands();
    }

    public void run() {
        setVisible(true);
    }

    /**
     * Refresh the command list from the Arraylist <code>cmds</code>
     */
    public void refreshCommands() {
        this.setVisible(false);
        scroller = new JScrollPane();
        JPanel passListPanel = new JPanel();
        BoxLayout box = new BoxLayout(passListPanel, BoxLayout.Y_AXIS);
        passListPanel.setLayout(box);
        passListPanel.setSize(this.getX(),this.getY());
        
        final JPanel headers = new JPanel();
        JLabel label1 = new JLabel("Command");
        JLabel label2 = new JLabel("Param 1");
        JLabel label3 = new JLabel("Param 2");
        JLabel label4 = new JLabel("Conc.");
        
        headers.add(Box.createHorizontalStrut(30));
        headers.add(label1, Box.LEFT_ALIGNMENT);
        headers.add(Box.createHorizontalStrut(42));
        headers.add(label2, Box.LEFT_ALIGNMENT);
        headers.add(Box.createHorizontalStrut(2));
        headers.add(label3, Box.LEFT_ALIGNMENT);
        headers.add(Box.createHorizontalStrut(0));
        headers.add(label4, Box.LEFT_ALIGNMENT);
        
        headers.setPreferredSize(new Dimension(300,30));
        headers.setMaximumSize(new Dimension(300, 30));
        passListPanel.add(headers, Box.LEFT_ALIGNMENT);
        passListPanel.add(Box.createVerticalStrut(0));
        
        for(Command c : cmds){
            final CommandPane b = new CommandPane(c);
            b.setPreferredSize(new Dimension(300,30));
            b.setMaximumSize(new Dimension(300, 30));
            passListPanel.add(b, Box.CENTER_ALIGNMENT);
            passListPanel.add(Box.createVerticalStrut(20));
        }
        scroller = new JScrollPane(passListPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel pane = new JPanel();
        BoxLayout boxes = new BoxLayout(pane, BoxLayout.Y_AXIS);
        pane.setLayout(boxes);
        pane.add(Box.createVerticalStrut(5));        
        pane.add(stop, Box.CENTER_ALIGNMENT);
        pane.add(Box.createVerticalStrut(5));
        pane.add(scroller);
        pane.add(Box.createVerticalStrut(5));
        pane.add(addCmd);
        pane.add(Box.createVerticalStrut(25));
        pane.add(save);
        pane.add(Box.createVerticalStrut(5));
        this.setContentPane(pane);
        this.setVisible(true);
    }
        
    /**
     * save the current commands to the ArrayList <code>cmds</code>
     */
    public void saveCommands()
    {
        cmds = new ArrayList<>();
        for(Component c : ((JPanel)scroller.getViewport().getComponent(0)).getComponents())
        {
            //System.out.println(c.getClass());
            if(c instanceof CommandPane)
            {
                CommandPane cpn = ((CommandPane)c);
                Command cmd = new Command();
                cmd.command_int = cpn.commandList.getSelectedIndex();
                cmd.concurrent_with_next = cpn.withNext.isSelected();
                if(!cpn.param1.getText().equals(""))
                {
                    cmd.param1 = Integer.parseInt(cpn.param1.getText());
                }
                if(!cpn.param2.getText().equals(""))
                {
                    cmd.param2 = Integer.parseInt(cpn.param2.getText());
                }
                cmds.add(cmd);
            }
        }
    }
    
}
