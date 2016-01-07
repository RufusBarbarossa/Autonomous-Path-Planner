/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.calc;

import autonomouspathplanner.GUI.FalconLinePlot;
import autonomouspathplanner.GUI.objects.Command;
import autonomouspathplanner.GUI.objects.GUIObject;
import autonomouspathplanner.GUI.objects.PositionCommandGroup;
import autonomouspathplanner.GUI.objects.SmoothLine;
import autonomouspathplanner.GUI.objects.StraightLine;
import autonomouspathplanner.GUI.objects.TimeCommandGroup;
import autonomouspathplanner.PositionUtil;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Calculation class that has all of the methods needed to create an Autonomous path from a play
 * @author Alex
 */
public class AutonomousPathCreator {
    /**
     * the Robot Configuration profile
     */
    public static TrajectoryGenerator.Config cnfg = new TrajectoryGenerator.Config();
    
    /** 
     * Retrieve the robot configuration profile
     * @return current robot configuration profile
     */
    public static TrajectoryGenerator.Config getCnfg()
    {
       
        return cnfg;
    }
    
    /**
     * Calculates the robot path from the given play (<code>o</code>), with robot parameters
     * being specified by <code>conf</code> and <code>rbt_wheel_base
     * @param o the play's list of things to run through
     * @param conf the configuration of the robot/play
     * @param rbt_wheel_base the width of the robot
     * @return the path generated from the given inputs, as saved in a <code>SpeedStorage</code> object
     */
    public static SpeedStorage createPath(ArrayList<GUIObject> o, TrajectoryGenerator.Config conf, double rbt_wheel_base)
    {
        cnfg = conf;
        ArrayList<GUIObject> objs = copyGUIRay(o);
        ArrayList<PositionCommandGroup> stopGroups = new ArrayList<>();
        ArrayList<GUIObject> lines = new ArrayList<>();
        for(int i = 4; i<objs.size(); i++)
        {
            GUIObject obj = objs.get(i);
            if(obj instanceof PositionCommandGroup && ((PositionCommandGroup)obj).stop_robot)
            {
                stopGroups.add(((PositionCommandGroup)obj));
                //System.out.println("Stopper found at " + ((PositionCommandGroup)obj).pos);
            }
            else if(obj instanceof StraightLine || obj instanceof SmoothLine)
            {
                lines.add(obj);
            }
        }
        for(int i = 0; i<lines.size(); i++)
        {
            GUIObject obj = lines.get(i);
            
            if(obj instanceof StraightLine)
            {
                int index = -1;
                for(PositionCommandGroup grp : stopGroups)
                {
                    if(grp.stop_robot == true && PositionUtil.onLine(grp.pos, ((StraightLine)obj).initialPosition, ((StraightLine)obj).finalPosition))
                    {
                        //System.out.println("Yo group on line");
                        StraightLine newLine = new StraightLine();
                        newLine.initialPosition = new Point(grp.pos);
                        newLine.finalPosition = new Point(((StraightLine)obj).finalPosition);
                        double d1 = PositionUtil.distance(((StraightLine)obj).initialPosition, ((StraightLine)obj).finalPosition);
                        double d2 = PositionUtil.distance(((StraightLine)obj).initialPosition, grp.pos);
                        ((StraightLine)obj).finalPosition = new Point(grp.pos);
                        lines.add(i+1, newLine);
                        //System.out.println(d1 + " " + d2 + " " + d2/d1 + " " + ((StraightLine)obj).time);
                        newLine.time = ((StraightLine)obj).time * d2/d1;
                        //System.out.println(newLine.time);
                        index = stopGroups.indexOf(grp);
                        break;
                    }
                }
                if(index != -1) stopGroups.remove(index);
            }
            else if(obj instanceof SmoothLine)
            {
                SmoothLine ln = (SmoothLine)obj;
                if(lines.size() != i+1){
                while(lines.get(i+1) instanceof SmoothLine)
                {
                    for(Point p : ((SmoothLine)lines.get(i+1)).coordinates)
                    {
                        ln.coordinates.add(new Point(p));
                    }
                    ln.maxTime += ((SmoothLine)lines.get(i+1)).maxTime;
                    lines.remove(i+1); 
                }
                }
                double totalDistance = 0;
                for(int j = 1; j<ln.coordinates.size(); j++)
                {
                    totalDistance += PositionUtil.distance(ln.coordinates.get(j), ln.coordinates.get(j-1));
                }
                double tempDist = 0;
                for(int j = 1; j<ln.coordinates.size(); j++)
                {
                    tempDist += PositionUtil.distance(ln.coordinates.get(j), ln.coordinates.get(j-1));
                    int index = -1;
                    for(PositionCommandGroup grp : stopGroups)
                    {
                        if(grp.stop_robot == true && PositionUtil.onLine(grp.pos, ((SmoothLine)obj).coordinates.get(j-1), ((SmoothLine)obj).coordinates.get(j)))
                        {
                            double d1 = totalDistance - PositionUtil.distance(ln.coordinates.get(j), grp.pos);                            
                            System.out.println("Yo group on smooth line");
                            SmoothLine newLine = new SmoothLine();
                            newLine.coordinates = new ArrayList<>();
                            newLine.coordinates.add(new Point(grp.pos));
                            double time = ln.maxTime;
                            //System.out.println(time);
                            newLine.maxTime = time * (totalDistance - d1)/totalDistance;
                            ln.maxTime = time*d1/totalDistance;
                            
                            //System.out.println(ln.maxTime);
                            //System.out.println(newLine.maxTime);
                            while(ln.coordinates.size() > j)
                            {
                                newLine.coordinates.add(new Point(ln.coordinates.get(j)));
                                ln.coordinates.remove(j);
                            }
                            ln.coordinates.add(new Point(grp.pos));
                            lines.add(i+1, newLine);
                            index = stopGroups.indexOf(index);
                        }
                    }
                    if(index != -1)
                    {
                        stopGroups.remove(index);
                        break;
                    }
                }
            }
        }
        
        SpeedStorage stg = new SpeedStorage();
        int numPoints = 0;
        int numThings = 0;
        double totaltime = 0;
        for(GUIObject g : lines)
        {
            System.out.println("New Line: " + g.getClass());
            double[][] path = null;
            double time = 0;
            if(g instanceof StraightLine)
            {
                StraightLine sl = (StraightLine)g;
                double dx = sl.finalPosition.getX() - sl.initialPosition.getX();
                double dy =  sl.finalPosition.getY() - sl.initialPosition.getY();
                Trajectory t = TrajectoryGenerator.generate(getCnfg(), TrajectoryGenerator.SCurvesStrategy, 0, Math.atan2(dy,dx), PositionUtil.distance(sl.finalPosition,sl.initialPosition), 0, Math.atan2(dy, dx));
                
                for(Trajectory.Segment s : t.segments_)
                {
                    double x = Math.cos(s.heading) * s.pos + sl.initialPosition.getX();
                    double y = Math.sin(s.heading) * s.pos + sl.initialPosition.getY();
                    //System.out.println("SL: " + x + "," + y);
                    stg.headings.add(s.heading);
                    stg.robot_points.add(new double[] {x,y});
                    stg.left_track_speeds.add(s.vel);
                    stg.right_track_speeds.add(s.vel);
                }
                
            }
            else if(g instanceof SmoothLine)
            {
                SmoothLine ln = (SmoothLine)g;
                //path = new double[ln.coordinates.size()][2];
                double heading1 = 0;
                //if(lines.indexOf(g) == 0)
                //{
                    double dx0 = ln.coordinates.get(1).getX() - ln.coordinates.get(0).getX();
                    double dy0 = ln.coordinates.get(1).getY() - ln.coordinates.get(0).getY();
                    heading1 = Math.atan2(dy0,dx0);
                /*}
                else
                {
                    GUIObject prev = lines.get(lines.indexOf(g)-1);
                    if(prev instanceof StraightLine)
                    {
                        double dx = ((StraightLine)prev).finalPosition.getX() - ((StraightLine)prev).initialPosition.getX();
                        double dy = ((StraightLine)prev).finalPosition.getY() - ((StraightLine)prev).initialPosition.getY();
                        heading1 = Math.atan2(dy,dx);
                    }
                    else
                    {
                        SmoothLine prevLn = (SmoothLine)prev;
                        
                        int max = prevLn.coordinates.size()-1;
                        
                        double dx = prevLn.coordinates.get(max).getX() - prevLn.coordinates.get(max-1).getY();
                        double dy = prevLn.coordinates.get(max).getY() - prevLn.coordinates.get(max-1).getY();
                        heading1 = Math.atan2(dy,dx);
                    }
                }
                */
                double v0 = 0;
                double totalDist = 0;
                double totalInc = 0;
                double totalDec = 0;
                double d = ln.coordinates.size();
                d = d/2;
                for(int i = 0; i<(int)d; i++)
                {
                    totalInc += PositionUtil.distance(ln.coordinates.get(i), ln.coordinates.get(i+1));
                }
                for(int i = (int)d+1; i<ln.coordinates.size()-1; i++)
                {
                    totalDec += PositionUtil.distance(ln.coordinates.get(i), ln.coordinates.get(i+1));
                }
                for(int i = 0; i<ln.coordinates.size()-1; i++)
                {
                    double dx = ln.coordinates.get(i+1).getX() - ln.coordinates.get(i).getX();
                    double dy = ln.coordinates.get(i+1).getY() - ln.coordinates.get(i).getY();
                    double heading2 = Math.atan2(dy,dx);
                    
                    double dist = PositionUtil.distance(ln.coordinates.get(i), ln.coordinates.get(i+1));
                                  
                    //System.out.println("H1 " + heading1 + " H2 " + heading2);
                    //System.out.println((dist*Math.cos(heading1) + ln.coordinates.get(i).getX()) + "," + (dist*Math.sin(heading1) + ln.coordinates.get(i).getY()));
                    
                    double v1 = getCnfg().max_vel/4;
                    if(i<=d)
                    {
                        v1 = v0 + dist/totalInc * (ln.maxTime/2) * getCnfg().max_acc/10;
                    }
                    else
                    {
                        v1 = v0 - dist/totalDec * (ln.maxTime/2) * getCnfg().max_acc/10;
                    }
                    if(i == ln.coordinates.size()-2)
                    {
                        v1 = 0;
                    }
                    double v1t = v1;
                    double v0t = v0;
                    
                    if(v1 > getCnfg().max_vel)
                    {
                        v1t = getCnfg().max_vel;
                    }
                    if(v0 > getCnfg().max_vel)
                    {
                        v0t = getCnfg().max_vel;
                    }

                    //System.out.println(i + " V0 " + v0t  + " "  + v1 + " V1 " + v1t + " pos " + PositionUtil.distance(ln.coordinates.get(i), ln.coordinates.get(i+1)) + " dist " + dist);
                    //Trajectory t = TrajectoryGenerator.generate(getCnfg(),TrajectoryGenerator.AutomaticStrategy,v0t,heading1,PositionUtil.distance(ln.coordinates.get(i), ln.coordinates.get(i+1)),v1t,heading2);
                    Trajectory t = TrajectoryGenerator.generate(getCnfg(),TrajectoryGenerator.AutomaticStrategy,0,heading1,PositionUtil.distance(ln.coordinates.get(i), ln.coordinates.get(i+1)),0,heading2);

                    
                    //System.out.println(ln.coordinates.get(i));
                    double prevX=0;
                    double prevY=0;
                    double x=0;
                    double y=0;
                    for(int index = 0; index<t.segments_.length; index++)
                    {
                        Trajectory.Segment s = t.segments_[index];
                        double prevHeading;
                        if(index == 0)
                        {
                            prevHeading = t.segments_[0].heading;
                        }
                        else
                        {
                            prevHeading = t.segments_[index-1].heading;
                        }
                        
                        prevX = x;
                        prevY = y;
                        numPoints++;
                        x = Math.cos(s.heading) * s.pos + ln.coordinates.get(i).getX();
                        y = Math.sin(s.heading) * s.pos + ln.coordinates.get(i).getY();
                        stg.headings.add(s.heading);
                        stg.robot_points.add(new double[] {x,y});
                        double[] spds = calcSpeed(s.vel, s.heading, prevHeading, rbt_wheel_base, getCnfg().dt);
                        stg.left_track_speeds.add(spds[0]);
                        stg.right_track_speeds.add(spds[1]);
                        //System.out.println("SmoothLine: " + x + " " + y);
                    }
                     //System.out.println(ln.coordinates.get(i+1));
                     double delx = x-prevX;
                     delx = delx/getCnfg().dt;
                     double dely = y-prevY;
                     dely = dely/getCnfg().dt;
                     //System.out.println(pythagoras(new double[] {delx,dely}) + " " + stg.left_track_speeds.get(stg.left_track_speeds.size()-1));
                     v0 = pythagoras(new double[] {delx, dely});
                     double prev4X = stg.robot_points.get(stg.robot_points.size() - 7)[0];
                     double prev4Y = stg.robot_points.get(stg.robot_points.size() - 7)[1];
                     dx = x-prev4X;
                     dy = y-prev4Y;
                     heading1 = Math.atan2(dy,dx);
                }
                time = ((SmoothLine)g).maxTime;
            }
            totaltime += time;
            System.out.println(time);
            
        }
        return stg;
    }
    
    /**
     * This copies an arraylist of GUIObjects into a new ArrayList so that the old things are not modified in the process of 
     * determining the path, as that would cause the user to potentially see major changes in his/her play after trying to view it.
     * @param objs the original ArrayList
     * @return a copy of the original exactly the same but without the connections to the AutoPathCanvas
     */
    public static ArrayList<GUIObject> copyGUIRay(ArrayList<GUIObject> objs)
    {
        ArrayList<GUIObject> o = new ArrayList<>();
        
        for(GUIObject obj : objs)
        {
            if(obj instanceof StraightLine)
            {
                StraightLine Sl = (StraightLine) obj;
                StraightLine toCopy = new StraightLine();
                toCopy.initialPosition = new Point(Sl.initialPosition);
                toCopy.finalPosition = new Point(Sl.finalPosition);
                toCopy.time = Sl.time;
                
                o.add(toCopy);
            }
            else if(obj instanceof SmoothLine)
            {
                SmoothLine sm = (SmoothLine)obj;
                SmoothLine toCopy = new SmoothLine();
                for(Point p : sm.coordinates)
                {
                    toCopy.coordinates.add(new Point(p));
                }
                toCopy.maxTime = sm.maxTime;
                
                o.add(toCopy);
            }
            else if(obj instanceof PositionCommandGroup)
            {
                PositionCommandGroup grp = (PositionCommandGroup)obj;
                PositionCommandGroup toCopy = new PositionCommandGroup();
                
                for(Command c : grp.cmds)
                {
                    Command newC = new Command();
                    newC.command_int = c.command_int;
                    newC.param1 = c.param1;
                    newC.param2 = c.param2;
                    newC.concurrent_with_next = c.concurrent_with_next;
                    toCopy.cmds.add(newC);
                }
                toCopy.pos = new Point(grp.pos);
                toCopy.stop_robot = grp.stop_robot;
                
                o.add(toCopy);
            }
            else if(obj instanceof TimeCommandGroup)
            {
                TimeCommandGroup grp = (TimeCommandGroup)obj;
                TimeCommandGroup toCopy = new TimeCommandGroup();
                
                for(Command c : grp.cmds)
                {
                    Command newC = new Command();
                    newC.command_int = c.command_int;
                    newC.param1 = c.param1;
                    newC.param2 = c.param2;
                    newC.concurrent_with_next = c.concurrent_with_next;
                    toCopy.cmds.add(newC);
                }
                toCopy.pos = new Point(grp.pos);
                toCopy.stop_robot = grp.stop_robot;
                toCopy.time = grp.time;
                
                o.add(toCopy);
            }
        }
        
        return o;
    }
    
    /**
     * Figures out all of the locations of the PositionCommandGroups on the generated path <br>
     * Specifically this means that the method finds out at which index in the generated path each PositionCommandGroup falls on
     * @param objs the original ArrayList of objects
     * @param s the SpeedStorage data storage representing the class
     * @return 
     */
    public static int[] getPCmDLocation(ArrayList<GUIObject> objs, SpeedStorage s)
    {
        ArrayList<GUIObject> o = copyGUIRay(objs);
        
        ArrayList<PositionCommandGroup> g = new ArrayList<>();
        
        for(int i = 4; i<o.size(); i++)
        {
            GUIObject obj = o.get(i);
            if(obj instanceof PositionCommandGroup)
            {
                g.add((PositionCommandGroup)obj);
            }
        }
        int[] grpLocs = new int[g.size()];
        for(PositionCommandGroup grp : g)
        {
            int index = 0;
            double min = Double.MAX_VALUE;
            for(int i = 0; i<s.robot_points.size(); i++)
            {
                double[] d = s.robot_points.get(i);
                double deltax = d[0] - grp.pos.getX();
                double deltay = d[1] - grp.pos.getY();
                double dist = Math.sqrt(Math.pow(deltax, 2) + Math.pow(deltay, 2));
                if(min > dist)
                {
                    index = i;
                    min = dist;
                }
            }
            grpLocs[g.indexOf(grp)] = index;
        }
        
        return grpLocs;
    }
    
    
    /**
     * pythagoras is a method that finds out the distance between two points 
     * given a list of deltas (Delta_x,Delta_y,Delta_z,Delta_t, etc). The method accepts
     * double[] of any size and it can calculate the distance given as many deltas as you want
     * @param d a list of deltas (Delta_x,Delta_y,Delta_z,Delta_t, etc)
     * @return the distance between two points separated by the given deltas along the axes
     */
    public static double pythagoras(double[] d)
    {
        double total = 0;
        for(double dbl : d)
        {
            total += Math.pow(dbl, 2);
        }
        return Math.sqrt(total);
    }
    
    /**
     * Test method
     * @param args 
     */
    public static void main(String[] args)
    {
        Trajectory t = TrajectoryGenerator.generate(getCnfg(), TrajectoryGenerator.AutomaticStrategy, 0, -1.2589682692342312, 905.2071586106686, 0, -1.2589682692342312);
        double[][] ray = new double[t.segments_.length][2];
        for(int i = 0; i<t.segments_.length; i++)
        {
            Trajectory.Segment s = t.segments_[i];
            double x = Math.cos(s.heading) * s.pos + 0;
            double y = Math.sin(s.heading) * s.pos + 0;
            //System.out.println(s.x + "," + s.y + " {" + s.heading + "}" + " (" + (s.heading - 0.5) + ")");  
            //System.out.println(x + "," + y);
            ray[i][0] = x;
            ray[i][1] = y;
        }
        
        FalconLinePlot pl = new FalconLinePlot(ray);
        
    }
    
    /**
     * Calculate the speed of the wheels given the current robot speed, the current heading and previous heading, 
     * the width of the robot and the delta_t
     * @param robot_speed the speed of the robot
     * @param prev_head the heading of the robot 1 time_step previously
     * @param head the current heading of the robot
     * @param robot_track_width the width of the robot
     * @param time_step the time between the previous heading and the current heading
     * @return the wheel speeds given the above inputs
     */
    public static double[] calcSpeed(double robot_speed, double prev_head, double head, double robot_track_width, double time_step)
    {
        double left_track_spd = 0;
        double right_tracl_spd = 0;
        
        double dTheta = prev_head - head;
        double omega = dTheta/time_step;
        
        left_track_spd = robot_speed + omega*robot_track_width/2;
        right_tracl_spd = robot_speed - omega*robot_track_width/2;
        
        return new double[] {left_track_spd, right_tracl_spd};
    }
}
