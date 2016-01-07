/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

/**
 * A canvas object that shows if the user is connected or not
 * @author Alex
 */
public class connectionIndicator extends Canvas{
    /**
     * is the user connected to a roboRIO?
     */
    public boolean connected;
    
    /**
     * Tell the connectionIndicator to change its display to match whether it is connected or not
     * @param c whether it is connected or not
     */
    public void setConnected(boolean c)
    {
        connected = c;
        if(c)
        {
            //this.setBounds(this.getX(), this.getX(), this.getHeight(), 500);
        }
        else
        {
           // this.setBounds(this.getX(), this.getX(), this.getHeight(), 1000);
        }
        
        repaint();
    }
    
    @Override
    public void paint(Graphics g)
    {
        if(connected)
        {
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.SERIF, Font.BOLD, this.getHeight()-4));
            g.drawString("ROBO-RIO CONNECTED", 1, this.getHeight()-3);
        }
        else
        {
            g.setColor(Color.RED);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.SERIF, Font.BOLD, this.getHeight()-4));
            g.drawString("NO ROBORIO CONNECTION", 1, this.getHeight()-3);
        }
    }
}
