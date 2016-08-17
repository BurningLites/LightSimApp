//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//--------------------------------------------- LightViewer.java -----

package lightsim;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import java.util.Arrays;

import lightsim.LightArray.Light;

//======================================================================
// class LightViewer
//======================================================================

public class LightViewer extends JPanel
            implements KeyListener, MouseListener, MouseMotionListener
    {
    class Bounds
        {
        double min=Double.MAX_VALUE, max=Double.MIN_VALUE;
        public void adjust (double value)
            {
            if (value < min)    min = value;
            if (value > max)    max = value;
            }
        public double center()  { return (max + min) / 2.0; }
        public double length()  { return max - min; }
        }

    private LightArray my_light_arrays;
    private Light zlights[];
    private Bounds x_bnds, y_bnds, z_bnds;  // array bounds
    private double vx, vy, vz;              // coords of viewer
    private double scale;
    private int  mx, my, mxm1, mym1;        // mouse coordindates
    
  // ----- constructor -------------------------------------------------
  //
    public LightViewer (LightArray light_arrays)
        {
        my_light_arrays = light_arrays;

        x_bnds = new Bounds();
        y_bnds = new Bounds();
        z_bnds = new Bounds();
        
        for (Light l : my_light_arrays.getLights())
            {
            x_bnds.adjust (l.x);
            y_bnds.adjust (l.y);
            z_bnds.adjust (l.z);
            }

        // Center the light arrays.  The viewer moves around this
        // center.
        //
        double xc = x_bnds.center();
        double yc = y_bnds.center();
        double zc = z_bnds.center();
        for (Light l : my_light_arrays.getLights())
            {
            l.x = l.x - xc;
            l.y = l.y - yc;
            l.z = l.z - zc;
            }

        addComponentListener (new ComponentAdapter() {
            public void componentResized (ComponentEvent e)
                {
                init();
                }
            });
        }
    
  // ----- init() ------------------------------------------------------
  //
    public void init()
        {
        int width = getWidth();
        int height = getHeight();

      // Compute an initial z distance and scale based on fitting the
      // lights into 75% of the window's height.
      //
        double xbl = x_bnds.length(),
               ybl = y_bnds.length();
        vz = 4.0*ybl;
        scale = 0.75*width / ybl;

      // Does this work for the width of the light arrays?  If not,
      // revise the distance to the center of the light arrays and the
      // scale.
      //
        double x_prj = 0.75*height*xbl / ybl;
        if (x_prj > 0.75*width)
          { double d = (0.75*width*ybl*ybl) / (height*xbl - 0.75*width*ybl);
            vz = d + ybl;
            scale = 0.75 * width / xbl;
            }

        double alpha = Math.toRadians (15.0);
        double beta = Math.toRadians (7.0);

        double vz0 = vz;
        vx = -vz0 * Math.sin(alpha);    // Move a little to right
        vz = vz * Math.cos(alpha);
        vy = vz0 * Math.sin(beta);      // and a little down.
        vz = vz * Math.cos(beta);

        changeViewpoint();

        addKeyListener (this);
        requestFocus();
        addMouseListener (this);
        addMouseMotionListener (this);
        }
    
  // ----- changeViewpoint() -------------------------------------------
  //
    public void changeViewpoint()
        {
        double d = Math.sqrt (vx*vx + vz*vz);
        double vd = Math.sqrt (vx*vx + vy*vy + vz*vz);
            
        double alpha = Math.atan2 (vy, d);
        double beta = Math.atan2 (vx, vz);
        
        double cosa = Math.cos (alpha);
        double sina = Math.sin (alpha);
        double cosb = Math.cos (beta);
        double sinb = Math.sin (beta);
        
        for (Light l : my_light_arrays.getLights())
            {
          // Rotation transform
          //
            l.xt = l.x*cosb + l.z*sinb;
            l.yt = l.x*sina*sinb + l.y*cosa - l.z*sina*cosb;
            l.zt = -l.x*cosa*sinb + l.y*sina + l.z*cosa*cosb;

          // Apply perspective transform.
          //
            double pt = vd / (vd - l.zt);
            l.xt = pt * l.xt;
            l.yt = pt * l.yt;
            }

      // Prepare an array for drawing that is sorted in z-order.
      //
        zlights = new Light[my_light_arrays.getLights().size()];
        my_light_arrays.getLights().toArray(zlights);
        Arrays.sort (zlights);
        update (getGraphics());
        }
    
  // ----- paint() ------------------------------------------------------
  //
    public void paint (Graphics g)
        {
        g.setColor (Color.DARK_GRAY);
        g.fillRect (0,0, getWidth(),getHeight());
        if (zlights != null)
            {
            int xc = getWidth() / 2;
            int yc = getHeight() / 2;
            for (Light l : zlights)
                {
                int x = (int) (xc + scale*l.xt + 0.5);
                int y = (int) (yc - scale*l.yt + 0.5);  // invert y sense
                
                if (l.on)
                  { g.setColor (l.color);
                    g.fillOval (x-4, y-4, 9,9);
                    }
                  else
                  { g.setColor (Color.LIGHT_GRAY);
                    g.fillRect (x,y, 3,3);
                    }
                }
            }
        }

  // ========== KeyListener support ==================================
  //    
    public void keyPressed (KeyEvent e)
        {
        if (e.getSource () != this)  return;

        int key_code = e.getKeyCode();
        int mods = e.getModifiers();

        double vx0, vz0;
        double alpha, cosa, sina;

        double zoom_in = 1.1, zoom_out = 0.9;
        double dy_factor = 0.10;
        double rotation = 5.0;
        if ((mods & InputEvent.CTRL_MASK) != 0)
            {
            zoom_in = 1.02;
            zoom_out = 0.98;
            dy_factor = 0.02;
            rotation = 1.0;
            }

        switch (key_code)
            {
            case KeyEvent.VK_DOWN:
                if ((mods & InputEvent.SHIFT_MASK) != 0)
                    {
                    scale = zoom_out * scale;
                    }
                  else
                    {
                    vy = vy - dy_factor*y_bnds.length();
                    }
                changeViewpoint();
                break;

            case KeyEvent.VK_UP:
                if ((mods & InputEvent.SHIFT_MASK) != 0)
                    {
                    scale = zoom_in * scale;
                    }
                  else
                    {
                    vy = vy + dy_factor*y_bnds.length();
                    }
                changeViewpoint();
                break;

            case KeyEvent.VK_RIGHT:
                vx0 = vx; vz0 = vz;

                alpha = Math.toRadians (-rotation);
                cosa = Math.cos (alpha);
                sina = Math.sin (alpha);

                vx = vx0*cosa + vz0*sina;
                vz = -vx0*sina + vz0*cosa;

                changeViewpoint();
                break;

            case KeyEvent.VK_LEFT:
                vx0 = vx; vz0 = vz;

                alpha = Math.toRadians (rotation);
                cosa = Math.cos (alpha);
                sina = Math.sin (alpha);

                vx = vx0*cosa + vz0*sina;
                vz = -vx0*sina + vz0*cosa;

                changeViewpoint();
                break;

            default:
                break;
            }
        }
    
    public void keyReleased (KeyEvent e)
        {
        }

    public void keyTyped (KeyEvent e)
        {
        }

  // ========== MouseMotionListener support ===========================
  //
    public void mouseClicked (MouseEvent e)     {}
    public void mouseEntered (MouseEvent e)     {}
    public void mouseExited  (MouseEvent e)     {}
    public void mousePressed (MouseEvent e)
        {
        requestFocus();
        mxm1 = e.getX();
        mym1 = e.getY();
        }
    public void mouseReleased (MouseEvent e)    {}

  // ========== MouseMotionListener support ===========================
  //
    public void mouseDragged (MouseEvent e)
        {
        mx = e.getX();
        my = e.getY();

        double vx0, vz0;

        double vd = Math.sqrt (vx*vx + vy*vy + vz*vz);
        double y_rotation = 4.0*(mx - mxm1) / vd;
        double dy = 0.08 * (my - mym1);
        mxm1 = mx;
        mym1 = my;

        double alpha = Math.toRadians (y_rotation);
        double cosa = Math.cos (alpha);
        double sina = Math.sin (alpha);
        vx0 = vx; vz0 = vz;
        vx = vx0*cosa + vz0*sina;
        vz = -vx0*sina + vz0*cosa;

        vy += dy;

        changeViewpoint();
        }

    public void mouseMoved (MouseEvent e)
        {
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
