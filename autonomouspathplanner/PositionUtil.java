/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner;

import autonomouspathplanner.GUI.AutoPathCanvas;
import static autonomouspathplanner.GUI.AutoPathCanvas.startObjs;
import autonomouspathplanner.GUI.objects.Command;
import autonomouspathplanner.GUI.objects.GUIObject;
import autonomouspathplanner.GUI.objects.PositionCommandGroup;
import autonomouspathplanner.GUI.objects.SmoothLine;
import autonomouspathplanner.GUI.objects.StraightLine;
import autonomouspathplanner.GUI.objects.TimeCommandGroup;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;

/**
 * Utility class that deals with various position-relating things that might be required such as calculating distance, checking if a point is o a line, etc.
 * @author Alex
 */
public class PositionUtil {
    /**
     * meters_per_tic is a conversion between pixels and meters. To change the actual value of this, reference <code>constants.java</code>
     */
    public static final double meters_per_tic = constants.meters_per_tic;
    
    /**
     * This is a method that determines if the Point p is "near" the GUIObject obj.
     * This is used for the determination of whether an object was clicked or dragged
     * "Near" is defined in several ways depending on the type of the GUIObject.
     * <ul>
     * <li>For <code>PositionCommandGroup</code> "near" is defined as being within the frame 
     * of the PositionCommandGroup</li>
     * <li>For <code>StraightLine</code>, "near" is defined as being within a square of size = <code>2*line_delta</code> 
     * centered around one of the endpoints.</li>
     * <li>For <code>SmoothLine</code> "near" is defined as being within a square of size = <code>2*line_delta</code> 
     * centered around any of the points that denotes the SmoothLine.</li>
     * </ul>
     * <b>A note on methodology:</b> <br>
     * we can say a Point p is within a square with center (x,y) and side length s, if and only if BOTH <br>
     * <code>withinBounds(x,p.getX(),s/2)</code> AND<br>
     * <code>withinBounds(y,p.getY(),s/2)</code> evaluate to <code>true</code><br>
     * In other words, we MUST have that both the x and y coordinates of p are within s/2 distance of x and y respectively.
     * @param obj the GUIObject that will be checked to see if <code>p</code> is close enough
     * @param p the point which we are checking to see if it is close enough to the point in question
     * @return true if <code>p</code> is "near" <code>obj</code> as defined above; false otherwise
     */
    public static boolean nearObject(GUIObject obj, Point p)
    {
        if(obj instanceof PositionCommandGroup)
        {
            return(withinBounds(((PositionCommandGroup)obj).pos.x,p.x,AutoPathCanvas.command_group_size/2) && withinBounds(((PositionCommandGroup)obj).pos.y,p.y,AutoPathCanvas.command_group_size/2));
        }
        else if (obj instanceof StraightLine)
        {
            return((withinBounds(((StraightLine)obj).initialPosition.x, p.x, 10) && (withinBounds(((StraightLine)obj).initialPosition.y, p.y, 10))) || (withinBounds(((StraightLine)obj).finalPosition.x, p.x, 10) && (withinBounds(((StraightLine)obj).finalPosition.y, p.y, 10))));
        }
        else if (obj instanceof SmoothLine)
        {
            for(int i = 0; i<((SmoothLine)obj).coordinates.size(); i++)
            {
                Point pt = new Point(((SmoothLine)obj).coordinates.get(i));
                if(withinBounds(pt.x,p.x,10) && withinBounds(pt.y,p.y,10))
                {
                    return true;
                }
            }
            return false;
        }
        else if (obj instanceof TimeCommandGroup)
        {
            return(withinBounds(((TimeCommandGroup)obj).pos.x,p.x,AutoPathCanvas.command_group_size/2) && withinBounds(((TimeCommandGroup)obj).pos.y,p.y,AutoPathCanvas.command_group_size/2));
        }
        return false;
    }
    
    //*********MISCALLANEOUS*******
    
    /**
     * Reads a point from a string representing said point. 
     * String must be of the form <code>(x,y)</code> where x and y are the x and y coordinates of a point.
     * <br> WARNING: X and Y MUST BE INTEGERS
     * @param p the String containing the point that must be parsed
     * @return the Point object represented by that string
     */
    public static Point parsePoint(String p)
    {
        char[] chs = p.toCharArray();
        int index = -1;
        for(int i = 1; i<chs.length; i++)
        {
            if(chs[i] == ',')
            {
                index = i;
                break;
            }
        }
        if(index == -1)
        {
            throw new NumberFormatException();
        }
        else
        {
            return new Point(Integer.parseInt(p.substring(1, index)),Integer.parseInt(p.substring(index + 1, p.length()-1)));
        }
    }
    
    /**
     * Checks if the two of <code>x</code> and <code>y</code> are within <code>epsilon</code> of each other. Since the code uses 
     * less than or equal to and greater than or equal to a value of <code> y = x - epsilon </code> or <code>y = x + epsilon</code> will still return true.<br>
     * Note that x and y are interchangeable in terms of the logic of the method. In other words, x and y are order agnostic.
     * @param x one of the two doubles to be checked if they are close to each other
     * @param y other of the two doubles to be checked if they are close to each other.
     * @param epsilon the maximum distance on either side that one of <code>x</code> or <code>y</code> can be from the other
     * @return true if x and y are within epsilon of each other, false otherwise
     */
    public static boolean withinBounds(double x, double y, double epsilon) {
        boolean a = x <= (y + epsilon);
        boolean b = x >= (y - epsilon);
        
        return a && b;
    }
    
    /**
     * Method that searches for the closest point to the parameter (<code>p</code>) that falls on
     * a SmoothLine or a StraightLine <br>
     * KNOWN BUG: The method will also consider the pallete lines when searching for a point that falls on a 
     * SmoothLine or a StraightLine and may decide that the nearest point is on one of those.
     * 
     * @param p the Point that we are looking to find the closest other point to
     * @return the Point on a line closest to p
     */
    public static Point closestPointonLine(ArrayList<GUIObject> objs, Point p)
    {
        //Define our temporary variables before the for loop
        Point closest = new Point();
        double mindistance = Integer.MAX_VALUE;
        
        //Iterate over all GUIObjects
        for(GUIObject obj : objs)
        {
            //We need to not check PositionCommandGroups and only check SmoothLines and StraightLines
            if(obj instanceof SmoothLine)
            {
                //Create a Smoothline object to avoid the clutter of casting obj to SmoothLine every time we need to call it.
                SmoothLine ln = ((SmoothLine)obj);
                
                //For every singe coordinate, we must create a line using that one and the previous one
                //and verify if this is the line with the closest point
                for(int i = 1; i<ln.coordinates.size(); i++)
                {
                    //We can mathematically determine the closest point on a line to a third point, so lets find that point
                    Point t = closestPointToLine(p, new Point(ln.coordinates.get(i-1)), new Point(ln.coordinates.get(i)));
                    
                    //if the distance to the closest point on this line is smaller than the smallest distance up to now, then
                    //this is our new closest point and its distance the new closest distance
                    double d = distance(p, t);
                    if(d < mindistance)
                    {
                        mindistance = d;
                        closest = t;
                    }
                    
                }
            }
            else if(obj instanceof StraightLine)
            {
                //Create a StraightLine variable to avoid the cluster of casting every time we want to call obj
                StraightLine ln = ((StraightLine)obj);
                
                //We can mathematically determine the closest point on a line to a third point, so lets find that point
                Point t = closestPointToLine(p, new Point(ln.initialPosition), new Point(ln.finalPosition));
                
                //if the distance to the closest point on this line is smaller than the smallest distance up to now, then
                //this is our new closest point and its distance the new closest distance
                double d = distance(p, t);
                if(d < mindistance)
                {
                    mindistance = d;
                    closest = t;
                }
            }
        }
        return closest;
    }

    /**
     * Determines the closest point on the line segment given by <code>V0</code> and <code>V1</code> to a third point <code>Q</code>. <br>
     * This program will first attempt to find the closest point on the line given by <code>V0</code> and <code>V1</code> and then determines if the 
     * point is on the line segment. If not, the result outputted will be the closer of V0 or V1. 
     * @param Q The point that we wish to find the closest point on the line segment to
     * @param V0 one endpoint of a line segment
     * @param V1 other endpoint of a line segment
     * @return the closest point on the line segment defined by V0 and V1 to a third point Q.
     */
    public static Point closestPointToLine(Point Q, Point V0, Point V1)
    {
        //Vx and Vy can also be called Delta X and Delta T
        double Vx = V1.getX() - V0.getX();
        double Vy = V1.getY() - V0.getY();
        //P is the dummy variable for the result
        Point p = new Point();
        
        //If the line is vertical (Vx == 0) or horizontal (Vy == 0), the problem is much more
        //simple and does not require all of these calculations that could also possibly create divide by 0 errors.
        if(Vx != 0 && Vy != 0)
        {
            //Line V defined by V0 and V1 can be written in the form y = mv*x + bv
            double mv = Vy/Vx;
            double bv= V0.getY() - mv*V0.getX();
            
            //It is known that the line formed by P (the closest point on the line to Q) and Q will be perpendicular
            //to Line V, so let us calculate it's equation
            //Line S, perpendicular to V and passing through Q, also of the form y = ms*x + bs
            double ms = -(1/mv);
            double bs = Q.getY() - ms*Q.getX();
            
            //we do not need to check for ms == mv as that would necessitate mv == -1/mv or mv^2 = -1 which is impossible as mv is a real number
            
            //Do some nice math to figure out (x,y) for P
            double x = (bv - bs)/(ms - mv);
            double y = mv*x+bv;
            
            //convert line V into vector notation in order to make sure 
            //that we arent outside of the line segment
            
            //By the way I have defined the line, t(V0) = 0 and t(V1) = tmax
            double tmax = (V1.getX() - V0.getX())/(Vx);
            //t is the time at (x,y)
            double t = (x - V0.getX())/(Vx);
            
            //if tmax is less than 0, or in other words v0 is further up the line
            //then if t>0 we have t being too big and V0 is the closest, and if t<tmax then
            //we hve t being too small and V1 is the closest.
            
            //otherwise if tmax is greater than 0, we have the two options of if t<0 (meaning t is too small
            //and V0 is the closest) or t>tmax (meaning t is too big and V1 is the closest)
            if(tmax <0)
            {
                if(t>0)
                {
                    return V0;
                }
                else if(t<tmax)
                {
                    return V1;
                }
            }
            else
            {
                if(t<0)
                {
                    return V0;
                }
                else if(t>tmax)
                {
                    return V1;
                }
            }
            
            //If we have oassed the above if statements, then p is within the line segment, and we should return it (but only after specidying its location
            p.setLocation(((int) x), ((int)y));
            
            return p;
        }
        else if(Vx == 0)
        {
            //If Vx == 0, the line is vertical and we only need to check Y-coordinates.
            if(Q.getY() < V0.getY() && Q.getY() <V1.getY())
            {
                //in this case, we know that Q is lower than the entire line segment, so we simply have to know 
                //which of V0 or V1 is lower and therefore closer, as we will return that one
                if(V0.getY() < V1.getY())
                {
                    return V0;
                }
                else
                {
                    return V1;
                }
            }
            else if(Q.getY() > V0.getY() && Q.getY() > V1.getY())
            {
                //in this case, we know that Q is bigger than the entire line segment, so we simply have to know 
                //which of V0 or V1 is higher and therefore closer, as we will return that one
                if(V0.getY() >V1.getY())
                {
                    return V0;
                }
                else
                {
                    return V1;
                }
            }
            else
            {
                //otherise, Q is somewhere between the two points (i.e. on the line segment
                //In this case the closest point is simply the one that shares the X-coordinate of the line and 
                // is at the y-coordinate of q
                return new Point((int)V0.getX(), (int)Q.getY());
            }
        }
        else
        {
            //If Vy == 0, the line is horizontal and we only need to check X-coordinates.
            if(Q.getX() < V0.getX() && Q.getX() <V1.getX())
            {
                //in this case, we know that Q is to the left of the entire line segment, so we simply have to know 
                //which of V0 or V1 is more to the left and therefore closer, as we will return that one
                if(V0.getX() < V1.getX())
                {
                    return V0;
                }
                else
                {
                    return V1;
                }
            }
            else if(Q.getX() > V0.getX() && Q.getX() > V1.getX())
            {
                //in this case, we know that Q is to the right of the entire line segment, so we simply have to know 
                //which of V0 or V1 is more to the right and therefore closer, as we will return that one
                if(V0.getX() >V1.getX())
                {
                    return V0;
                }
                else
                {
                    return V1;
                }
            }
            else
            {
                //otherise, Q is somewhere between the two points (i.e. on the line segment
                //In this case the closest point is simply the one that shares the y-coordinate of the line and 
                // is at the x-coordinate of q
                return new Point((int)Q.getX(), (int)V0.getY());
            }
        }
    }
    
    /**
     * Calculates the distance between two points using the Pythagorean theorem
     * @param a the first point
     * @param b the second point
     * @return the distance between the two points
     */
    public static double distance(Point a, Point b)
    {
        double x = a.getX() - b.getX();
        double y = a.getY() - b.getY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
    
    /**
     * Determine whether the point q falls on the line segment from V0 to V1
     * @param q the point to determine whether it falls on the line segment
     * @param V0 one of the endpoints of the line segment
     * @param V1 the other endpoint of the line segment
     * @return true if q falls on the line segment; false otherwise
     */
    public static boolean onLine(Point q, Point V0, Point V1)
    {
        Point p = closestPointToLine(q, V0, V1);
        /**
        //if either of the x or y coordinates of q are greater than both of or less than both of V0 and V1's x or y coordinates (respectively)
        //then we can safely say that the point is not on the line segment EVEN IF it is on the line.
        //Basically if the execution of this if loop does not happen and we move on, we know that Q is within a rectangle with two corners
        //(specifically opposite corners) being V0 and V1
        if(((q.getX() < V0.getX() + 10 && q.getX() < V1.getX() + 10) || (q.getX() > V0.getX()-10 && q.getX() > V1.getX()-10)) || ((q.getY() < V0.getY()+10 && q.getY() < V1.getY()+10) || (q.getY() > V0.getY()-10 && q.getY() > V1.getY()-10)))
        {
            return false;
        }
        */
        if(q.equals(V0)) return false;
        if(q.equals(V1)) return true;
        return distance(q,p) <15;
        
        /**
        //Vx and Vy are the delta's of the line segment;
        //These deltas are also the rectangle's dimensions
        double Vx = V1.getX() - V0.getX();
        double Vy = V1.getY() - V0.getY();
        
        if(Vx == 0)
        {
            //if Vx is 0, then we know that the line segment is vertical, and we 
            //know that the point is on the line, as the rectangle mentione above (by the first if statement)
            //now has no width, meaning it is really a line corresponding to the line V
            return true;
        }
        else if(Vy == 0)
        {
            //if Vy is 0, then we know that the line segment is horizontal, and we 
            //know that the point is on the line, as the rectangle mentione above (by the first if statement)
            //now has no height, meaning it is really a line corresponding to the line V
            return ((q.getX() < V0.getY() +10) && (q.getX() > V0.getY() -10));
        }
        
        //write the line in mx+b form
        double m = Vy/Vx;
        double b = V0.getY() - m*V0.getX();
        
        //if q.gety == m*q.getX() + b, then it is on the line.
        //The 0.5 simply exists just in case and provides a small leeway.
        return withinBounds(q.getY(), m*q.getX() + b, 20);
                */
    }
    
    /**
     * Converts a point object to a double[]. This method is called by various sub-programs to for calculations.
     * @param p
     * @return 
     */
    public static double[] pointToRay(Point p)
    {
        double[] d = new double[2];
        d[0] = p.getX();
        d[1] = p.getY();
        return d;
    }
    
    /**
     * Given a path in "Pixel location" convert this to a more intuitive path with units of meters
     * where y=0 is in the bottom rather than the top and y++ moves an object up rather than down.
     * @param ray the path in "pixel locations" as a double[][2]
     * @return the path in the more intuitive Cartesian coordinate system with units of meters rather than pixels
     */
    public static double[][] convertPathToMeters(double[][] ray)
    {
        double[][] newR = new double[ray.length][2];
        for(int i = 0; i<ray.length; i++)
        {
            newR[i] = convertPointToMeters(ray[i]);
        }
        return newR;
    }
    
    /**
     * Converts the point represented by the double[2] d from its pixel location
     * to a Cartesian coordinate system where y=0 is at the bottom, y++ moves a point up and 
     * the unit is meters.
     * @param d the point as represented by a double array of size 2
     * @return the point in the Cartesian coordinate system.
     */
    public static double[] convertPointToMeters(double[] d)
    {
        double x = d[0];
        double y = d[1];
        
        y = AutoPathCanvas.time_loc_interface_y - y;
        
        x = x*meters_per_tic;
        y = y*meters_per_tic;
        
        double[] newD = new double[2];
        newD[0] = x;
        newD[1] = y;
        
        return newD;
    }
}
