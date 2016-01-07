/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI.objects;

import java.awt.Point;

/**
 * Data Storage Class for Straight Lines
 * @author Alex
 */
public class StraightLine implements GUIObject{
    public Point initialPosition;
    public Point finalPosition;
    public double time;
    
    public StraightLine()
    {
        initialPosition = new Point();
        finalPosition = new Point();
    }
    
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof StraightLine))
        {
            return false;
        }
        
        StraightLine l = (StraightLine) o;
        
        return (l.initialPosition.equals(initialPosition) && l.finalPosition.equals(finalPosition) && l.time == time);
    }
}
