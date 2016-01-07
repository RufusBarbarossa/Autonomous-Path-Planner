/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI.objects;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Data Storage Class for Time Command Groups
 * @author Alex
 */
public class TimeCommandGroup implements GUIObject{
    public ArrayList<Command> cmds;
    public boolean stop_robot;
    public double time;
    public Point pos;
    
    public TimeCommandGroup()
    {
        cmds = new ArrayList();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof TimeCommandGroup))
        {
            return false;
        }
        TimeCommandGroup g = (TimeCommandGroup) o;
        return (g.cmds.equals(cmds) && g.stop_robot == stop_robot && g.time == time);
    }
}
