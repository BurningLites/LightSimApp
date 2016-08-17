//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- DiamondController.java -----

package lightsim;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//======================================================================
// class DiamondController
//======================================================================

public class LightSimToolbar extends JPanel
                    implements ActionListener, ItemListener
    {
    private static ImageIcon  PAUSE_ICON, RESET_ICON, RUN_ICON, STEP_ICON;
    static
        {
        PAUSE_ICON =
            ImageLoader.loadIcon ("Images/pause_icon.gif");
        RESET_ICON =
            ImageLoader.loadIcon ("Images/reset_icon.gif");
        RUN_ICON =
            ImageLoader.loadIcon ("Images/run_icon.gif");
        STEP_ICON =
            ImageLoader.loadIcon ("Images/step_icon.gif");
        }

    private JButton     reset_button;
    private JButton     run_button;
    private JButton     step_button;
    
    private JTextField  step_txtfld, time_txtfld;

    private JComboBox<String>   speed_cbx;
    private JComboBox<LightController>  controller_cbx;

    LightSimExec    my_exec;
    private static final String 
        SPEED_OPTIONS[] = { "1", "2", "5", "10", "20", "50", "100" };

  // ----- constructor ------------------------------------------------
  //
    public LightSimToolbar (LightSimExec exec)
        {
        super();
        my_exec = exec;
        my_exec.setToolbar (this);
        init();
        }

    private void init()
    {
        setLayout (new FlowLayout(FlowLayout.LEFT));

      // ----------------------------------------------------
      // Create the run control buttons which include
      // a reset button, a combined run/pause button, and a
      // step button.  All of these appear within the same
      // exec_toolbar.
      //
        reset_button = create_button (RESET_ICON, "reset",
                                      "Reset lights");
        reset_button.setEnabled (false);
        run_button = create_button (RUN_ICON, "run",
                                          "Run lights");
        step_button = create_button (STEP_ICON, "step",
                                     "Single step lights");

        JToolBar run_toolbar = new JToolBar ("Run Toolbar");
        run_toolbar.setFloatable (false);
        run_toolbar.setBorderPainted (true);

        run_toolbar.add (reset_button);
        run_toolbar.add (run_button);
        run_toolbar.add (step_button);

      // Add a separator and the popdown menus for controlling
      // speed and selecting a light controller.
      //
        run_toolbar.addSeparator();
        speed_cbx = new JComboBox (SPEED_OPTIONS);
        speed_cbx.addItemListener (this);
        run_toolbar.add (speed_cbx);
        run_toolbar.add (new JLabel("Hz  "));

        controller_cbx = new JComboBox();
        run_toolbar.add (controller_cbx);

      // Add another separator and text fields for reporting
      // time and step.
      //
        run_toolbar.addSeparator();
        run_toolbar.add (new JLabel("time:"));
        time_txtfld = new JTextField ("  -  ", 7);
        run_toolbar.add (time_txtfld);

        run_toolbar.add (new JLabel(" step:"));
        step_txtfld = new JTextField ("  -  ", 5);
        run_toolbar.add (step_txtfld);

        add (run_toolbar);
        }

  // ----- addController() --------------------------------------------
  //
    public void addController (LightController controller)
        {
        controller_cbx.addItem (controller);
        }

  // ----- create_button() --------------------------------------------
  //
    private JButton create_button (ImageIcon icon, String action_command,
                                String tool_tip_text)
        {
        JButton button = new JButton (icon);
        button.setActionCommand (action_command);
        button.addActionListener (this);
        button.setToolTipText (tool_tip_text);

        return button;
        }

  // ----- enableControls() -------------------------------------------
  //
    public final void enableControls (boolean value)
        {
        reset_button.setEnabled (value && my_exec.isRunning());
        boolean have_controllers = controller_cbx.getItemCount() > 0;
        run_button.setEnabled (value && have_controllers);
        step_button.setEnabled (value && have_controllers);
        
        speed_cbx.setEnabled (value);
        controller_cbx.setEnabled (value && have_controllers);
        }

  // ----- getSelectedController() -----------------------------------
  //
    public LightController getSelectedController()
        {
        LightController lc = (
                LightController) controller_cbx.getSelectedItem();
        return lc;
        }

  // ----- getSpeed() ----------------------------------------------
  //
    public double getSpeed()
        {
        String string_hz = (String) speed_cbx.getSelectedItem();
        double hertz = Double.valueOf (string_hz);
        return hertz;
        }

  // ----- setTimeField() ---------------------------------------------
  //
    public void setStepField (int step_count)
        {
        if (step_count < 0)
            {
            step_txtfld.setText ("  -  ");
            }
          else
            {
            step_txtfld.setText (String.format ("%d", step_count));
            }
        }

  // ----- setTimeField() ---------------------------------------------
  //
    public void setTimeField (int millisec)
        {
        if (millisec < 0)
            {
            time_txtfld.setText ("  -  ");
            }
          else
            {
            double t = millisec / 1000.0;
            time_txtfld.setText (String.format ("%.3f", t));
            }
        }

  // ----- setToolbarState() ---------------------------------------
  //
    public void setToolbarState (String state)
        {
        switch (state)
            {
            case "reset":
                run_button.setIcon (RUN_ICON);
                run_button.setActionCommand ("run");
                run_button.setToolTipText ("run lights");
                break;

            case "pause":
                reset_button.setEnabled (true);
                run_button.setIcon (RUN_ICON);
                run_button.setActionCommand ("run");
                run_button.setToolTipText ("run lights");
                run_button.setEnabled (true);
                step_button.setEnabled (true);
                break;

            case "run":
                reset_button.setEnabled (false);
                run_button.setIcon (PAUSE_ICON);
                run_button.setActionCommand ("pause");
                run_button.setToolTipText ("pause lights");
                step_button.setEnabled (false);
                break;

            case "step":
                reset_button.setEnabled (true);
                run_button.setEnabled (false);
                step_button.setEnabled (false);
                break;
            }
        }

  // ========== support for ActionListener ============================
  //
    public void actionPerformed (ActionEvent event)
        {
        String  command = event.getActionCommand();
        setToolbarState (command);
        my_exec.actionPerformed (
                new ActionEvent (event.getSource(),
                                    event.getID(), command));
        }

  // ========== support for ItemListener ==============================
  //
    public void itemStateChanged (ItemEvent event)
        {
        Object src = event.getSource();
        
        if (src == speed_cbx)
            {
            my_exec.setSpeed (getSpeed());
            }
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
