/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI.objects;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Data Storage Class for Smooth Lines
 * @author Alex
 */
public class SmoothLine implements GUIObject{
    public ArrayList<Point> coordinates = new ArrayList<>();
    public double maxTime;
    
    public SmoothLine()
    {
        coordinates = new ArrayList();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof SmoothLine))
        {
            return false;
        }
        SmoothLine ln = ((SmoothLine) o);
        
        return (ln.coordinates.equals(coordinates) && ln.maxTime == maxTime);
    }
}
