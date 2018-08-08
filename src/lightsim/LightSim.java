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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
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
    private LeanExec    exec;
    private LightArray      my_light_arrays;
    private boolean enable_gui;
    private boolean scheduled;

    private ArrayList<LightController> controllers;
  // ----- main() ----------------------------------------------------
  //
  /**
   *   @param args the command line arguments
   */
    public static void main (String[] args)
        {
        List<String> argList = Arrays.asList(args);
        Console.log("args are %s", argList.toString());
        boolean enable_gui = !argList.contains("--no-gui");
        boolean scheduled = argList.contains("--scheduled");
        prefs = Preferences.userRoot();
        light_sim = new LightSim(enable_gui, scheduled);
        light_sim.init();
        }

  // ----- constructor -----------------------------------------------
  //
    public LightSim(boolean enable_gui, boolean scheduled)
        {
        this.enable_gui = enable_gui;
        this.scheduled = scheduled;
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
        controllers.add(new WaveController());
        
        exec = new LeanExec(my_light_arrays);
        if (enable_gui) {
            my_window = new LightSimWindow(controllers, exec, my_light_arrays);
        } else {
            exec = new LeanExec(my_light_arrays);
            exec.setController(new WaveController());
        }
        Server server = new Server(exec);
        server.start();
        
        
        if (scheduled) {
            Console.log("running in scheduled mode");
            exec.setIsScheduled(true);
        } else {
            Console.log("starting immediately");
            exec.start();  // Starts running LeanExec on the executor.
        }
    }
}

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
