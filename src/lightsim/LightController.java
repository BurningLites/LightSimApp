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
