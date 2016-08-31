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
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

//======================================================================
// class LightSim
//======================================================================

public class LightSim
    {
    static LightSim     light_sim;
    static public Preferences   prefs;
    
    private LightSimWindow  my_window;
    private LeanExec    my_sim_exec;
    private LightArray      my_light_arrays;
    private boolean enable_gui;

    private ArrayList<LightController> controllers;
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
        
        controllers = new ArrayList<>();
        controllers.add(new SimpleSnakesController());
        controllers.add(new SnakeController());
        controllers.add(new ColorCubeController());
        controllers.add(new DiamondController());
        controllers.add(new GameOfLifeController());
        controllers.add(new HelloWorldController());
        controllers.add(new PulseController());
        controllers.add(new ShootingStarController());
        controllers.add(new SpiralController());
        controllers.add(new StringIDsController());
        controllers.add(new TimesSquareController());
        controllers.add(new StarBurstController());
        controllers.add(new SparklesController());
        
        if (enable_gui) {
            my_sim_exec = new LeanExec(my_light_arrays);
            my_window = new LightSimWindow(controllers, my_sim_exec, my_light_arrays);
        } else {
            LeanExec leanExec = new LeanExec(my_light_arrays);
            leanExec.setController(new ShootingStarController());
            leanExec.start();  // Starts running LeanExec on the executor.

            Server server = new Server(leanExec);
            server.start();
        }
    }
}

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
