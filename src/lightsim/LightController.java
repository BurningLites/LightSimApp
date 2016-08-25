//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- LightController.java -----

package lightsim;

import java.awt.Color;
import java.util.ArrayList;
import lightsim.LightArray.Light;

//======================================================================
// abstract class LightController
//======================================================================

/**
 * The light controllers support these methods:
 *
 *      init (LightArray light_array)
 *          Perform any startup tasks.
 *          Access is provided to the light array so that the controller
 *          has the option of storing computed values directly into
 *          the lights, and can get meta information about the structure
 *          of the light array.
 *      String name()
 *          Returns the name of the plug-in.
 *      boolean step (int clock)
 *      Returns true if the plug-in wants to continue running.
 *      setLight (LightArray.Light light)
 *
 * LightController is an abstract class that defines trivial, default
 * implementations for several of these methods.  It also contains
 * useful methods and data for setting light values.
 *
 * Controllers can use the x,y,z information contained in Light instances
 * to
 *     - Determine the current on/off status and color of the Light.
 *     - Arrange lights into arrays that facilitate manipulating their
 *       values for particular display patterns.
 */
public abstract class LightController
    {
    public static final Color[]
        RAINBOW = { Color.RED, new Color(255,127,0), Color.YELLOW,
                    Color.GREEN, new Color(101,101,255), Color.MAGENTA};
    public static class BC_Color extends Color
        {
        public BC_Color (int r, int g, int b)   { super (r,g,b); }
        public BC_Color (int r, int g, int b, int ix, int iy)
            {
            this (r,g,b);
            this.ix = ix;
            this.iy = iy;
            }
        public int ix, iy;
        }
    public static BC_Color[][] BRIGHT_COLOR_HEXAGON;
    public static ArrayList<BC_Color> BRIGHT_COLORS;
    static
        {
        BRIGHT_COLORS = new ArrayList<>();
        BRIGHT_COLOR_HEXAGON = new BC_Color[11][];
        for (int i=0; i<11; i++)
          { int len = 11 - Math.abs (i-5);
            BRIGHT_COLOR_HEXAGON[i] = new BC_Color[len];
            }
        for (int ir=0; ir<=5; ir++)
            for (int ig=0; ig<=5; ig++)
                for (int ib=0; ib<=5; ib++)
                  {
                    if ( ! (ir==5 || ig==5 || ib==5) )  continue;
                    int ix = 5 + ib - ig;
                    int iy = (10 + 2*ir - ig - ib - Math.abs(5-ix)) / 2;
                    BC_Color bcc = new BC_Color (51*ir, 51*ig, 51*ib, ix,iy);
                    BRIGHT_COLORS.add (bcc);
                    BRIGHT_COLOR_HEXAGON[ix][iy] = bcc;
                  }
        }
    
    protected LightSimExec  my_exec;
    protected LightArray    my_light_array;
    protected int           my_step;

  // ----- access methods ---------------------------------------------
  //
    public int getStep()    { return my_step; }
        
    public void setExec (LightSimExec exec)     { my_exec = exec; }
    public void setLights (int time)  {}
        
  // ----- LightController API methods --------------------------------
  //
    public void init (LightArray light_array)
        {
        my_light_array = light_array;
        my_step = 0;
        }
    abstract public String name();
    abstract public boolean step (int time);

  // ----- toString() -------------------------------------------------
  //
    @Override
    public String toString()    { return name(); }
    
  // ----- increment_step() -------------------------------------------
  //
    protected int increment_step()
        {
        my_step++;
        if (my_exec != null)
            my_exec.setStepField (my_step);
        return my_step;
        }    

  // ----- clear_layer() -------------------------------------------
  //
    protected void clear_layer (Light layer[][])
        {
        int nx = layer.length;
        int nz = layer[0].length;

        for (int ix=0; ix<nx; ix++)
            for (int iz=0; iz<nz; iz++)
                {
                Light l = layer[ix][iz];
                l.on = false;
                l.color = Color.LIGHT_GRAY;
                }
        }

  // ----- copy_layer() -------------------------------------------
  //
    protected void copy_layer (Light from[][], Light to[][])
        {
        int nx = from.length;
        int ny = from[0].length;

        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
                {
                Light from_l = from[ix][iy];
                Light to_l = to[ix][iy];
                to_l.on = from_l.on;
                to_l.color = from_l.color;
                }
        }

  // ----- mirror_y_layer() -------------------------------------------
  //
    protected void mirror_y_layer (Light layer[][], byte pattern[],
                                    Color color)
        {
        for (int ix=0; ix<5; ix++)
            {
            int mask = 0b1;
            int row_pattern = pattern[ix];
            for (int iz=0; iz<5; iz++)
                {
                Light l = layer[4-ix][4-iz];
                if ((mask & row_pattern) !=  0)
                    {
                    l.on = true;
                    l.color = color;
                    }
                  else
                    {
                    l.on = false;
                    l.color = Color.LIGHT_GRAY;
                    }
                mask = mask << 1;
                }
            }
        }

  // ----- pickBrightColor() ------------------------------------------
  //
    public BC_Color pickBrightColor()
        {
        int ic = pick_number (0, BRIGHT_COLORS.size()-1);
        return BRIGHT_COLORS.get (ic);
        }
    
  // ----- pickAdjacentColor() ----------------------------------------
  //
    public BC_Color pickAdjacentColor (BC_Color bcc)
        {
        int cix = bcc.ix;
        int ciy = bcc.iy;
        ArrayList<BC_Color> adj_colors = new ArrayList<>();
        for (int ix=-1; ix<=1; ix++)
          { int ix_bch = cix + ix;
            if (ix_bch < 0 || BRIGHT_COLOR_HEXAGON.length <= ix_bch)  
                continue;
            int y0, yn;
            switch (ix)
              {
                case -1:
                    if (ix_bch <= 5)  { y0 = -1;  yn = 0; }
                    else              { y0 = 0;      yn = 1; }
                    break;
                case 0:
                    y0 = -1;
                    yn = 1;
                    break;
                case 1:
                    if (ix_bch < 5)  { y0 = 0;     yn = 1; }
                    else             { y0 = -1; yn = 0; }
                    break;
                default:
                    y0 = 1;     // Don't execute inner loop
                    yn = -1;
                    System.err.println (
                        "Error in pickAdjacentColor: cix,ciy=" + cix + " " + ciy);
                    break;
              }
            for (int iy=y0; iy<=yn; iy++)
              {
                int iy_bch = ciy + iy;
                if (    0 <= iy_bch
                     && iy_bch < BRIGHT_COLOR_HEXAGON[ix_bch].length)
                    adj_colors.add (BRIGHT_COLOR_HEXAGON[ix_bch][iy_bch]);
              }
          }
        int ic = pick_number (0, adj_colors.size()-1);
        return adj_colors.get(ic);
        }
    
  // ----- pick_number() -------------------------------------------
  //
  // Randomly pick a number within the given range (inclusive).  The
  // probably of selecting a particular number is 1 / (uprbnd-lwrbnd+1).
  // Thus,
  //        pick_number (0, 1)
  // returns 0 or 1, each with a probably of 0.5.
  //
    protected int pick_number (int lwrbnd, int uprbnd)
        {
        double pick = (uprbnd - lwrbnd + 1) * Math.random();
        int ipick = (int) pick;
        return lwrbnd + ipick;
        }

  // ----- set_y_layer() -------------------------------------------
  //
    protected void set_y_layer (Light layer[][], byte pattern[],
                                Color color)
        {
        for (int ix=0; ix<5; ix++)
            {
            int mask = 0b10000;
            int row_pattern = pattern[ix];
            for (int iz=0; iz<5; iz++)
                {
                Light l = layer[ix][iz];
                if ((mask & row_pattern) !=  0)
                    {
                    l.on = true;
                    l.color = color;
                    }
                  else
                    {
                    l.on = false;
                    l.color = Color.LIGHT_GRAY;
                    }
                mask = mask >>> 1;
                }
            }
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
