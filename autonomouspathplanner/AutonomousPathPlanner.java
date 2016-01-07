/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner;

import autonomouspathplanner.GUI.AutoPathFrame;

/**
 * The "Main Class" of the project. It simply starts up an auto path frame and allows it to run unhindered.
 * @author Alex
 */
public class AutonomousPathPlanner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AutoPathFrame frm = new AutoPathFrame();
        frm.setVisible(true);
        frm.setSize(1600,1000);
    }
    
}
