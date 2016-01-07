/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

import autonomouspathplanner.FileIO;
import autonomouspathplanner.GUI.objects.Command;
import autonomouspathplanner.GUI.objects.GUIObject;
import autonomouspathplanner.GUI.objects.PositionCommandGroup;
import autonomouspathplanner.GUI.objects.SmoothLine;
import autonomouspathplanner.GUI.objects.StraightLine;
import autonomouspathplanner.GUI.objects.TimeCommandGroup;
import autonomouspathplanner.PositionUtil;
import autonomouspathplanner.calc.AutonomousPathCreator;
import autonomouspathplanner.calc.SpeedStorage;
import autonomouspathplanner.constants;
import autonomouspathplanner.ftp.FTP;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Alex
 */
public class AutoPathFrame extends javax.swing.JFrame {
    /**
     * firstLine is the first line in the save files of plays. This allows the program to ascertain that the file it is reading is a save file 
     * and not another file.
     */
    protected final String firstLine = constants.firstLine;
    
    /**
     * The Save location for the program
     */
    protected File saveFolder;
    
    /**
     * Where to save on the robot
     */
    protected String roboRIOSaveFolder = constants.roboRIOSaveFolder;
    
    /**
     * A ArrayList of play arrays. each play array is equivalent to the the objs array in the <code>AutoPathCanvas</code> class.
     */
    protected ArrayList<ArrayList<GUIObject>> plays;
    
    /**
     * ArrayList of play titles. Each index corresponds to the index of the ArrayList<GUIObject> in the plays ArrayList
     */
    protected ArrayList<String> titles;
    
    /**
     * configuration settings for creating the trajectory
     */
    protected autonomouspathplanner.calc.TrajectoryGenerator.Config cnfg;
    
    /**
     * The wheelbase of the robot
     */
    protected double robot_wheel_base;
    
    /**
     * The FTP server that the robot is creating. Basically this allows you to communicate to the 
     */
    protected FTP f;
    /**
     * Creates new form AutoPathFrame
     */
    public AutoPathFrame() {
        
        initComponents();
        saveFolder = new File("C:\\Users\\Alex\\Desktop\\Plays");
        cnfg = new autonomouspathplanner.calc.TrajectoryGenerator.Config();
        cnfg.dt = constants.default_robot_dt;
        cnfg.max_acc = constants.default_robot_max_acc / PositionUtil.meters_per_tic;
        cnfg.max_vel = constants.default_robot_max_vel / PositionUtil.meters_per_tic;
        cnfg.max_jerk = constants.default_robot_max_jerk / PositionUtil.meters_per_tic;
        robot_wheel_base = constants.default_robot_wheelbase / PositionUtil.meters_per_tic;
        try {
            f = findFTP();
            if(f == null)
            {
                ((connectionIndicator)canvas2).setConnected(false);
                this.send.setEnabled(false);
            }
            else
            {
                ((connectionIndicator)canvas2).setConnected(true);
                this.send.setEnabled(true);
            }
        } catch (IOException ex) {
            Logger.getLogger(AutoPathFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        reload();
        //FoldSelect sl = new FoldSelect(saveFolder, this);
        //sl.setVisible(true);
    }

    /**
     * Searches the network for FTP servers with the roboRIO format for the hostname
     * @return the FTP object corresponding to the RoboRIO; or NULL if there was nothing found
     * @throws IOException if an exception occurs
     */
    public FTP findFTP() throws IOException
    {
        GenericProgress pro = new GenericProgress();
        
        pro.changeProgress("Searching IP Addresses", 0);
        pro.setVisible(true);
        ArrayList<String> ips = new ArrayList<>();
        Process p = Runtime.getRuntime().exec("arp -a");
        BufferedReader rd = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
        ArrayList<String> lst = new ArrayList<>();
            String line = null;
            double total = 0;
            while ((line = rd.readLine()) != null) {
                
                if(line.startsWith("Interface: "))
                {
                    lst.add(line);
                    total++;
                }
            }
        double  ind = 0;
        for (Iterator<String> it = lst.iterator(); it.hasNext();) {
            line = it.next();
            double progress = 15 + 75*(ind/total);
            double progress2 = 15 + 75*(ind +1)/total;
            pro.changeProgress("Creating hostname for " + line.substring(11, line.length()-8), progress);
            System.out.println(line);
            System.out.println(line.substring(11, line.length()-8));
            String ip = line.substring(11, line.length()-8);
            String s = "roboRIO-";
            if(!ip.startsWith("10."))
            {
                System.out.println("Banana");
                break;
            }
            int index = 3;
            while(ip.charAt(index) != '.')
            {
                index ++;
            }
            System.out.println(ip.substring(3,index));
            s = s.concat(ip.substring(3,index));
            int index2 = index+1;
            while(ip.charAt(index2) != '.')
            {
                index2 ++;
            }
            s=s.concat(ip.substring(index+1, index2));
            s=s.concat("-FRC.local");
            
            pro.changeProgress("Checking Hostname: " + s, (progress2-progress)/4 + progress);
            FTP f = new FTP(s,"anonymous", "");
            if(f.canConnect())
            {
                pro.changeProgress("FTP Server found at hostname " + s, 100);
                //pro.dispose();
                return f;
            }
        }
        pro.changeProgress("No FTP Servers on Network", 100);
        return null;
    }
    
    /**
     * NONGENERAL XML to CSV converter that converts the XML version of a play to a CSV version of the play
     * The CSV version is a pure motion profile and does not contain any information about command groups
     * @param f the XML file to be converted
     * @return the CSV version of the play
     */
    public File XML_to_CSV(File f)
    {
        File f2 = new File(f.toString().substring(0,f.toString().length() - 3) + "csv");
        System.out.println(f2.toString());
        System.out.println(f.toString());
        FileWriter write = null;
        try {
            ArrayList<String> lst = new ArrayList<>();
            try {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                } catch (FileNotFoundException ex) {
                    
                }
                
                //Construct BufferedReader from InputStreamReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                
                String line = null;
                while ((line = br.readLine()) != null) {
                    lst.add(line);
                }
                
                
                br.close();
                
                
            } catch (IOException ex) {
            }
            ArrayList<String> str = new ArrayList<>();
            str.add("Point #,Left Speed,RightSpeed,Left Distance,Right Distance,Heading");
            int index = 0;
            for(int i = 2; i<lst.size(); i++)
            {
                String s = lst.get(i);
                if(s.equals("<Velocity_Profile>") || s.equals("</Velocity_Profile>"))
                {
                    continue;
                }
                if(s.equals("<POSITION_Command_Group>"))
                {
                    i = Integer.MAX_VALUE;
                    break;
                }
                if(s.equals("<Intermediate_Heading>"))
                {
                    i+=2;
                    continue;
                }
                if(s.equals("<Point>"))
                {
                    index++;
                    i+=2;
                    String thing = index + "";
                    String temp = lst.get(i);
                    while(!temp.equals("<Point>") && !temp.equals("</Velocity_Profile>"))
                    {
                        
                        thing+=",";
                        thing+=temp;
                        
                        i+=3;
                        temp = lst.get(i);
                        
                    }
                    str.add(thing);
                    //System.out.println(thing);
                    i--;
                    continue;
                }
                else
                {
                    System.out.println(s);
                }
            }
            write = new FileWriter(f2, false);
            BufferedWriter out = new BufferedWriter(write);
            for(String s : str)
            {
                //System.out.println(s);
                out.write(s);
                out.newLine();
            }
            out.close();
           
            
        } catch (IOException ex) {
            
        } finally {
            try {
                write.close();
            } catch (IOException ex) {
            }
        }

        
        return f2;
    }
    
    /**
     * researches the save folder for plays. 
     */
    public void reload()
    {
        plays = new ArrayList<>();
        titles = new ArrayList<>();
        for(File f : saveFolder.listFiles())
        {
            String[] ray = autonomouspathplanner.FileIO.getAllLines(f);
            if(ray[0].equals(firstLine)){
                plays.add(AutoPathCanvas.getObjs(f));
                titles.add(autonomouspathplanner.FileIO.getTitle(f));
            }
        }
        
        if(plays.isEmpty())
        {
            addNew();
        }
        else
        {
            Play.removeAllItems();
            for(String s : titles)
            {
                Play.addItem(s);
            }
            Play.setSelectedIndex(0);
            ((AutoPathCanvas)canvas1).objs = plays.get(0);
            ((AutoPathCanvas)canvas1).playTitle = titles.get(0);
            canvas1.repaint();
        }
    }
    
    /**
     * Saves the play represented by the object array in the plays arraylist at the index <code>index</code>
     * @param index the index of the object array representing the play to be saved
     */
    public void savePlay(int index)
    {
        File fl = new File(saveFolder.getPath() + java.io.File.separator + titles.get(index) + ".txt");
        ArrayList<String> lines = new ArrayList<>();
        lines.add(firstLine);
        for(int j = 4; j<plays.get(index).size(); j++)
        {
            GUIObject obj = plays.get(index).get(j);
            if(obj instanceof StraightLine)
            {
                lines.add("<STRAIGHT LINE>");
                lines.add("(" + ((StraightLine)obj).initialPosition.x + "," + ((StraightLine)obj).initialPosition.y + ")");
                lines.add("(" + ((StraightLine)obj).finalPosition.x + "," + ((StraightLine)obj).finalPosition.y + ")");
                lines.add("" + ((StraightLine)obj).time);
                lines.add("</STRAIGHT LINE>");
            }
            else if(obj instanceof PositionCommandGroup)
            {
                lines.add("<POSITION COMMAND GROUP>");
                lines.add("(" + ((PositionCommandGroup)obj).pos.x + "," + ((PositionCommandGroup)obj).pos.y + ")");
                lines.add(((PositionCommandGroup)obj).stop_robot + "");
                lines.add("<commands>");
                for(Command c : ((PositionCommandGroup)obj).cmds)
                {
                    lines.add(c.command_int + ";" + c.param1 + ";" + c.param2 + ";" + c.concurrent_with_next);
                }
                lines.add("</commands>");
                lines.add("</POSITION COMMAND GROUP>");
            }
            else if(obj instanceof TimeCommandGroup)
            {
                lines.add("<TIME COMMAND GROUP>");
                lines.add("" + ((TimeCommandGroup)obj).time);
                lines.add(((TimeCommandGroup)obj).stop_robot + "");
                lines.add("<commands>");
                for(Command c : ((TimeCommandGroup)obj).cmds)
                {
                    lines.add(c.command_int + ";" + c.param1 + ";" + c.param2 + ";" + c.concurrent_with_next);
                }
                lines.add("</commands>");
                lines.add("</TIME COMMAND GROUP>");
            }
            else if(obj instanceof SmoothLine)
            {
                lines.add("<SMOOTH_LINE>");
                for(int i = 0; i<((SmoothLine)obj).coordinates.size(); i++)
                {
                    lines.add("(" + ((SmoothLine)obj).coordinates.get(i).x + "," + ((SmoothLine)obj).coordinates.get(i).y + ")");
                }
                lines.add("" + ((SmoothLine)obj).maxTime);
                
                lines.add("</SMOOTH_LINE>");
            }
        }
        try{
        autonomouspathplanner.FileIO.overwrite(fl, lines);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Play = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        canvas1 = new AutoPathCanvas();
        send = new javax.swing.JButton();
        canvas2 = new connectionIndicator();
        uploadMethod = new javax.swing.JComboBox<>();
        MenuBar = new javax.swing.JMenuBar();
        File = new javax.swing.JMenu();
        newPlay = new javax.swing.JMenuItem();
        download = new javax.swing.JMenuItem();
        savePlay = new javax.swing.JMenuItem();
        saveAll = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        close = new javax.swing.JMenuItem();
        params = new javax.swing.JMenu();
        changeFold = new javax.swing.JMenuItem();
        changeParams = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        changeRIO = new javax.swing.JMenuItem();
        connectionCheck = new javax.swing.JMenuItem();
        connectionOption = new javax.swing.JMenuItem();
        refresh = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        changeView = new javax.swing.JMenuItem();
        generate = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Play.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "New Play" }));
        Play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlayActionPerformed(evt);
            }
        });

        jLabel1.setText("Play Selected");

        send.setText("Send Plays to Robot");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });

        uploadMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "XML", "CSV" }));
        uploadMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadMethodActionPerformed(evt);
            }
        });

        File.setText("File");

        newPlay.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newPlay.setText("New Play");
        newPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPlayActionPerformed(evt);
            }
        });
        File.add(newPlay);

        download.setText("Download Plays");
        File.add(download);

        savePlay.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        savePlay.setText("Save");
        savePlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePlayActionPerformed(evt);
            }
        });
        File.add(savePlay);

        saveAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAll.setText("Save All Plays");
        saveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllActionPerformed(evt);
            }
        });
        File.add(saveAll);
        File.add(jSeparator1);

        close.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        close.setText("Close");
        close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        File.add(close);

        MenuBar.add(File);

        params.setText("Edit");

        changeFold.setText("Change Save Folder");
        changeFold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeFoldActionPerformed(evt);
            }
        });
        params.add(changeFold);

        changeParams.setText("Change Robot Params");
        changeParams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeParamsActionPerformed(evt);
            }
        });
        params.add(changeParams);

        MenuBar.add(params);

        jMenu1.setText("Connections");

        changeRIO.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        changeRIO.setText("Change roboRIO");
        changeRIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeRIOActionPerformed(evt);
            }
        });
        jMenu1.add(changeRIO);

        connectionCheck.setText("Check Connection");
        connectionCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionCheckActionPerformed(evt);
            }
        });
        jMenu1.add(connectionCheck);

        connectionOption.setText("Connection Options");
        connectionOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionOptionActionPerformed(evt);
            }
        });
        jMenu1.add(connectionOption);

        MenuBar.add(jMenu1);

        refresh.setText("View");

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setText("Refresh Folder");
        refresh.add(jMenuItem7);

        changeView.setText("Change View Type");
        changeView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeViewActionPerformed(evt);
            }
        });
        refresh.add(changeView);

        generate.setText("View Generated Path");
        generate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateActionPerformed(evt);
            }
        });
        refresh.add(generate);

        MenuBar.add(refresh);

        setJMenuBar(MenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(canvas1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(25, 25, 25))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(uploadMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(670, 670, 670)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Play, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(217, 217, 217)
                        .addComponent(canvas2, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60)
                        .addComponent(send)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Play, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(uploadMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)))
                .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, 771, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(send, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                        .addGap(28, 28, 28))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(canvas2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * changes the folder that we are saving to and reading from. Does NOT refresh the folder.
     * @param evt 
     */
    private void changeFoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeFoldActionPerformed
        FoldSelect sl = new FoldSelect(saveFolder, this);
        sl.setVisible(true);
    }//GEN-LAST:event_changeFoldActionPerformed

    /**
     * Changes the hostname to the one specified by the user. WILL NOT CHANGE THE hostname IF THE HOSTNAME IS INVALID
     * @param evt 
     */
    private void changeRIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeRIOActionPerformed
        String initial = "";
        if(f != null) initial = f.IP;
        String ip = JOptionPane.showInputDialog("Please enter the hostname or IP of the robot", initial);
        String s = ip;
        if(ip.startsWith("10."))
        {
            s = "roboRIO-";
            
            int index = 3;
            while(ip.charAt(index) != '.')
            {
                index ++;
            }
            System.out.println(ip.substring(3,index));
            s = s.concat(ip.substring(3,index));
            int index2 = index+1;
            while(ip.charAt(index2) != '.')
            {
                index2 ++;
            }
            s=s.concat(ip.substring(index+1, index2));
            s=s.concat("-FRC.local");
        }
        
        FTP ft = new FTP(s, 21, "anonymous", "");
        
        //if(ft.canConnect())
        //{
            f = ft;
            ((connectionIndicator)canvas2).setConnected(true);
            this.send.setEnabled(true);
        //}
        //else
        //{
        //    JOptionPane.showMessageDialog(this, "ERROR: Could not connect to: " + s + "\nHostname not changed", "CONNECTION ERROR", JOptionPane.ERROR_MESSAGE);
        //}
    }//GEN-LAST:event_changeRIOActionPerformed

    /**
     * NOTHING. IGNORE IT
     * @param evt 
     */
    private void changeViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeViewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_changeViewActionPerformed

    /**
     * Saves every single play in the play that is in this folder.
     * @param evt 
     */
    private void saveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllActionPerformed
        for(int i = 0; i<Play.getItemCount(); i++)
        {
            savePlay(i);
        }
    }//GEN-LAST:event_saveAllActionPerformed

    /**
     * Writes the XML or CSV files and sends them to the robot that is connected through
     * the "FTP" object
     * @param evt 
     */
    private void sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendActionPerformed
        GenericProgress pro = new GenericProgress();
        pro.changeProgress("Beginning File Creation", 0);
        pro.setVisible(true);
        File temp = new File(System.getenv("TMP"));
        System.out.println(temp.toString());
        long l = System.currentTimeMillis();
        double size = plays.size();
        double incAmt = 75/size;
        for(ArrayList<GUIObject> objs : plays)
        {
            double progress = plays.indexOf(objs)*incAmt;
            
            SpeedStorage s = AutonomousPathCreator.createPath(objs, cnfg, robot_wheel_base);
            int[] pgrploc = AutonomousPathCreator.getPCmDLocation(objs, s);
            ArrayList<String> lines = new ArrayList<String>();
            lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            
            lines.add("<Play>");
            int i = 1;
            
            
            double totalCmdGrps = 0;
            for(int j = 4; j<objs.size(); j++)
            {
                if(objs.get(j) instanceof PositionCommandGroup || objs.get(j) instanceof TimeCommandGroup)
                {
                    totalCmdGrps ++;
                }
            }
            double inc = incAmt/((double)(s.left_track_speeds.size() + totalCmdGrps*20));
            pro.changeProgress("Adding Speeds for Play " + titles.get(plays.indexOf(objs)), inc*i + progress);
            double lpos = 0;
            double rpos = 0;
            while(i<s.left_track_speeds.size())
            {
                lines.add("<Velocity_Profile>");
                
                pro.setProgressBar(inc*i + progress);
                lines.add("<Point>");
                lines.add("<Left_Speed>");
                lines.add("0.0");
                lines.add("</Left_Speed>");
                lines.add("<Right_Speed>");
                lines.add("0.0");
                lines.add("</Right_Speed>");
                lines.add("<Left_Distance>");
                lines.add(lpos + "");
                lines.add("</Left_Distance>");
                lines.add("<Right_Distance>");
                lines.add(rpos + "");
                lines.add("</Right_Distance>");
                lines.add("<Next_Heading>");
                lines.add("" + s.headings.get(1));
                lines.add("</Next_Heading>");
                lines.add("</Point>");
                while((s.left_track_speeds.get(i) != 0 || s.right_track_speeds.get(i) != 0))
                {
                    lpos += s.left_track_speeds.get(i)*PositionUtil.meters_per_tic * cnfg.dt;
                    rpos += s.right_track_speeds.get(i)*PositionUtil.meters_per_tic * cnfg.dt;
                    
                    lines.add("<Point>");
                    lines.add("<Left_Speed>");
                    lines.add(s.left_track_speeds.get(i)*PositionUtil.meters_per_tic + "" );
                    lines.add("</Left_Speed>");
                    lines.add("<Right_Speed>");
                    lines.add(s.right_track_speeds.get(i)*PositionUtil.meters_per_tic + "" );
                    lines.add("</Right_Speed>");
                    lines.add("<Left_Distance>");
                    lines.add(lpos + "");
                    lines.add("</Left_Distance>");
                    lines.add("<Right_Distance>");
                    lines.add(rpos + "");
                    lines.add("</Right_Distance>");
                    lines.add("<Next_Heading>");
                    if(i == s.left_track_speeds.size()-1)
                    {
                        lines.add("" + s.headings.get(i));
                    }
                    else
                    {
                        lines.add("" + s.headings.get(i+1));
                    }
                    
                    lines.add("</Next_Heading>");
                    lines.add("</Point>");
                    
                    i++;
                    if(i == s.left_track_speeds.size())
                    {
                        break;
                    }
                }
                lines.add("<Point>");
                lines.add("<Left_Speed>");
                lines.add("0.0");
                lines.add("</Left_Speed>");
                lines.add("<Right_Speed>");
                lines.add("0.0");
                lines.add("</Right_Speed>");
                lines.add("<Left_Distance>");
                lines.add(lpos + "");
                lines.add("</Left_Distance>");
                lines.add("<Right_Distance>");
                lines.add(rpos + "");
                lines.add("</Right_Distance>");
                lines.add("<Next_Heading>");
                if(i == s.left_track_speeds.size()-1)
                {
                    lines.add("" + s.headings.get(i));
                }
                else
                {
                    lines.add("" + s.headings.get(i+1));
                }
                lines.add("</Next_Heading>");
                lines.add("</Point>");
                lines.add("</Velocity_Profile>");
                if(i == s.left_track_speeds.size())
                {
                    lines.add("<Intermediate_Heading>");
                    lines.add(s.headings.get(i-1) + "");
                    lines.add("</Intermediate_Heading>");
                }
                else
                {
                    lines.add("<Intermediate_Heading>");
                    lines.add(s.headings.get(i) + "");
                    lines.add("</Intermediate_Heading>");
                }
                i++;
                i++;
            }
            
            
            int index = 0;
            pro.changeProgress("Adding PositionCommandGroups for Play " + titles.get(plays.indexOf(objs)), inc*i + progress);
            for(int j = 4; j<objs.size(); j++)
            {
                
                pro.setProgressBar(inc*i + progress);
                i+=20;
                GUIObject g = objs.get(j);
                if(g instanceof PositionCommandGroup)
                {
                    lines.add("<POSITION_Command_Group>");
                    PositionCommandGroup grp = (PositionCommandGroup)g;
                    System.out.println(index);
                    lines.add("<loc>" + pgrploc[index] + "</loc>");
                    lines.add("<Stop>" + grp.stop_robot + "</Stop>");
                    lines.add("<Cmds>");
                    for(Command c : grp.cmds)
                    {
                        lines.add("<CMD_int>" + c.command_int + "</CMD_int>");
                        lines.add("<Param1>" + c.param1 + "</Param1>");
                        lines.add("<Param2>" + c.param2 + "</Param2>");
                        lines.add("<Concurrency>" + c.concurrent_with_next + "</Concurrency>");
                    }
                    lines.add("</Cmds>");
                    index++;
                    lines.add("</POSITION_Command_Group>");
                }
            }
            
            
            pro.changeProgress("Adding TimeCommandGroups for Play " + titles.get(plays.indexOf(objs)), inc*i + progress);
            for(int j = 4; j<objs.size(); j++)
            {
                pro.setProgressBar(inc*i + progress);
                i+=20;
                GUIObject g = objs.get(j);
                if(g instanceof TimeCommandGroup)
                {
                    lines.add("<TIME_Command_Group>");
                    TimeCommandGroup grp = (TimeCommandGroup)g;
                    
                    lines.add("<Time>" + grp.time + "</Time>");
                    lines.add("<Stop>" + grp.stop_robot + "</Stop>");
                    lines.add("<Cmds>");
                    for(Command c : grp.cmds)
                    {
                        lines.add("<CMD_int>" + c.command_int + "</CMD_int>");
                        lines.add("<Param1>" + c.param1 + "</Param1>");
                        lines.add("<Param2>" + c.param2 + "</Param2>");
                        lines.add("<Concurrency>" + c.concurrent_with_next + "</Concurrency>");
                    }
                    lines.add("</Cmds>");
                    lines.add("</TIME_Command_Group>");
                }
            }
            lines.add("</Play>");
            File temp2 = new File(temp.getPath() + "\\" + titles.get(plays.indexOf(objs)) + ".XML");
            try {
                FileIO.overwrite(temp2, lines);
            } catch (IOException ex) {
                Logger.getLogger(AutoPathFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        System.out.println("Time: " + (System.currentTimeMillis() - l));
        
        if(f == null)
        {
            pro.changeProgress("ERR: CONNECTION FAILURE", 100);
        }
        else
        {
            pro.changeProgress("Connecting to RoboRIO", 75);
            if(!f.connectToServer())
            {
            
                pro.changeProgress("ERR: CONNECTION FAILURE", 100);
                return;
            }
            pro.changeProgress("Uploading Plays",80);
            for(double i = 0; i<titles.size(); i++)
            {
                pro.changeProgress("Uploading Play \"" + titles.get((int)i) + "\"", 20*(i/((double)titles.size())) + 80);
                
                String title = titles.get((int)i);
                if(uploadMethod.getSelectedItem().equals("XML"))
                {
                    title = title.concat(".XML");
                }
                else
                {
                    title = title.concat(".CSV");
                    XML_to_CSV(new File(temp.getPath() + "\\" + titles.get((int)i) + ".XML"));
                }
                try{
                    boolean b = f.uploadToServer(roboRIOSaveFolder + title, new File(temp.getPath() + "\\" + title));
                    if(b == false)
                    {
                        pro.changeProgress("ERR: UNSPECIFIED. UPLOAD OF FILE " + title + " FAILED.", 100);
                        return;
                    }
                }
                catch(NullPointerException ex)
                {
                    pro.changeProgress("ERR: UNSPECIFIED. UPLOAD OF FILE " + title + " FAILED.", 100);
                    return;
                }
                
            }
            pro.changeProgress("Plays Uploaded successfully", 100);
        }
    }//GEN-LAST:event_sendActionPerformed

    /**
     * Saves the currently selected play
     * @param evt 
     */
    private void savePlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePlayActionPerformed
        savePlay(Play.getSelectedIndex());
        int i = Play.getSelectedIndex();
        removeAsterisk = true;
        Play.setSelectedIndex(Play.getSelectedIndex());
        
    }//GEN-LAST:event_savePlayActionPerformed
    
    /**
     * When the selected play is changed, then set the current canvas to show 
     * the newly selected play
     */
    public boolean removeAsterisk = false;
    private void PlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayActionPerformed
        if(Play.getItemCount() == 0)
        {
            return;
        }
        int i = Play.getSelectedIndex();
        ((AutoPathCanvas)canvas1).objs = plays.get(Play.getSelectedIndex());
        ((AutoPathCanvas)canvas1).playTitle = titles.get(Play.getSelectedIndex());
        canvas1.repaint();
        
    }//GEN-LAST:event_PlayActionPerformed

    /**
     * Adds a new play. DOES NOT SAVE THIS
     * @param evt 
     */
    private void newPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPlayActionPerformed
       addNew();
    }//GEN-LAST:event_newPlayActionPerformed

    /**
     * Exit the program with no error code
     * @param evt 
     */
    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
        System.exit(0);
    }//GEN-LAST:event_closeActionPerformed

    /**
     * Generate the motion profile so the user can look and be in awe. Or not.
     * @param evt 
     */
    private void generateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateActionPerformed
        SpeedStorage s = AutonomousPathCreator.createPath(plays.get(Play.getSelectedIndex()), cnfg, robot_wheel_base);
        System.out.println("*****KAPPA******");
        double[][] ray = new double[s.robot_points.size()][2];
        double[][] ray2 = new double[s.robot_points.size()][2];
        double[][] ray3 = new double[s.robot_points.size()][2];
        double[][] lspd = new double[s.robot_points.size()][2];
        double[][] rspd = new double[s.robot_points.size()][2];
        for(int i = 0; i<s.robot_points.size(); i++)
        {
            ray[i] = s.robot_points.get(i);
            //System.out.println(ray[i][0] + " " + ray[i][0]);
            //ray2[i] = s.left_track_points.get(i);
            //ray3[i] = s.right_track_points.get(i);
            lspd[i][0] = i*0.02;
            rspd[i][0] = i*0.02;
            lspd[i][1] = s.left_track_speeds.get(i)*PositionUtil.meters_per_tic;
            rspd[i][1] = s.right_track_speeds.get(i)*PositionUtil.meters_per_tic;
        }
        FalconLinePlot posPlot = new FalconLinePlot(PositionUtil.convertPathToMeters(ray), Color.black, null);
        //FalconLinePlot posPlot = new FalconLinePlot(ray, Color.black, null);
        //posPlot.addData(PositionUtil.convertPathToMeters(ray2), Color.red);
        //posPlot.addData(PositionUtil.convertPathToMeters(ray3), Color.red);
        
        FalconLinePlot spdPlot = new FalconLinePlot(lspd, Color.BLUE, null);
        spdPlot.addData(rspd, Color.RED, null);
        
        
        posPlot.xGridOn();
        posPlot.yGridOn();
        posPlot.setTitle("Robot Path");
        posPlot.setYLabel("Y Position (Meters)");
        posPlot.setXLabel("X Position (Meters)");
        spdPlot.xGridOn();
        spdPlot.xGridOn();
        spdPlot.setXLabel("Time (seconds)");
        spdPlot.setYLabel("Speed (meters/second");
        spdPlot.setTitle("Robot Speed (Blue is left wheel, Red is Right wheel)");
    }//GEN-LAST:event_generateActionPerformed

    /**
     * Change the parameters of the robot
     * @param evt 
     */
    private void changeParamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeParamsActionPerformed
        setConfig st = new setConfig(cnfg, robot_wheel_base, this);
        st.setVisible(true);
    }//GEN-LAST:event_changeParamsActionPerformed

    /**
     * Rescan the network for a new FTP
     * @param evt 
     */
    private void connectionCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionCheckActionPerformed
        try {
            f = findFTP();
            if(f == null)
            {
                ((connectionIndicator)canvas2).setConnected(false);
                this.send.setEnabled(false);
            }
            else
            {
                ((connectionIndicator)canvas2).setConnected(true);
                this.send.setEnabled(true);
            }
        } catch (IOException ex) {
            Logger.getLogger(AutoPathFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_connectionCheckActionPerformed

    /**
     * Yeah no
     * @param evt 
     */
    private void connectionOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionOptionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_connectionOptionActionPerformed

    /**
     * ignore me
     * @param evt 
     */
    private void uploadMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadMethodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_uploadMethodActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AutoPathFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AutoPathFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AutoPathFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AutoPathFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AutoPathFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu File;
    private javax.swing.JMenuBar MenuBar;
    private javax.swing.JComboBox Play;
    private java.awt.Canvas canvas1;
    private java.awt.Canvas canvas2;
    private javax.swing.JMenuItem changeFold;
    private javax.swing.JMenuItem changeParams;
    private javax.swing.JMenuItem changeRIO;
    private javax.swing.JMenuItem changeView;
    private javax.swing.JMenuItem close;
    private javax.swing.JMenuItem connectionCheck;
    private javax.swing.JMenuItem connectionOption;
    private javax.swing.JMenuItem download;
    private javax.swing.JMenuItem generate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem newPlay;
    private javax.swing.JMenu params;
    private javax.swing.JMenu refresh;
    private javax.swing.JMenuItem saveAll;
    private javax.swing.JMenuItem savePlay;
    private javax.swing.JButton send;
    private javax.swing.JComboBox<String> uploadMethod;
    // End of variables declaration//GEN-END:variables

    private void addNew() {
        String title = JOptionPane.showInputDialog(this, "What is the title of the new play?", "Create New", JOptionPane.QUESTION_MESSAGE);
        plays.add(AutoPathCanvas.startObjs());
        titles.add(title);
        System.out.println(title);
        Play.insertItemAt(title, Play.getItemCount());
        Play.setSelectedIndex(Play.getItemCount()-1);
        ((AutoPathCanvas)canvas1).objs = plays.get(Play.getItemCount()-1);
        ((AutoPathCanvas)canvas1).playTitle = title;
        canvas1.repaint();
        
    }
    
    
}
