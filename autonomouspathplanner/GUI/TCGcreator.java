/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

import autonomouspathplanner.GUI.objects.Command;
import autonomouspathplanner.GUI.objects.PositionCommandGroup;
import autonomouspathplanner.GUI.objects.TimeCommandGroup;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.Pane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * The Time Command Group Creator (TCGcreator) class provides a UI for the modification and creation
 * of time command groups
 * @author Alex
 */
public class TCGcreator extends JFrame{
    /**
     * current TCGcreator so that we can access it later
     * *NOTE I DO KNOW THERE ARE BETTER WAYS TO DO THIS I'M TOO LAZY TO ACTUALLY FIX IT*
     */
    public static TCGcreator currentCreator;
    /**
     * The canvas that is the parent to the TimeCommandGroup
     */
    public AutoPathCanvas canv;
    /**
     * The index where the TiemCommandGroup is to be placed
     */
    private int loc;
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
     * JPanel for the time controls
     */
    private JPanel timePane;
    /**
     * Increase time by 1 s 
     */
    private JButton plus;
    /**
     * Decrease time by 1 s 
     */
    private JButton minus;
    /**
     * the time when the TimeCommandGroup is to be executed
     */
    private JTextField time;
    /**
     * the commands that are to exist in the PositionCommandGroup
     */
    private ArrayList<Command> cmds = new ArrayList<>();
    
    /**
     * Create a new PositionCommandGroup at the index location
     */
    public TCGcreator()
    {
        this.setAlwaysOnTop(true);
        currentCreator = this;
        //setLocation(600,800); //center of the screen
        setTitle("Create new Time Command Group");
        initComponents();
        this.loc = -1;
    }
    
    /**
     * Modify an existing PositionCommandGroup at the index location
     * @param location the index where it is to be added
     * @param grp the PositionCommandGroup to be modified
     */
    public TCGcreator(int location, TimeCommandGroup grp)
    {
        currentCreator = this;
        setTitle("Modify Existing TimeCommandGroup");
        cmds = grp.cmds;
        initComponents();
        //stop.setSelected(grp.stop_robot);
        
        
        time.setText(roundToTwo(grp.time));
        this.loc = location;
    }
    
    /**
     * Round the double d to 2 decimal places
     * @param d the double that needs to be rounded
     * @return the string representation of the double after rounding
     */
    private String roundToTwo(double d)
    {
        String realTime = (d+"");
        if((realTime.length()>4 && realTime.charAt(1) == '.'))
        {
            realTime = realTime.substring(0,4);
        }
        else if((realTime.length()>5 && realTime.charAt(2) == '.'))
        {
            realTime = realTime.substring(0,5);
        }
        return realTime;
    }
    
    /**
     * Initialize the components of the creator
     */
    private void initComponents() {
        int x = this.getX();
        int y  = this.getY();
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        this.setContentPane(contentPane);
        
        setSize(400,500);
        //setLocation((x-300),(y+300));
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        timePane = new JPanel();
        BoxLayout b = new BoxLayout(timePane, BoxLayout.X_AXIS);
        JLabel lbl = new JLabel("Time?");
        time = new JTextField("0");
        time.setMaximumSize(new Dimension(50,25));
        time.setPreferredSize(new Dimension(50,25));
        time.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    double i = Double.parseDouble(time.getText());
                    if(i<0)
                    {
                        time.setText("0");
                    }
                    time.setText(roundToTwo(i));
                } 
                catch(NumberFormatException ex){
                    time.setText("0");
                }
            }
        });
        plus = new JButton("+");
        plus.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                double i = Double.parseDouble(time.getText()) + 1;
                time.setText("" + i);
            }
        });
        minus = new JButton("-");
        minus.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                double i = Double.parseDouble(time.getText()) - 1;
                if(i<0)
                {
                    i++;
                }
                time.setText("" + i);
            }
        });
        timePane.add(Box.createHorizontalStrut(50));
        timePane.add(lbl);
        timePane.add(time);
        timePane.add(plus);
        timePane.add(minus);
        timePane.setMaximumSize(new Dimension(300,35));
        timePane.setPreferredSize(new Dimension(300,35));
        
        //stop = new JCheckBox("Stop Robot During Execution?");
        scroller  = new JScrollPane();
        save = new JButton("Save Position Command Group");
        addCmd = new JButton("Add New Command");
        addCmd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TCGcreator.currentCreator.saveCommands();
                TCGcreator.currentCreator.cmds.add(new Command());
                TCGcreator.currentCreator.refreshCommands();
            }
        });
        
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TCGcreator.currentCreator.saveCommands();
                System.out.println("" + loc);
                TimeCommandGroup g;
                if(loc != -1)
                {
                    g = (TimeCommandGroup) canv.objs.get(loc);
                }
                else
                {
                    g = new TimeCommandGroup();
                }
                g.cmds = cmds;
                g.stop_robot = false;//stop.isSelected();
                g.time = Double.parseDouble(roundToTwo(Double.parseDouble(time.getText())));
                double t = (double)g.time;
                Point p = new Point(((int)((t/16)*(canv.timeline_f.x-canv.timeline_0.x)) + canv.timeline_0.x),canv.timeline_0.y);
                g.pos = new Point(p);
                
                if(loc == -1)
                {
                    canv.objs.add(g);
                }
                canv.repaint();
                TCGcreator.currentCreator.setVisible(false);
                TCGcreator.currentCreator.dispose();
            }
        });
        
        refreshCommands();
    }

    public void run() {
        setVisible(true);
    }

    /**
     * Reload the commands displayed based on the Arraylist <code>cmds</code>
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
        pane.add(timePane, Box.CENTER_ALIGNMENT);
        pane.add(Box.createVerticalStrut(5));        
        //pane.add(stop, Box.CENTER_ALIGNMENT);
        //pane.add(Box.createVerticalStrut(5));
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
     * save the current displayed commands to the arrayList <code>cmds</code>
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
    
    public static void main(String[] args)
    {
        TCGcreator ctr = new TCGcreator();
        ctr.setVisible(true);
    }
    
}
