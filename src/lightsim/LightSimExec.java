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
import javax.swing.JCheckBox;
import java.util.ArrayList;

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
    LightSimToolbar my_toolbar;
    
    Thread  sim_thread;
    boolean paused, running, stepping;
    int step0;
    int time, dt, t_wait;
    double clock, dclock;
    int frame_rate;
    double speed_factor;
    boolean animate;

  // ----- constructor ------------------------------------------------
  //
    public LightSimExec (LightSim light_sim, LightArray light_arrays)
        {
        my_light_sim = light_sim;
        my_light_array = light_arrays;
        my_lights = my_light_array.getLights();

        animate = true;
        dt = 1000;
        }

  // ----- access methods --------------------------------------------
  //
    public boolean isPaused()
        { return paused; }
    public boolean isRunning()
        { return running; }
    public void setFrameRate (int _frame_rate)
        {
        frame_rate = _frame_rate;
        dclock = 1.0 / frame_rate;
        dt = (int) (1000.0/frame_rate + 0.5);
        speed_factor = my_toolbar.getSpeed();
        t_wait = (int) (1000.0/(frame_rate*speed_factor) + 0.5);
        }
    public void setSpeed (double speed_factor)
        {
        frame_rate = my_toolbar.getFrameRate();
        t_wait = (int) (1000.0/(frame_rate*speed_factor) + 0.5);
        }
    public void setStepField (int step)
        {
        my_toolbar.setStepField (step);
        if (stepping && step != step0)
          { paused = true;
            stepping = false;
            }
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

  // ----- report_load() ---------------------------------------------
  //
    private void report_load (double load)
        {
        System.out.println (String.format("load:%7.1f",100*load));
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
                my_light_controller.setExec (this);
                my_light_controller.init (my_light_array);
                time = 0;
                clock = 0.0;
                }
            sim_thread = new Thread (this);
            }
        if (!running || paused)
            {
            running = true;
            paused = false;
            if (start)
                {
                setFrameRate (my_toolbar.getFrameRate());
                sim_thread.start();
                }
            }
        }

  // ----- step() -----------------------------------------------
  //
    public void step()
        {
      // Make certain that the light controller has been set up
      // properly.
      //
        runLights (false);

      // Now set up this execution to run until the light controller
      // increments its step.
      //
        stepping = true;
        step0 = my_light_controller.getStep();
        
      // Kick off the run() loop.
      //
        running = true;
        paused = false;
        setFrameRate (my_toolbar.getFrameRate());
        t_wait = 0;
        try { sim_thread.join(); }
          catch (InterruptedException ie)  {}
        if (sim_thread == null)
            sim_thread = new Thread (this);
        sim_thread.start();
        }
    
  // ----- time_increment() -------------------------------------------
  //
    private void time_increment()
        {
        clock += dclock;
        time = (int) (1000.0*clock + 0.5);
        my_toolbar.setTimeField (time);     
        }
    
  // ========== support for ActionListener ============================
  //
    public void actionPerformed (ActionEvent event)
        {
        String command = event.getActionCommand();

        switch (command)
            {
            case "animate":
                JCheckBox chkbx = (JCheckBox) event.getSource();
                animate = chkbx.isSelected();
                break;

            case "pause":
                paused = true;
                try { sim_thread.join(); }
                  catch (InterruptedException ie)  {}
                break;

            case "reset":
                running = false;
                paused = false;
                my_light_array.reset();
                my_light_sim.update();
                my_toolbar.setToolbarState ("reset");
                my_toolbar.enableControls (true);
                my_toolbar.setStepField (-1);
                my_toolbar.setTimeField (-1);
                sim_thread = null;
                my_light_array.releaseTemps();
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
    
  // ========== Runnable support =====================================
  //
    public void run()
        {
        while (running && !paused)
            {
            long t_begin = System.currentTimeMillis();

            my_light_controller.step (time);
            my_light_controller.setLights (time);
            if (animate)
                my_light_sim.update();

            long t_end = System.currentTimeMillis();
            double load = (t_end - t_begin) / dt;
//            if ((step % frame_rate) == 0)
//                report_load (load);
            
            try { Thread.sleep (t_wait); }
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
