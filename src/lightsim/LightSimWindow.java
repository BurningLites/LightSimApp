//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//-------------------------------------------- LightSimWindow.java -----

package lightsim;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//======================================================================
// class LightSimWindow
//======================================================================

public class LightSimWindow extends JFrame
    {
    private LightSim my_light_sim;

    private LightSimToolbar  my_toolbar;
    private LightViewer my_light_viewer;

  // ----- constructor ------------------------------------------------
  //
    LightSimWindow (LightSim light_sim, LightArray light_arrays)
        {
        super ("Light Simulation");
        my_light_sim = light_sim;

        Container content = getContentPane();
        content.setLayout (new BorderLayout());

      // Set up a default window bounds and provide for storing and retrieving
      // the most recent window size and location in/from the preferences
      // for LightSim.  
      //
        setSize (LightSim.prefs.getInt ("LightSim_window_width", 600),
                 LightSim.prefs.getInt ("LightSim_window_height", 400));
        setLocation (LightSim.prefs.getInt ("LightSim_window_x", 100),
                     LightSim.prefs.getInt ("LightSim_window_y", 100));
        
        addComponentListener (new ComponentAdapter() {
            public void componentMoved (ComponentEvent e)
                {
                LightSim.prefs.putInt ("LightSim_window_x", getX());
                LightSim.prefs.putInt ("LightSim_window_y", getY());
                }
            public void componentResized (ComponentEvent e)
                {
                LightSim.prefs.putInt ("LightSim_window_width", getWidth());
                LightSim.prefs.putInt ("LightSim_window_height", getHeight());
                update(getGraphics());
                }
            });

      // Check that the window is on screen given the current set of
      // screen devices.  If not, reset the bounds to their default
      // values.  This is done _after_ setting up the ComponentListener
      // so that the reset is captured and saved.
      //
        Rectangle window_bounds = getBounds();
        GraphicsEnvironment 
                genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        boolean on_screen = false;
        for (GraphicsDevice gdev : genv.getScreenDevices())
            {
            GraphicsConfiguration gconfig = gdev.getDefaultConfiguration();
            if (gconfig.getBounds().contains(window_bounds))
                on_screen = true;
            }
        if (!on_screen)
            setBounds (100,100, 600,400);

        my_toolbar = new LightSimToolbar (my_light_sim.getLightSimExec());
        content.add (my_toolbar, BorderLayout.NORTH);
        my_light_viewer = new LightViewer (light_arrays);
        content.add (my_light_viewer, BorderLayout.CENTER);     
        
        addWindowListener (new WindowAdapter() {
            public void windowClosing (WindowEvent e)
                { System.exit(0); }
            });

        setVisible (true);
        my_light_viewer.init();
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
