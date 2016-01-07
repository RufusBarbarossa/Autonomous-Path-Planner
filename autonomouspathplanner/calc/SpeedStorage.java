/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.calc;

import java.awt.Point;
import java.util.ArrayList;

/**
 * A utility class that stores Autonomous Paths and all of the data associated with them
 * @author Alex
 */
public class SpeedStorage {
    public ArrayList<double[]> left_track_points;
    public ArrayList<double[]> right_track_points;
    public ArrayList<double[]> robot_points;
    public ArrayList<Double> left_track_speeds;
    public ArrayList<Double> right_track_speeds;
    public ArrayList<Double> headings;
    
    public SpeedStorage()
    {
        left_track_points = new ArrayList<>();
        right_track_points = new ArrayList<>();
        robot_points = new ArrayList<>();
        left_track_speeds = new ArrayList<>();
        right_track_speeds = new ArrayList<>();
        headings = new ArrayList<>();
    }
}
