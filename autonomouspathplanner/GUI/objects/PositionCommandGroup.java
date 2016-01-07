/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI.objects;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Data Storage class for Position Command Groups
 * @author Alex
 */
public class PositionCommandGroup implements GUIObject{
    public ArrayList<Command> cmds;
    public boolean stop_robot;
    public Point pos;
    
    public PositionCommandGroup()
    {
        cmds = new ArrayList();
        pos = new Point();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof PositionCommandGroup))
        {
            return false;
        }
        PositionCommandGroup g = (PositionCommandGroup) o;
        return (g.cmds.equals(cmds) && g.stop_robot == stop_robot && g.pos.equals(pos));
    }
}
