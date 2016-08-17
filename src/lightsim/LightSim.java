//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//------------------------------------------------- LightSim.java -----

package lightsim;

import java.awt.event.*;
import java.util.prefs.Preferences;

//======================================================================
// class LightSim
//======================================================================

public class LightSim implements ActionListener
    {
    static LightSim     light_sim;
    static public Preferences   prefs;
    
    private LightSimWindow  my_window;
    private LightSimExec    my_sim_exec;
    private LightArray      my_light_arrays;

  // ----- main() ----------------------------------------------------
  //
  /**
   *   @param args the command line arguments
   */
    public static void main (String[] args)
        {
        prefs = Preferences.userRoot();
        light_sim = new LightSim();
        light_sim.init();
        }

  // ----- constructor -----------------------------------------------
  //
    public LightSim()
        {
        }

  // ----- init() -----------------------------------------------------
  //
    public void init()
        {
        my_light_arrays = new LightArray();
        my_sim_exec = new LightSimExec (this, my_light_arrays);
        my_window = new LightSimWindow (this, my_light_arrays);

        my_sim_exec.addController (new HelloWorldController());
        my_sim_exec.addController (new GameOfLifeController());
        my_sim_exec.addController (new DiamondController());
        my_sim_exec.addController (new SpiralController());
        my_sim_exec.addController (new PulseController());
        my_sim_exec.addController (new ColorCubeController());
        }

  // ----- access/convenience methods ---------------------------------
  //
    public LightSimExec getLightSimExec()   { return my_sim_exec; }
    public void update()    { my_window.repaint(); }

  // ========== ActionListener support ================================
  //
    public void actionPerformed (ActionEvent e)
        {

        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
