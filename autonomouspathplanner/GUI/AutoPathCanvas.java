/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

import autonomouspathplanner.GUI.objects.Command;
import autonomouspathplanner.GUI.objects.GUIObject;
import autonomouspathplanner.GUI.objects.PositionCommandGroup;
import autonomouspathplanner.GUI.objects.SmoothLine;
import autonomouspathplanner.GUI.objects.StraightLine;
import autonomouspathplanner.GUI.objects.TimeCommandGroup;
import autonomouspathplanner.PositionUtil;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * <code>AutoPathCanvas</code> is a class which extends Canvas and serves as the location on which all of the Autonomous Path creation
 * will take place in terms of user interaction. It allows for the addition of paths, the addition of commands groups and the modification
 * of these
 * @author Alex
 */
public class AutoPathCanvas extends Canvas implements MouseMotionListener, MouseListener, KeyListener{
    //CONSTANTS
    
    /**
     * These are constants that specify various constants for use
     * throughout this class.
     */
    public static final int time_loc_interface_y = 550;
    public static final int pallete_left_x = 1200;
    public static final int command_group_size = 50;
    public static final int line_delta = 10;
    public static final int distance_to_line = 50;
    
    public static final Point timeline_0 = new Point(40,675);
    public static final Point timeline_f = new Point(1150, 675);
    
    protected BufferedImage img;
    
    public String playTitle;
    
    /**
     * The objs ArrayList stores all of the <code>GUIObject</code>s that exist within the
     * canvas. The first three indeces are the pallete objects,
     */
    protected ArrayList<GUIObject> objs;
    
    /**
     * This class creates the Canvas and sets up the first three objects in the 
     * <code>objs</code> ArrayList
     */
    public AutoPathCanvas()
    {
        //Call super just to be safe
        super();
        
        initComponents();
    }
    
    /**
     * Sets up the objs array and adds the mouse listener and such.
     */
    public void initComponents()
    {
        objs = startObjs();
        /*
        try {
            img = ImageIO.read(new File("C:\\Users\\Alex\\Desktop\\field.jpg"));
            Image tmp = img.getScaledInstance(pallete_left_x, time_loc_interface_y, Image.SCALE_SMOOTH);
            BufferedImage dimg = new BufferedImage(pallete_left_x, time_loc_interface_y, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = dimg.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();
            img = dimg;
        } catch (IOException ex) {
            //img = null;
            Logger.getLogger(AutoPathCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        //Make sure our background color is correct
        this.setBackground(new Color(238,238,238));
        
        //Really we just want everything drawn from the get-go
        repaint();
        
        
        //Insufficient to just have the mouse stuff implemented we also need to sset this ish up 
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addKeyListener(this);
    }
    
    /**
     * extension of the paint method inherited from java.awt.Canvas
     * This routinely repaints the background elements when called as well as iterating
     * through the <code>objs</code> list and painting each element
     * @param g the graphics element used to paint the things needing painting
     */
    @Override
    public void paint(Graphics g)
    {
        double delta = 1/PositionUtil.meters_per_tic;
        delta = delta/1;
        for(int i = 0; i<pallete_left_x; i+= delta)
        {
            g.drawLine(i, 0, i, time_loc_interface_y);
        }
        
        for(int j = time_loc_interface_y; j>0; j-= delta)
        {
            g.drawLine(0, j, pallete_left_x, j);
        }
        
        //System.out.println("Repainting");
        //Paint the Background Elements first
        //g.drawImage(img, 0, 0, this);
        
        //Titles
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 48));
        g.setColor(Color.black);
        g.drawString("Field Movement", 400, 50);
        g.drawString("Palette", 1300, 50);
        g.drawString("Time Commands", 400, 600);
        
        //The field
        
        
        //Dividers
        g.setColor(Color.RED);
        g.drawLine(pallete_left_x, 0, pallete_left_x, this.getHeight());
        g.drawLine(0, time_loc_interface_y, pallete_left_x, time_loc_interface_y);
        
        //Timeline
        g.setColor(Color.black);
        g.drawLine(timeline_0.x, timeline_0.y, timeline_f.x, timeline_f.y);
        int temp = timeline_f.x - timeline_0.x;
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
        for(double i = 0; i<16; i++)
        {
            int x_pos = (int)((i/16)*temp) + timeline_0.x;// + 15;
            g.drawLine(x_pos, timeline_0.y-10, x_pos, timeline_0.y+10);
            int num = (int)i;
            g.drawString("" + num , x_pos-2, timeline_0.y+14 + g.getFont().getSize());
        }
        int[] xpoints = {timeline_f.x, timeline_f.x-40, timeline_f.x-40, timeline_f.x};
        int[] ypoints = {timeline_f.y, timeline_f.y-15, timeline_f.y+15, timeline_f.y};      
        
        g.fillPolygon(xpoints, ypoints, 4);
        
        //Draw the Objects
        try {
            for(GUIObject o : objs)
            {
                drawGUIObject(o, g);
            }
        } catch (UnknownClassException ex) {
            Logger.getLogger(AutoPathCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //If we are in a creationMode; highlight with an arrow which GUIObject we're creating
        if(creationStatus == 1)
        {
            g.setColor(Color.RED);
            int[] xPoints = {1400,1475,1475,1600,1600,1475,1475,1400};
            int[] yPoints = {150,200,175,175,125,125,100,150};
            g.fillPolygon(xPoints, yPoints, 8);
        }
        else if(creationStatus == 2)
        {
            g.setColor(Color.BLACK);
            int[] xPoints = {1400,1475,1475,1600,1600,1475,1475,1400};
            int[] yPoints = {275,325,300,300,250,250,225,275};
            g.fillPolygon(xPoints, yPoints, 8);
        }
        else if(creationStatus == 3)
        {
            g.setColor(Color.BLUE);
            int[] xPoints = {1400,1475,1475,1600,1600,1475,1475,1400};
            int[] yPoints = {400,450,425,425,375,375,350,400};
            g.fillPolygon(xPoints, yPoints, 8);
        }
    }
    
    //Misc
    /**
     * Method that loads the play specified in the file <code>f</code>
     * @param f the file to load a play from.
     */
    public static ArrayList<GUIObject> getObjs(File f)
    {
        ArrayList<GUIObject> o = startObjs();
        
        String [] lines = autonomouspathplanner.FileIO.getAllLines(f);
        if(lines.length == 1)
        {
            return o;
        }
        int i = 1;
        while(i<lines.length)
        {
            switch(lines[i])
            {
                case "<POSITION COMMAND GROUP>":
                    i++;
                    PositionCommandGroup g = new PositionCommandGroup();
                    g.pos = PositionUtil.parsePoint(lines[i]);
                    i++;
                    g.stop_robot = Boolean.parseBoolean(lines[i]);
                    i += 2;
                    while(!lines[i].equals("</commands>"))
                    {
                        g.cmds.add(Command.parseCommand(lines[i]));
                        i++;
                    }
                    i++;
                    o.add(g);
                    break;
                case "<TIME COMMAND GROUP>":
                    i++;
                    TimeCommandGroup t = new TimeCommandGroup();
                    t.time = Double.parseDouble(lines[i]);
                    Point p = new Point(((int)((t.time/16)*(timeline_f.x-timeline_0.x)) + timeline_0.x),timeline_0.y);
                    t.pos = new Point(p);
                    i++;
                    t.stop_robot = Boolean.parseBoolean(lines[i]);
                    i += 2;
                    while(!lines[i].equals("</commands>"))
                    {
                        t.cmds.add(Command.parseCommand(lines[i]));
                        i++;
                    }
                    i++;
                    o.add(t);
                    break;
                case "<STRAIGHT LINE>":
                    //System.out.println("HIIII");
                    i++;
                    StraightLine sl = new StraightLine();
                    //System.out.println(PositionUtil.parsePoint(lines[i]) + " " + PositionUtil.parsePoint(lines[i+1]) + " " + lines[i + 2]);
                    sl.initialPosition = PositionUtil.parsePoint(lines[i]);
                    i++;
                    sl.finalPosition = PositionUtil.parsePoint(lines[i]);
                    i++;
                    sl.time = Double.parseDouble(lines[i]);
                    o.add(sl);
                    break;
                case "<SMOOTH LINE>":
                    i++;
                    SmoothLine ln = new SmoothLine();
                    while(!lines[i+1].equals("</SMOOTH LINE>"))
                    {
                        ln.coordinates.add(PositionUtil.parsePoint(lines[i]));
                        i++;
                    }
                    ln.maxTime = Double.parseDouble(lines[i]);
                    i++;
                    o.add(ln);
                default:
                    i++;
            }
        }
        return o;
    }
    
    /**
     * lastTime is a variable used by repaintWait to determine when the last repaint was
     */
    private long lastTime;
    
    /**
     * <code>repaintWait</code> is a method that forces the <code>repaint</code> method to only operate
     * at 60fps. Anything higher causes jitteriness and flashes on the screen
     */
    public void repaintWait()
    {
        //Basically if we have gone past the 17 ms required to force it to 60fps, we can repaint the frame. otherwise, not allowed.
        if(System.currentTimeMillis()-lastTime>17) //17 ms between each is approx. 60 fps
        {
            repaint(15);
            lastTime = System.currentTimeMillis();
        }
    }
    
    double[] oldvalues = new double[4];
    /**
     * <code>repaintWait</code> is a method that forces the <code>repaint</code> method to only operate
     * at 60fps. Anything higher causes jitteriness and flashes on the screen. 
     * This version also only repaints the relevant portions of the screen for an object movement
     * @param objIndex the index of the object that moved.
     * @param pointIndex the index of the point on that object. Only relevant for Straight and SmoothLines.
     * For StraightLines 0 corresponds to initialPosition and 1 to finalPosition
     * For SmoothLines this corresponds to the index in the coordinates array
     * @param prevPoint the location that the object moved from
     */
    public void repaintWait(int objIndex, int pointIndex, Point prevPoint)
    {
        GUIObject obj = objs.get(objIndex);
        System.out.println("Object @ " + objIndex + " is " + obj);
        System.out.println("Previous Position is: (" + prevPoint.getX() + "," + prevPoint.getY() + ")");
        int topY = Integer.MAX_VALUE;
        int botY = Integer.MIN_VALUE;
        int leftX = Integer.MAX_VALUE;
        int rightX = Integer.MIN_VALUE;
        Rectangle r = new Rectangle(0, 0, 0, 0);
        if(obj instanceof StraightLine)
        {
            Point V0 = new Point(((StraightLine)obj).initialPosition);
            Point V1 = new Point(((StraightLine)obj).finalPosition);
            System.out.println("V0x: " + V0.getX() + " V0y: " + V0.getY() + " V1x: " + V1.getX() + " V1y: " + V1.getY());
            topY = (int)Math.min(V0.getY(), V1.getY());
            botY = (int)Math.max(V0.getY(), V1.getY());
            leftX = (int)Math.min(V0.getX(), V1.getX());
            rightX = (int)Math.max(V0.getX(), V1.getX());
            topY = (int)Math.min(topY, prevPoint.getY());
            botY = (int)Math.max(botY, prevPoint.getY());
            leftX = (int)Math.min(leftX, prevPoint.getX());
            rightX = (int)Math.max(rightX, prevPoint.getX());
            System.out.println("topY : " + topY + " botY : " + botY + " leftX : " + leftX + " rightX: " + rightX);
            
            if(pointIndex == 0)
            {
                for(int i = objIndex-1; i>=4; i--)
                {
                    if(objs.get(i) instanceof StraightLine)
                    {
                        StraightLine sl = (StraightLine)objs.get(objIndex);
                        topY = (int)Math.min(topY, sl.finalPosition.getY());
                        botY = (int)Math.max(botY, sl.finalPosition.getY());
                        leftX = (int)Math.min(leftX, sl.finalPosition.getX());
                        rightX = (int)Math.max(rightX, sl.finalPosition.getY());
                        break;
                    }
                    else if(objs.get(i) instanceof SmoothLine)
                    {
                        SmoothLine sl = (SmoothLine)objs.get(objIndex);
                        Point p = sl.coordinates.get(sl.coordinates.size() -1);
                        topY = (int)Math.min(topY, p.getY());
                        botY = (int)Math.max(botY, p.getY());
                        leftX = (int)Math.min(leftX, p.getX());
                        rightX = (int)Math.max(rightX, p.getX());
                        break;
                    }
                }
            }
            else if(pointIndex == 1)
            {
                
                for(int i = objIndex+1; i<objs.size(); i++)
                {
                    if(objs.get(i) instanceof StraightLine)
                    {
                        System.out.println("Next Object is at index: " + i);
                        StraightLine sl = (StraightLine)objs.get(i);
                        topY = (int)Math.min(topY, sl.finalPosition.getY());
                        botY = (int)Math.max(botY, sl.finalPosition.getY());
                        leftX = (int)Math.min(leftX, sl.finalPosition.getX());
                        rightX = (int)Math.max(rightX, sl.finalPosition.getX());
                        System.out.println("Slx: " + sl.finalPosition.getX() + " Sly: " + sl.finalPosition.getY()); 
                        System.out.println("topY : " + topY + " botY : " + botY + " leftX : " + leftX + " rightX: " + rightX);
                        break;
                    }
                    else if(objs.get(i) instanceof SmoothLine)
                    {
                        SmoothLine sl = (SmoothLine)objs.get(i);
                        Point p = sl.coordinates.get(0);
                        topY = (int)Math.min(topY, p.getY());
                        botY = (int)Math.max(botY, p.getY());
                        leftX = (int)Math.min(leftX, p.getX());
                        rightX = (int)Math.max(rightX, p.getX());
                        
                        Point p2 = sl.coordinates.get(1);
                        topY = (int)Math.min(topY, p2.getY());
                        botY = (int)Math.max(botY, p2.getY());
                        leftX = (int)Math.min(leftX, p2.getX());
                        rightX = (int)Math.max(rightX, p2.getX());
                        break;
                    }
                }
            }
        }
        else if(obj instanceof SmoothLine)
        {
            SmoothLine ln = ((SmoothLine)obj);
            Point V0 = new Point(ln.coordinates.get(pointIndex));
            
            topY = (int)V0.getY();
            botY = (int)V0.getY();
            leftX = (int)V0.getX();
            rightX = (int)V0.getX();
            
            topY = (int)Math.min(topY, prevPoint.getY());
            botY = (int)Math.max(botY, prevPoint.getY());
            leftX = (int)Math.min(leftX, prevPoint.getX());
            rightX = (int)Math.max(rightX, prevPoint.getX());
            
            System.out.println(pointIndex);
            if(pointIndex == 0)
            {
                for(int i = objIndex; i>=4; i--)
                {
                    if(objs.get(objIndex) instanceof StraightLine)
                    {
                        StraightLine sl = (StraightLine)objs.get(objIndex);
                        topY = (int)Math.min(topY, sl.finalPosition.getY());
                        botY = (int)Math.max(botY, sl.finalPosition.getY());
                        leftX = (int)Math.min(leftX, sl.finalPosition.getX());
                        rightX = (int)Math.max(rightX, sl.finalPosition.getY());
                        break;
                    }
                    else if(objs.get(objIndex) instanceof SmoothLine)
                    {
                        SmoothLine sl = (SmoothLine)objs.get(objIndex);
                        Point p = sl.coordinates.get(sl.coordinates.size() -1);
                        topY = (int)Math.min(topY, p.getY());
                        botY = (int)Math.max(botY, p.getY());
                        leftX = (int)Math.min(leftX, p.getX());
                        rightX = (int)Math.max(rightX, p.getX());
                        break;
                    }
                }
                Point p = ln.coordinates.get(1);
                topY = (int)Math.min(topY, p.getY());
                botY = (int)Math.max(botY, p.getY());
                leftX = (int)Math.min(leftX, p.getX());
                rightX = (int)Math.max(rightX, p.getX());
            }
            else if(pointIndex == ln.coordinates.size()-1)
            {
                for(int i = pointIndex; i<objs.size(); i++)
                {
                    if(objs.get(objIndex) instanceof StraightLine)
                    {
                        StraightLine sl = (StraightLine)objs.get(objIndex);
                        topY = (int)Math.min(topY, sl.initialPosition.getY());
                        botY = (int)Math.max(botY, sl.initialPosition.getY());
                        leftX = (int)Math.min(leftX, sl.initialPosition.getX());
                        rightX = (int)Math.max(rightX, sl.initialPosition.getY());
                        break;
                    }
                    else if(objs.get(objIndex) instanceof SmoothLine)
                    {
                        SmoothLine sl = (SmoothLine)objs.get(objIndex);
                        Point p = sl.coordinates.get(0);
                        topY = (int)Math.min(topY, p.getY());
                        botY = (int)Math.max(botY, p.getY());
                        leftX = (int)Math.min(leftX, p.getX());
                        rightX = (int)Math.max(rightX, p.getX());
                        break;
                    }
                }
                Point p = ln.coordinates.get(pointIndex-1);
                topY = (int)Math.min(topY, p.getY());
                botY = (int)Math.max(botY, p.getY());
                leftX = (int)Math.min(leftX, p.getX());
                rightX = (int)Math.max(rightX, p.getX());
            }
            else
            {
                Point p = ln.coordinates.get(pointIndex-1);
                topY = (int)Math.min(topY, p.getY());
                botY = (int)Math.max(botY, p.getY());
                leftX = (int)Math.min(leftX, p.getX());
                rightX = (int)Math.max(rightX, p.getX());
                
                p = ln.coordinates.get(pointIndex+1);
                topY = (int)Math.min(topY, p.getY());
                botY = (int)Math.max(botY, p.getY());
                leftX = (int)Math.min(leftX, p.getX());
                rightX = (int)Math.max(rightX, p.getX());
            }
        }
        else if(obj instanceof PositionCommandGroup)
        {
            int size2 = (int)(command_group_size * 1.5);
            int x = (int)((PositionCommandGroup)obj).pos.getX();
            int y = (int)((PositionCommandGroup)obj).pos.getY();
            topY = y-size2;
            leftX = x-size2;
            botY = y+size2;
            rightX = x+size2;
        }
        else if(obj instanceof TimeCommandGroup)
        {
            int size2 = (int)(command_group_size * 1.5);
            int x = (int)((TimeCommandGroup)obj).pos.getX();
            int y = (int)((TimeCommandGroup)obj).pos.getY();
            topY = y-size2;
            leftX = x-size2;
            botY = y+size2;
            rightX = x+size2;
        }
        if(System.currentTimeMillis()-lastTime>17) //17 ms between each is approx. 60 fps
        {
            //System.out.println("*******************************PAINTED SUCCESSFULLY**********************************");
            if(oldvalues[0] != -1)
            {
                topY = (int)Math.min(topY, oldvalues[0]);
                botY = (int)Math.max(botY, oldvalues[1]);
                leftX = (int)Math.min(leftX, oldvalues[2]);
                rightX = (int)Math.max(rightX, oldvalues[3]);
                oldvalues[0] = -1;
                oldvalues[1] = -1;
                oldvalues[2] = -1;
                oldvalues[3] = -1;
            }
            int dy = botY-topY;
            int dx = rightX-leftX;
            r.setBounds(leftX-15, topY-15, dx+30, dy+30);
            repaint(15,r.x,r.y,r.width,r.height);
            lastTime = System.currentTimeMillis();
        }
        else
        {
            System.out.println("*******************************DID NOT PAINT DUE TO TIME RESTRICTIONS**********************************");
            
            oldvalues[0] = topY;
            oldvalues[1] = botY;
            oldvalues[2] = leftX;
            oldvalues[3] = rightX;
        }
        System.out.println();
    }
    
    /**
     * <code>drawGUIObject_inList</code> is an extension of <code>drawGUIObject</code>
     * that draws specifically GUIObjects within the objs list.
     * @param index the index of the GUIObject to be drawn
     * @param g the Graphics class to use for drawing
     * @throws UnknownClassException if the GUIObject given by <code>index</code> is not a recognized subclass of GUIObject
     */
    public void drawGUIObject_inList(int index, Graphics g) throws UnknownClassException
    {
        drawGUIObject(objs.get(index), g);
    }
    
    /**
     * The method <code>drawGUIObject</code> draws the object inputted through the parameter obj
     * onto the canvas frame
     * @param obj the <code>GUIObject</code> to be drawn
     * @param g the Graphics to use for drawing
     * @throws UnknownClassException if the GUIObject given by <code>obj</code> is not of the following classes: 
     * <ul>
     * <li><code>PositionCommandGroup</code></li>
     * <li><code>SmoothLine</code></li>
     * <li><code>StraightLine</code></li>
     * <li><code>TimeCommandGroup</code></li>
     * </ul>
     */
    public void drawGUIObject(GUIObject obj, Graphics g) throws UnknownClassException
    {
        //System.out.print("Drawing ");
        // Using multiple if/else check what kind of an object the guven GUIObject is.
        if(obj instanceof PositionCommandGroup)
        {
            //System.out.println("PositionCommandGroup");
            //PositionCommandGroups are red squares of dimension of command_group_size
            g.setColor(Color.RED);
            if(PositionUtil.distance(((PositionCommandGroup)obj).pos, PositionUtil.closestPointonLine(objs, ((PositionCommandGroup)obj).pos)) >10)
            {
                if(objs.indexOf(obj) != 0)
                {
                    g.setColor(Color.GRAY);
                }
            }
            
            int x = (int)((PositionCommandGroup)obj).pos.getX();
            int y = (int)((PositionCommandGroup)obj).pos.getY();
            //System.out.println("(" + x + "," + y + ")");
            //fillRect fills a rectangle starting from the point given by the first two inputs and of dimensions given by the second two
            //We want (X,Y) to represent the center of the circle, so draw the square starting at (X,Y) - (Size/2,Size/2) with dimensions of (size,size)
            g.fillRect(x-(command_group_size/2),y-(command_group_size/2),command_group_size,command_group_size);
        }
        else if(obj instanceof TimeCommandGroup)
        {
            //System.out.println("TimeCommandGroup");
            //TimeCommandGroups are green squares of dimension of command_group_size
            g.setColor(Color.GREEN);
            /*
            if(PositionUtil.distance(((TimeCommandGroup)obj).pos, PositionUtil.closestPointonLine(objs, ((TimeCommandGroup)obj).pos)) >10)
            {
                if(objs.indexOf(obj) != 3)
                {
                    g.setColor(Color.GRAY);
                }
            }
            */
            int x = (int)((TimeCommandGroup)obj).pos.getX();
            int y = (int)((TimeCommandGroup)obj).pos.getY();
            
            //IDEA: SHOW TIME ON THE TIMECOMMAND GROUP
            
            //fillRect fills a rectangle starting from the point given by the first two inputs and of dimensions given by the second two
            //We want (X,Y) to represent the center of the circle, so draw the square starting at (X,Y) - (Size/2,Size/2) with dimensions of (size,size)
            g.fillRect(x-(command_group_size/2),y-(command_group_size/2),command_group_size,command_group_size);
        }
        else if(obj instanceof SmoothLine)
        {
            //System.out.println("SmoothLine");
            //For Smoothline, we want to draw lines connecting every point on the smoothline.
            g.setColor(Color.blue);
            
            
            //prevPos is the position of the previous point drawn on the line.
            int[] prevPos = null;
            for(int i = 0; i<((SmoothLine)obj).coordinates.size(); i++)
            {
                //pos is the current point's line
                int[] pos = new int[2];
                pos[0] = ((SmoothLine)obj).coordinates.get(i).x;
                pos[1] = ((SmoothLine)obj).coordinates.get(i).y;
                
                //if prevPos is null, that means we just started drawing, so skio this one and go to the next
                if(prevPos == null){
                    prevPos = pos;
                    continue;
                }
                g.fillOval(prevPos[0]-5, prevPos[1]-5, 10, 10);
                //g.fillOval(pos[0]-4, pos[1]-4, 8, 8); //UNSURE
                //draw a straight line from the previous point to this one, and set the current point as pre
                g.drawLine(prevPos[0], prevPos[1], pos[0], pos[1]);
                prevPos = pos;
            }
        }
        else if(obj instanceof StraightLine)
        {
           // //System.out.println("StraightLine");
            //Simple - just draw a line from the initial position to the final one
            g.setColor(Color.black);
            g.fillOval(((StraightLine)obj).initialPosition.x-5, ((StraightLine)obj).initialPosition.y-5, 10, 10);
            //System.out.println(((StraightLine)obj).initialPosition + "    " + ((StraightLine)obj).finalPosition);
            //g.fillOval(((StraightLine)obj).finalPosition.x-4, ((StraightLine)obj).finalPosition.y-4, 8, 8);            
            g.drawLine(((StraightLine)obj).initialPosition.x, ((StraightLine)obj).initialPosition.y, ((StraightLine)obj).finalPosition.x, ((StraightLine)obj).finalPosition.y);
        }
        else
        {
            //Seriously dude this should only exist if some idiot messes wiwth my code.
            throw new UnknownClassException();
        }
    }
    
    
    
    //Location/Movement
    

    
    //*********************MOUSE EVENT ITEMS*******************
    
    /**
     * CreationStatus has four options. <br>
     * 0 - Nothing to create. Proceed with caution. <br>
     * 1 - Creating a PositionCommandGroup <br>
     * 2 - Creating a StraightLine <br>
     * 3 - Creating a SmoothLine <br>
     * 4 - Creating a TimeCommandGroup
     */
    private int creationStatus; 
    
    /**
     * MovementStatus has four options.  <br>
     * 0 - Nothing to create. Proceed with caution.  <br>
     * 1 - Moving a PositionCommandGroup <br>
     * 2 - Moving a StraightLine <br>
     * 3 - Moving a SmoothLine <br>
     * 4 - Moving a TimeCommandGroup
     */
    private int movementStatus;
    
    /**
     * data1 is used to denote which point on a SmoothLine or StraightLine that we are currently modifying
     */
    int data1;
    
    /**
     * data2 is used to denote which obj in objs we are currently modifying
     */
    int data2;
    
    GUIObject prev = null;
    GUIObject next = null;
    
    /**
     * mouseDragged is a method that is called whenever a mouse button is depressed while the mouse is moving
     * @param e the current information about the mouse
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if(creationStatus == 0)
        {
            if(movementStatus == 1)
            {
                PositionCommandGroup grp = (PositionCommandGroup)objs.get(data2);
                Point p = new Point(grp.pos);
                grp.pos = e.getPoint();
                
                repaintWait(data2,-1,p);
            }
            else if(movementStatus == 2)
            {
                StraightLine ln = (StraightLine)objs.get(data2);
                Point p;
                if(data1 == 1)
                {
                    p = new Point(ln.finalPosition);
                    Point V0 = new Point(ln.initialPosition);
                    Point V1 = new Point(ln.finalPosition);
                    
                    Point S0 = null;
                    Point S1 = null;
                    if(next != null)
                    {
                        if(next instanceof StraightLine)
                        {
                            StraightLine nxt = (StraightLine)next;
                        
                            S0 = new Point(nxt.initialPosition);
                            S1 = new Point(nxt.finalPosition);
                        }
                        else if(next instanceof SmoothLine)
                        {
                            SmoothLine nxt = (SmoothLine)next;
                            
                            S0 = new Point(nxt.coordinates.get(0));
                            S1 = new Point(nxt.coordinates.get(1));
                        }
                    }
                    
                    ln.finalPosition.setLocation(e.getPoint());
                
                    if(next != null)
                    {
                        if(next instanceof StraightLine)
                        {
                            StraightLine nxt = (StraightLine)next;
                        
                            nxt.initialPosition.setLocation(e.getPoint());
                        }
                        else if(next instanceof SmoothLine)
                        {
                            SmoothLine nxt = (SmoothLine)next;
                            
                            nxt.coordinates.get(0).setLocation(e.getPoint());
                        }
                    }

                }
                else
                {
                    p = new Point(ln.finalPosition);
                    //only reason that data1 would be 0 is if this is the first line 
                    Point V0 = new Point(ln.initialPosition);
                    Point V1 = new Point(ln.finalPosition);
                    ln.initialPosition.setLocation(e.getPoint());
                }
                
                repaintWait(data2,data1,p);
            }
            else if(movementStatus == 3)
            {
                SmoothLine ln = (SmoothLine)objs.get(data2);
                
                Point p = new Point(ln.coordinates.get(data1));
                if(data1 == 0 && prev != null)
                {
                    if(prev instanceof StraightLine)
                    {
                        ((StraightLine)prev).finalPosition.setLocation(e.getPoint());
                    }
                    else if(prev instanceof SmoothLine)
                    {
                        ((SmoothLine)prev).coordinates.get(((SmoothLine)prev).coordinates.size() -1).setLocation(e.getPoint());
                    }
                }
                
                if(data1 == ln.coordinates.size()-1 && next != null)
                {
                    if(next instanceof StraightLine)
                    {
                        ((StraightLine)next).initialPosition.setLocation(e.getPoint());
                    }
                    else if(next instanceof SmoothLine)
                    {
                        ((SmoothLine)next).coordinates.get(0).setLocation(e.getPoint());
                    }
                }
                //Point S0 = null; new Point(ln.xcoordinates.get(data1), ln.ycoordinates.get(data1));
                //Point S1 = null; new Point(ln.xcoordinates.get(data1+1), ln.ycoordinates.get(data1+1));
                
                ln.coordinates.set(data1, e.getPoint());
                
                repaintWait(data2,data1,p);
            }
            else if(movementStatus == 4)
            {
                double d = timeline_f.x - timeline_0.x;
                d = 15*d/16;
                int max = (int)d + timeline_0.x;
                Point p = new Point(((TimeCommandGroup)objs.get(data2)).pos);
                if(e.getX()>=max)
                {
                    ((TimeCommandGroup)objs.get(data2)).time = 15;
                    ((TimeCommandGroup)objs.get(data2)).pos = new Point(max, timeline_f.y);
                }
                else if(e.getX()<= timeline_0.x)
                {
                    ((TimeCommandGroup)objs.get(data2)).time = 0;
                    ((TimeCommandGroup)objs.get(data2)).pos = new Point(timeline_0);
                }
                else
                {
                    double s = e.getX() - timeline_0.x;
                    d = timeline_f.x - timeline_0.x;
                    ((TimeCommandGroup)objs.get(data2)).time = 16*(s)/(d);
                    ((TimeCommandGroup)objs.get(data2)).pos = new Point(e.getX(), timeline_0.y);
                }
                repaintWait(data2,-1,p);
            }
        }
    }
    
    /**
     * method called whenever the mouse is moved
     * @param e the current information about the mouse
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if(creationStatus == 1)
        {
            Point p = ((PositionCommandGroup)objs.get(data2)).pos;
            ((PositionCommandGroup)objs.get(data2)).pos.setLocation(e.getPoint());
            repaintWait(data2,-1,p);
        }
        else if(creationStatus == 2 && data1 == 1)
        {
            Point p = ((StraightLine)objs.get(data2)).finalPosition;
            ((StraightLine)objs.get(data2)).finalPosition.setLocation(e.getPoint());
            repaintWait(data2,1,p);
        }
        else if(creationStatus == 3 && data1 > 0)
        {
            Point p = ((SmoothLine)objs.get(data2)).coordinates.get(data1);
            ((SmoothLine)objs.get(data2)).coordinates.set(data1, e.getPoint());
            repaintWait(data2,data1,p);
        }
    }

    /**
     * method that is called when a mouse button is pressed and then depressed almost immediately
     * (also known as someone clicking)
     * @param e the state of the mouse
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            if(creationStatus == 0)
            {
                if(PositionUtil.nearObject(objs.get(0), e.getPoint()))
                {
                    boolean first = true;
                    for(int i = 4; i<objs.size(); i++)
                    {
                        if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                        {
                            //System.out.println('i');
                            first = false;
                            break;
                        }
                    }
                    
                    if(first)
                    {
                        JOptionPane.showMessageDialog(this, "You cannot add a PositionCommandGroup until you add a movement line", "ERROR", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    creationStatus = 1;
                    data2 = objs.size();
                    PositionCommandGroup g = new PositionCommandGroup();
                    g.pos.setLocation(e.getPoint());
                    objs.add(g);
                    repaint();
                }
                else if(PositionUtil.nearObject(objs.get(1), e.getPoint()))
                {
                   creationStatus = 2;
                   data1 = 0;
                   repaint();
                }
                else if(PositionUtil.nearObject(objs.get(2), e.getPoint()))
                {
                    creationStatus = 3;
                    repaint();
                }
                else if(PositionUtil.nearObject(objs.get(3), e.getPoint()))
                {
                    //System.out.println("Hi");
                    TCGcreator ctr = new TCGcreator();
                    ctr.canv = this;
                    ctr.setVisible(true);
                }
                else
                {
                    for(int banana = 4; banana<objs.size(); banana++)
                    {
                        if(objs.get(banana) instanceof StraightLine && PositionUtil.nearObject(objs.get(banana), e.getPoint()))
                        {
                            int loc = 1;
                            if(PositionUtil.distance(((StraightLine)objs.get(banana)).initialPosition, e.getPoint())<PositionUtil.distance(((StraightLine)objs.get(banana)).finalPosition, e.getPoint()))
                            {
                                loc = 0;
                            }
                            try {
                                PointLocChange c = new PointLocChange(banana, loc, this);
                                c.setVisible(true);
                            } catch (UnknownClassException ex) {
                                Logger.getLogger(AutoPathCanvas.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                        }
                        else if(objs.get(banana) instanceof SmoothLine && PositionUtil.nearObject(objs.get(banana), e.getPoint()))
                        {
                            int loc = 0;
                            double mindist = Double.MAX_VALUE;
                            for(Point p : ((SmoothLine)objs.get(banana)).coordinates)
                            {
                                double dist = PositionUtil.distance(p, e.getPoint());
                                if(dist < mindist)
                                {
                                    mindist = dist;
                                    loc = ((SmoothLine)objs.get(banana)).coordinates.indexOf(p);
                                }
                            }
                            try {
                                PointLocChange c = new PointLocChange(banana, loc, this);
                                c.setVisible(true);
                            } catch (UnknownClassException ex) {
                                Logger.getLogger(AutoPathCanvas.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                        }
                    }
                }
            }
            else if(creationStatus == 1)
            {
                Point p = PositionUtil.closestPointonLine(objs, e.getPoint());
                if(PositionUtil.distance(e.getPoint(),p) < 50)
                {
                    ((PositionCommandGroup)objs.get(data2)).pos.setLocation(e.getPoint());
                    PCGcreator crtr = new PCGcreator(data2);
                    crtr.canv = this;
                    crtr.run();
                    creationStatus = 0;
                    data1 = 0;
                    data2 = 0;
                    repaint();
                }
            }
            else if(creationStatus == 2)
            {
                if(e.getX()>pallete_left_x || e.getY()>time_loc_interface_y)
                {
                    return;
                }
                if(data2 != 0)
                {
                    int maxTime = 0;
                    try{
                        maxTime = Integer.parseInt(JOptionPane.showInputDialog("What is the max time that this can run?"));
                    }
                    catch(NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(this, "Please right click on the line and enter a valid number");
                    }
                    ((StraightLine)objs.get(data2)).time = maxTime;
                }
                int index = -1;
                for(int i = objs.size()-1; i>3; i--)
                {
                    if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                    {
                        index = i;
                        break;
                    }
                }
                if(index == -1)
                {
                    data2 = objs.size();
                    StraightLine line = new StraightLine();
                    line.initialPosition.setLocation(e.getPoint());
                    line.finalPosition.setLocation(e.getPoint());
                    data1 = 1;
                    objs.add(line);
                    repaintWait(data2,data1,e.getPoint());
                }
                else
                {
                    if(objs.get(index) instanceof SmoothLine)
                    {
                        SmoothLine ln = (SmoothLine) objs.get(index);
                        int max = ln.coordinates.size() - 1;
                        if(PositionUtil.withinBounds(ln.coordinates.get(max).x,e.getX(),30) && PositionUtil.withinBounds(ln.coordinates.get(max).y, e.getY(), 30))
                        {
                            data2 = objs.size();
                            StraightLine line = new StraightLine();
                            line.initialPosition.setLocation(ln.coordinates.get(max));
                            line.finalPosition.setLocation(e.getPoint());
                            data1 = 1;
                            objs.add(line);
                            repaintWait(data2,data1,ln.coordinates.get(max));
                        }
                    }
                    else if(objs.get(index) instanceof StraightLine)
                    {
                        StraightLine ln = ((StraightLine)objs.get(index));
                        if(PositionUtil.withinBounds(ln.finalPosition.x,e.getX(),30) && PositionUtil.withinBounds(ln.finalPosition.y, e.getY(), 30))
                        {
                            data2 = objs.size();
                            StraightLine line = new StraightLine();
                            line.initialPosition.setLocation(ln.finalPosition);
                            line.finalPosition.setLocation(e.getPoint());
                            data1 = 1;
                            objs.add(line);
                            repaintWait(data2,data1,ln.finalPosition);
                        }
                    }
                }
                
                
            }
            else if(creationStatus == 3 && data1 == 0)
            {
                if(e.getX()>pallete_left_x || e.getY()>time_loc_interface_y)
                {
                    return;
                }
                int index = -1;
                for(int i = objs.size()-1; i>3; i--)
                {
                    if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                    {
                        index = i;
                        break;
                    }
                }
                if(index == -1)
                {
                    data2 = objs.size();
                    SmoothLine line = new SmoothLine();
                    line.coordinates.add(e.getPoint());
                    line.coordinates.add(e.getPoint());
                    data1 = 1;
                    objs.add(line);
                    repaintWait(data2,data1,e.getPoint());
                }
                else
                {
                    if(objs.get(index) instanceof SmoothLine)
                    {
                        SmoothLine ln = (SmoothLine) objs.get(index);
                        int max = ln.coordinates.size() - 1;
                        if(PositionUtil.withinBounds(ln.coordinates.get(max).x,e.getX(),30) && PositionUtil.withinBounds(ln.coordinates.get(max).x, e.getY(), 30))
                        {
                            data2 = objs.size();
                            SmoothLine line = new SmoothLine();
                            line.coordinates.add(new Point(ln.coordinates.get(max)));
                            line.coordinates.add(e.getPoint());
                            data1 = 1;
                            objs.add(line);
                            repaintWait(data2,data1, ln.coordinates.get(max));
                        }
                    }
                    else if(objs.get(index) instanceof StraightLine)
                    {
                        StraightLine ln = ((StraightLine)objs.get(index));
                        if(PositionUtil.withinBounds(ln.finalPosition.x,e.getX(),30) && PositionUtil.withinBounds(ln.finalPosition.y, e.getY(), 30))
                        {
                            data2 = objs.size();
                            SmoothLine line = new SmoothLine();
                            line.coordinates.add(new Point(ln.finalPosition));
                            line.coordinates.add(e.getPoint());
                            data1 = 1;
                            objs.add(line);
                            repaintWait(data2,data1,e.getPoint());
                        }
                    }
                }
            }
            else if(creationStatus == 3 && data1 > 0)
            {
                if(e.getX()>pallete_left_x || e.getY()>time_loc_interface_y)
                {
                    return;
                }
                ((SmoothLine)objs.get(data2)).coordinates.add(e.getPoint());
                data1++;
            }
        }
        else if(e.getButton() == MouseEvent.BUTTON3)
        {
            if(creationStatus == 0)
            {
               for(int banana = 4; banana < objs.size(); banana++)
               {
                   GUIObject g = objs.get(banana);
                   if(PositionUtil.nearObject(g, e.getPoint()))
                   {
                        if(g instanceof PositionCommandGroup)
                        {
                       
                            PCGcreator ctr = new PCGcreator(objs.indexOf(g), ((PositionCommandGroup)g));
                            ctr.canv = this;
                            ctr.run();
                            break;
                        }
                        else if(g instanceof StraightLine)
                        {
                           int index = objs.indexOf(g);
                           int ind2 = -1;
                           for(int i = index+1; i<objs.size(); i++)
                           {
                                if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                                {
                                   ind2 = i;
                                   break;
                                }
                            }
                            boolean first = true;
                            for(int i = index -1; i>3; i--)
                            {
                                if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                                {
                                    first = false;
                                }
                            }
                            if(PositionUtil.distance(e.getPoint(), ((StraightLine)g).initialPosition) > PositionUtil.distance(e.getPoint(), ((StraightLine)g).finalPosition))
                            {
                                first = false;
                            }
                            InsertLine ln = new InsertLine(this, index, ind2, first);
                            ln.setVisible(true);
                            break;
                        }
                        else if(g instanceof SmoothLine)
                        {
                            SmoothLine smth = (SmoothLine)g;
                            int index = -1;
                            for(int i = 0; i<smth.coordinates.size(); i++)
                            {
                                Point p = smth.coordinates.get(i);
                                if(PositionUtil.withinBounds(p.getX(),e.getX(),15) && PositionUtil.withinBounds(p.getY(),e.getY(),15))
                                {
                                    index = i;
                                    break;
                                }
                            }
                            if(index == 0 || index == smth.coordinates.size()-1)
                            {
                                int ind = objs.indexOf(g);
                                int ind2 = -1;
                                for(int i = ind + 1; i<objs.size(); i++)
                                {
                                    if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                                    {
                                        ind2 = i;
                                        break;
                                    }
                                }
                                boolean first = true;
                                for(int i = ind -1; i>3; i--)
                                {
                                    if(objs.get(i) instanceof SmoothLine || objs.get(i) instanceof StraightLine)
                                    {
                                        first = false;
                                        break;
                                    }
                                }
                                if(index != 0) first = false;
                                InsertLine line = new InsertLine(this, ind, ind2, first);
                                line.setVisible(true);
                            }
                            else
                            {
                                InsertSegments seg = new InsertSegments(this, objs.indexOf(g), index);
                                seg.setVisible(true);
                            }
                            break;
                        }
                        else if(g instanceof TimeCommandGroup)
                        {
                            TCGcreator ctr = new TCGcreator(objs.indexOf(g), (TimeCommandGroup)g);
                            ctr.canv = this;
                            ctr.setVisible(true);
                            break;
                        }
                    }
                   else
                   {
                       if(g instanceof StraightLine)
                       {
                           String[] options = {"Delete this", "Modify this", "Nothing"};
                           if(PositionUtil.onLine(e.getPoint(), ((StraightLine)g).initialPosition, ((StraightLine)g).finalPosition)){
                               
                               int i = JOptionPane.showOptionDialog(this, "What would you like to do?", "Property Select", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                                                              
                               if(i == 0)
                               {
                                   for(int j = banana+1; j<objs.size(); j++)
                                   {
                                       if(objs.get(j) instanceof StraightLine)
                                       {
                                           StraightLine ln = ((StraightLine)objs.get(j));
                                           ln.initialPosition.setLocation(((StraightLine)g).initialPosition);
                                           break;
                                       }
                                       else if(objs.get(j) instanceof SmoothLine)
                                       {
                                           SmoothLine ln = (SmoothLine)objs.get(j);
                                           ln.coordinates.get(0).setLocation(((StraightLine)g).initialPosition);
                                           break;
                                       }
                                   }
                                   objs.remove(banana);
                               }
                               else if(i == 1)
                               {
                                   try{
                                        ((StraightLine)g).time = Double.parseDouble(JOptionPane.showInputDialog("What is the new time?", ((StraightLine)g).time));
                                   }
                                   catch(NumberFormatException ex)
                                   {
                                       JOptionPane.showMessageDialog(this, "ERROR: One cannot use that as a time. Please try again", "ERROR", JOptionPane.ERROR_MESSAGE);
                                   }
                               }
                               repaint();
                           }
                       }
                       else if(g instanceof SmoothLine)
                       {
                           SmoothLine ln = (SmoothLine)g;
                           for(int i = 1; i<ln.coordinates.size(); i++)
                           {
                               if(PositionUtil.onLine(e.getPoint(), ln.coordinates.get(i-1), ln.coordinates.get(i)))
                               {
                                   String[] options = {"Delete this segment", "Delete  this line", "Modify this line", "Nothing"};
                                   int j = JOptionPane.showOptionDialog(this, "What would you like to do?", "Property Select", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
                                   
                                   if(j == 0)
                                   {
                                       if(ln.coordinates.size()-1 == i)
                                       {
                                           for(int k = banana+1; k<objs.size(); k++)
                                           {
                                                if(objs.get(k) instanceof StraightLine)
                                                {
                                                    StraightLine lin = ((StraightLine)objs.get(k));
                                                    lin.initialPosition.setLocation(ln.coordinates.get(i-1));
                                                    break;
                                                }
                                                else if(objs.get(k) instanceof SmoothLine)
                                                {
                                                    SmoothLine lin = (SmoothLine)objs.get(k);
                                                    lin.coordinates.get(0).setLocation(ln.coordinates.get(i-1));
                                                    break;
                                                }
                                           }
                                       }
                                       ln.coordinates.set(i, ln.coordinates.get(i-1));
                                       ln.coordinates.remove(i-1);
                                       if(ln.coordinates.size() == 1)
                                       {
                                           objs.remove(banana);
                                       }
                                   }
                                   else if(j == 1)
                                   {
                                       for(int k = banana+1; k<objs.size(); k++)
                                        {
                                            if(objs.get(k) instanceof StraightLine)
                                            {
                                                StraightLine lin = ((StraightLine)objs.get(k));
                                                lin.initialPosition.setLocation(ln.coordinates.get(0));
                                                break;
                                            }
                                            else if(objs.get(k) instanceof SmoothLine)
                                            {
                                                SmoothLine lin = (SmoothLine)objs.get(k);
                                                lin.coordinates.get(0).setLocation(ln.coordinates.get(0));
                                                break;
                                            }
                                        }
                                        objs.remove(banana);
                                   }
                                   else if(j == 2)
                                   {
                                       try{
                                            ((SmoothLine)g).maxTime = Double.parseDouble(JOptionPane.showInputDialog("What is the new time?", ((SmoothLine)g).maxTime));
                                        }
                                        catch(NumberFormatException ex)
                                        {
                                            JOptionPane.showMessageDialog(this, "ERROR: One cannot use that as a time. Please try again", "ERROR", JOptionPane.ERROR_MESSAGE);
                                        }
                                   }
                                   repaint();
                                   break;
                               }
                           }
                       }
                   }
                       
                }
            }
            else if(creationStatus == 1)
            {
                
            }
            else if(creationStatus == 2)
            {
                if(e.getX()>pallete_left_x || e.getY()>time_loc_interface_y)
                {
                    return;
                }
                if(data1 == 0)
                {
                    return;
                }
                
                ((StraightLine)objs.get(data2)).finalPosition.setLocation(e.getPoint());
                
                
                int maxTime = 0;
                try{
                    maxTime = Integer.parseInt(JOptionPane.showInputDialog("What is the max time that this can run?"));
                }
                catch(NumberFormatException ex)
                {
                    JOptionPane.showMessageDialog(this, "Please right click on the line and enter a valid number");
                }
                ((StraightLine)objs.get(data2)).time = maxTime;
                creationStatus = 0;
                data1 = 0;
                data2 = 0;
                repaint();
            }
            else if(creationStatus == 3)
            {
                if(e.getX()>pallete_left_x || e.getY()>time_loc_interface_y)
                {
                    return;
                }
                if(data1 == 0)
                {
                    return;
                }
                
                //((SmoothLine)objs.get(data2)).coordinates.add(e.getPoint());
                
                
                int maxTime = 0;
                try{
                    maxTime = Integer.parseInt(JOptionPane.showInputDialog("What is the max time that this can run?"));
                }
                catch(NumberFormatException ex)
                {
                    JOptionPane.showMessageDialog(this, "Please right click on the line and enter a valid number");
                }
                ((SmoothLine)objs.get(data2)).maxTime = maxTime;
                creationStatus = 0;
                data1 = 0;
                data2 = 0;
                repaint();
            }
        }
    }

    /**
     * Method called when the mouse is pressed
     * @param e the state of the mouse
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if(creationStatus == 0 && movementStatus == 0)
        {
            for(int i = 4; i<objs.size(); i++)
            {
                if(PositionUtil.nearObject(objs.get(i), e.getPoint()))
                {
                    data2 = i;
                    if(objs.get(i) instanceof StraightLine)
                    {
                        StraightLine ln = (StraightLine)objs.get(i);
                        movementStatus = 2;
                        
                        if(PositionUtil.distance(ln.finalPosition, e.getPoint()) < PositionUtil.distance(ln.initialPosition, e.getPoint()))
                        {
                            data1 = 1;
                        }
                        else
                        {
                            data1 = 0;
                            break;
                        }
                        for(int j = data2+1; j<objs.size(); j++)
                        {
                            if(objs.get(j) instanceof StraightLine || objs.get(j) instanceof SmoothLine)
                            {
                                next = objs.get(j);
                                break;
                            }
                        }
                        
                    }
                    else if(objs.get(i) instanceof SmoothLine)
                    {
                        SmoothLine ln = (SmoothLine)objs.get(i);
                        movementStatus = 3;
                        
                        for(int j  = 0; j<ln.coordinates.size(); j++)
                        {
                            Point p = new Point(ln.coordinates.get(j));
                            if(PositionUtil.withinBounds(p.getX(),e.getX(),15) && PositionUtil.withinBounds(p.getY(),e.getY(),15))
                            {
                                data1 = j;
                                break;
                            }
                        }
                        if(data1 == 0)
                        {
                            for(int j = data2-1; j>3; j--)
                            {
                                if(objs.get(j) instanceof StraightLine || objs.get(j) instanceof SmoothLine)
                                {
                                    prev = objs.get(j);
                                    break;
                                }
                            }
                        }
                        else if(data1 == ln.coordinates.size() - 1)
                        {
                            for(int k = data2+1; k<objs.size(); k++)
                            {
                                if(objs.get(k) instanceof StraightLine || objs.get(k) instanceof SmoothLine)
                                {
                                   next = objs.get(k);
                                   break;
                                }
                            }
                        }
                    }
                    else if(objs.get(i) instanceof PositionCommandGroup)
                    {
                        movementStatus = 1;
                    }
                    else if(objs.get(i) instanceof TimeCommandGroup)
                    {
                        movementStatus = 4;
                    }
                    break;
                }
            }
        }
    }

    /**
     * Method called when a mouse button is depressed
     * @param e the state of the mouse
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if(creationStatus == 0 && movementStatus != 0)
        {
            if(movementStatus == 1)
            {
                Point loc = new Point(((PositionCommandGroup)objs.get(data2)).pos);
                Point p = PositionUtil.closestPointonLine(objs, loc);
                if(PositionUtil.distance(loc, p) < 15)
                {
                    ((PositionCommandGroup)objs.get(data2)).pos.setLocation(p);
                    repaint();
                }
            }
            movementStatus = 0;
            data1 = 0;
            data2 = 0;
            next = null;
            prev = null;
        }
    }

    /**
     * Method called when the mouse pointer enters the canvas
     * @param e the state of the mouse
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    /**
     * Method called when the mouse pointer enters the canvas
     * @param e the state of the mouse
     */
    @Override
    public void mouseExited(MouseEvent e) {
        
    }
    
    /**
     * Method called when a key is pressed and immediately released.  
     * @param e what key was typed along with modifiers.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    /**
     * Method called when a key is pressed.
     * @param e what key was typed along with modifiers.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println(e.getKeyCode() + " " + KeyEvent.VK_ESCAPE + " " + e.getKeyChar());
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            objs.remove(data2);
            creationStatus = 0;
            movementStatus = 0;
            data1 = 0;
            data2 = 0;
            repaint();
        }
    }

    /**
     * Method called when a key is released.
     * @param e what key was typed along with modifiers.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        
    }

    /**
     * Creates the basic elements of the objects array - specifically the Pallete 
     * @return the basic objects Array
     */
    public static ArrayList<GUIObject> startObjs() {
        ArrayList<GUIObject> o;
        
        //Create Pallete Elements
        
        //The pallete Position Comman dGroue
        PositionCommandGroup palleteCG = new PositionCommandGroup();
        palleteCG.pos = new Point(1350,150);
        
        //The pallete Straight Line
        StraightLine palleteSL = new StraightLine();
        palleteSL.initialPosition = new Point(1300,250);
        palleteSL.finalPosition = new Point(1400,300);
        
        //The pallete Smooth Line
        SmoothLine palleteSmL = new SmoothLine();
        palleteSmL.coordinates.add(new Point(1300,350));
        palleteSmL.coordinates.add(new Point(1350, 450));
        palleteSmL.coordinates.add(new Point(1400,400));
        
        //The pallete Time Comand Group
        TimeCommandGroup palleteTG = new TimeCommandGroup();
        palleteTG.pos = new Point(1350,600);

        //Now that they exist, add them to the objs ArrayList
        o = new ArrayList<>();
        o.add(palleteCG);
        o.add(palleteSL);
        o.add(palleteSmL);
        o.add(palleteTG);
        
        return o;
    }


   
}
