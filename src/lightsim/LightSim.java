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
    static LightSim     light_sim;
    static public Preferences   prefs;
    
    private LightSimWindow  my_window;
    private LightSimExec    my_sim_exec;
    private LightArray      my_light_arrays;
    private boolean enable_gui;

  // ----- main() ----------------------------------------------------
  //
  /**
   *   @param args the command line arguments
   */
    public static void main (String[] args)
        {
        boolean enable_gui = true;
        if (args.length > 0 && "--no-gui".equals(args[0])) {
            enable_gui = false;
        }
        prefs = Preferences.userRoot();
        light_sim = new LightSim(enable_gui);
        light_sim.init();
        }

  // ----- constructor -----------------------------------------------
  //
    public LightSim(boolean enable_gui)
        {
        this.enable_gui = enable_gui;
        }

  // ----- init() -----------------------------------------------------
  //
    public void init() {
        my_light_arrays = new LightArray();
        if (enable_gui) {
            my_sim_exec = new LightSimExec (this, my_light_arrays);
            my_window = new LightSimWindow (this, my_light_arrays);

            my_sim_exec.addController (new SnakeController());
            my_sim_exec.addController (new ColorCubeController());
            my_sim_exec.addController (new DiamondController());
            my_sim_exec.addController (new GameOfLifeController());
            my_sim_exec.addController (new HelloWorldController());
            my_sim_exec.addController (new PulseController());
            my_sim_exec.addController (new ShootingStarController());
            my_sim_exec.addController (new SpiralController());
            my_sim_exec.addController (new StringIDsController());
            my_sim_exec.addController (new TimesSquareController());
            my_sim_exec.addController (new StarBurstController());
        } else {
            // Not really *just* GUI disabled; really a totally different mode
            // of execution based on a ScheduleThreadPoolExecutor providing a
            // run loop.
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

            LeanExec leanExec = new LeanExec(exec, my_light_arrays);
            leanExec.setController(new ShootingStarController());
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
