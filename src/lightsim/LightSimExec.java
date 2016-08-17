//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//--------------------------------------------- LightSimExec.java -----

package lightsim;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.logging.*;

import lightsim.LightArray.Light;


//======================================================================
// class LightSimExec
//======================================================================
/**
 * This class loads and runs light control plug-ins.  
 */
public class LightSimExec implements ActionListener, Runnable
    {
    LightArray      my_light_array;
    ArrayList<Light>  my_lights;
    LightController my_light_controller;
    LightSim        my_light_sim;
    LightSimToolbar     my_toolbar;
    
    Thread  sim_thread;
    boolean paused, running;
    int step, time, dt;
    private static final Logger log =
      Logger.getLogger(LightSimExec.class.getPackage().getName());

  // ----- constructor ------------------------------------------------
  //
    public LightSimExec (LightSim light_sim, LightArray light_arrays)
        {
        my_light_sim = light_sim;
        my_light_array = light_arrays;
        my_lights = my_light_array.getLights();

        dt = 1000;
        }

  // ----- access methods --------------------------------------------
  //
    public boolean isPaused()
        { return paused; }
    public boolean isRunning()
        { return running; }
    public void setSpeed (double hertz)
        {
        dt = (int) (1000.0/hertz + 0.5);
        }
    public void setToolbar (LightSimToolbar toolbar)
        { my_toolbar = toolbar; }

  // ----- addController() -------------------------------------------
  //
    public void addController (LightController controller)
        {
        my_toolbar.addController (controller);
        }

  // ----- loadPlugIn() ----------------------------------------------
  //
    public void loadPlugin (LightController controller)
        {
        my_light_controller = controller;
        }

  // ----- pause() ---------------------------------------------------
  //
    public void pause()
        {
        running = false;
        }
    
  // ----- runLights() -----------------------------------------------
  //
    public void runLights (boolean start)
        {
        if (sim_thread == null)
            {
            if (!paused)
                {
                my_light_controller = my_toolbar.getSelectedController();
                my_light_controller.init (my_light_array);
                time = 0;
                step = 0;
                }
            sim_thread = new Thread (this);
            }
        if (!running || paused)
            {
            running = true;
            paused = false;
            if (start)
                {
                setSpeed (my_toolbar.getSpeed());
                sim_thread.start();
                }
            }
        }

  // ----- step() -----------------------------------------------
  //
    public void step()
        {
        if (!running)
            runLights (false);

        my_light_controller.step (time, step);
        my_light_controller.setLights (time, step);
        my_light_sim.update();
        
        time_increment();
        paused = true;
        }
    
  // ----- time_increment() -------------------------------------------
  //
    private void time_increment()
        {
        step++;
        time += dt;
        my_toolbar.setStepField (step);
        my_toolbar.setTimeField (time);     
        }
    
  // ========== support for ActionListener ============================
  //
    public void actionPerformed (ActionEvent event)
        {
        String command = event.getActionCommand();

        switch (command)
            {
            case "pause":
                paused = true;
                try { sim_thread.join(); }
                  catch (InterruptedException ie)  {}
                break;

            case "reset":
                reset();
                break;

            case "run":
                runLights (true);
                break;

            case "step":
                step();
                my_toolbar.setToolbarState ("pause");
                break;
            }
        }

    public void reset ()
        {
        running = false;
        paused = false;
        my_light_array.resetLights();
        my_light_sim.update();
        my_toolbar.setToolbarState ("reset");
        my_toolbar.enableControls (true);
        my_toolbar.setStepField (-1);
        my_toolbar.setTimeField (-1);
        sim_thread = null;
        }

    public void restart ()
        {
        reset ();
        runLights (true);
        }
    
  // ========== Runnable support =====================================
  //
    public void run()
        {
        while (running && !paused)
            {
            log.info("step " + step + " time " + time);
            my_light_controller.step (time, step);
            my_light_controller.setLights (time, step);
            my_light_sim.update();
            
            try { Thread.sleep (dt); }
              catch (InterruptedException e)
              { running = false;
                }
              catch (Exception e)
              { running = false;
                }
            time_increment();
            }
        sim_thread = null;
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
