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
import java.util.concurrent.*;
import java.util.prefs.Preferences;

//======================================================================
// class LightSim
//======================================================================

public class LightSim implements ActionListener
    {
    static final boolean ENABLE_GUI = true;

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
    public void init() {
        my_light_arrays = new LightArray();
        if (ENABLE_GUI) {
            my_sim_exec = new LightSimExec (this, my_light_arrays);
            my_window = new LightSimWindow (this, my_light_arrays);

            my_sim_exec.addController (new ColorCubeController());
            my_sim_exec.addController (new DiamondController());
            my_sim_exec.addController (new GameOfLifeController());
            my_sim_exec.addController (new HelloWorldController());
            my_sim_exec.addController (new PulseController());
            my_sim_exec.addController (new ShootingStarController());
            my_sim_exec.addController (new SpiralController());
            my_sim_exec.addController (new String_IDsController());
            my_sim_exec.addController (new TimesSquareController());
        } else {
            // Not really *just* GUI disabled; really a totally different mode
            // of execution based on a ScheduleThreadPoolExecutor providing a
            // run loop.
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

            LeanExec leanExec = new LeanExec(exec, my_light_arrays);
            leanExec.setController(new HelloWorldController());
            leanExec.start();  // Starts running LeanExec on the executor.
        }
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
