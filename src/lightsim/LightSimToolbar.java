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
import java.util.ArrayList;
import javax.swing.*;
import java.util.logging.*;


//======================================================================
// class DiamondController
//======================================================================

public class LightSimToolbar extends JPanel
                    implements ActionListener, ItemListener
    {
    enum FrameRate
        {
        FR_1    (1, "one frame/sec"),   // 1000 msec
        FR_10   (10, "10 frames/sec"),  // 100 msec
        FR_15   (15, "15 frames/sec"),  // 67 msec
        FR_24   (24, "24 frames/sec"),  // 42 msec
        FR_30   (30, "30 frames/sec"),  // 33 msec
        FR_60   (60, "60 frames/sec");  // 17 msec

        int frame_rate;
        String name;

        FrameRate (int n_frames, String name)
            {
            this.frame_rate = n_frames;
            this.name = name;
            }

        public int getFrameRate()   { return frame_rate; }
        public String toString()    { return name; }
        }

    enum Speed
        {
        SPEED_1_10 (0.10, "1/10"),
        SPEED_1_5  (0.20, "1/5"),
        SPEED_1_2  (0.5, "1/2"),
        SPEED_1    (1.0, "real time"),
        SPEED_2    (2.0, "2x"),
        SPEED_5    (5.0, "5x"),
        SPEED_10   (10.0, "10x"),
        SPEED_20   (20.0, "20x"),
        SPEED_50   (50.0, "50x")
        ;

        double speed_factor;    // Multiply frame rate by this.
        String name;

        Speed (double factor, String name)
            {
            speed_factor = factor;
            this.name = name;
            }

        public double getSpeedFactor()  { return speed_factor; }
        public String toString()        { return name; }
        }

    private static final Logger log =
      Logger.getLogger(LightSimToolbar.class.getPackage().getName());

    private static ImageIcon  PAUSE_ICON, RESET_ICON, RUN_ICON, STEP_ICON;
    static
        {
        PAUSE_ICON =
            ImageLoader.loadIcon ("pause_icon.gif");
        RESET_ICON =
            ImageLoader.loadIcon ("reset_icon.gif");
        RUN_ICON =
            ImageLoader.loadIcon ("run_icon.gif");
        STEP_ICON =
            ImageLoader.loadIcon ("step_icon.gif");
        }

    private JButton     reset_button;
    private JButton     run_button;
    private JButton     step_button;

    private JComboBox<LightController>  controller_cbx;

    private JComboBox<String>  frame_rate_cbx;
    private JComboBox<String>  speed_cbx;
    private JTextField  step_txtfld, time_txtfld;
    private JCheckBox   animation_chkbx;

    ArrayList<LightController> controllers;
    LeanExec    my_exec;
    String state;

  // ----- constructor ------------------------------------------------
  //
    public LightSimToolbar(ArrayList<LightController> controllers, LeanExec exec) {
        super();
        this.controllers = controllers;
        state = "reset";
        my_exec = exec;
        init();
    }

    private void init() {
        setLayout (new FlowLayout(FlowLayout.LEFT));

      // Create a toolbar to hold everything.
      //
        JToolBar run_toolbar = new JToolBar ("Run Toolbar");
        run_toolbar.setFloatable (false);
        run_toolbar.setBorderPainted (true);

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

        run_toolbar.add (reset_button);
        run_toolbar.add (run_button);
        run_toolbar.add (step_button);

      // Create the controller selection drop down menu.  This gets
      // populated with options as controller instances are added to
      // the LightSimExec.
      //
        run_toolbar.addSeparator();

        controller_cbx = new JComboBox();
        controller_cbx.addItemListener (this);
        controller_cbx.setMaximumRowCount(controllers.size());
        for (LightController controller : controllers) {
            controller_cbx.addItem (controller);
        }

        run_toolbar.add (controller_cbx);

        run_toolbar.add (new JLabel(" step:"));
        step_txtfld = new JTextField ("  -  ", 5);
        run_toolbar.add (step_txtfld);

      // Add a separator and the popdown menus for controlling
      // the frame rate, simulation speed and light controller
      // selection.
      //
        run_toolbar.addSeparator();

        run_toolbar.add (new JLabel("Frame rate:"));
        frame_rate_cbx = new JComboBox (FrameRate.values());
        frame_rate_cbx.setSelectedItem (FrameRate.FR_60);
        frame_rate_cbx.addItemListener (this);
        run_toolbar.add (frame_rate_cbx);

        run_toolbar.add (new JLabel("Speed:"));
        speed_cbx = new JComboBox (Speed.values());
        speed_cbx.setSelectedItem (Speed.SPEED_1);
        speed_cbx.addItemListener (this);
        run_toolbar.add (speed_cbx);

      // Add another separator and text fields for reporting
      // time and step.
      //
        run_toolbar.add (new JLabel("time:"));
        time_txtfld = new JTextField ("  -  ", 7);
        run_toolbar.add (time_txtfld);

      // Add a button to control the use of animation.
      //
        run_toolbar.addSeparator();
        animation_chkbx = new JCheckBox ("Animation");
        animation_chkbx.setActionCommand ("animate");
        animation_chkbx.setSelected (true);
        animation_chkbx.addActionListener(this);
        run_toolbar.add (animation_chkbx);

        add (run_toolbar);
    }

  // ----- create_button() --------------------------------------------
  //
    private JButton create_button (ImageIcon icon, String action_command,
                                String tool_tip_text) {
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

  // ----- getFrameRate() ---------------------------------------------
  //
    public int getFrameRate()
        {
        FrameRate selected_rate
                    = (FrameRate) frame_rate_cbx.getSelectedItem();
        return selected_rate.getFrameRate();
        }

    public void setSelectedController(LightController controller) {
        controller_cbx.setSelectedItem(controller);
    }

  // ----- getSelectedController() ------------------------------------
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
        double speed_factor
            = ((Speed) speed_cbx.getSelectedItem()).getSpeedFactor();
        return speed_factor;
        }

  // ----- setStepField() ---------------------------------------------
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
    public void setToolbarState (String state) {
        if (this.state.equals(state)) {
            return;
        }
        this.state = state;
        switch (state)
            {
            case "reset":
                reset_button.setEnabled (false);
                run_button.setIcon (RUN_ICON);
                run_button.setActionCommand ("run");
                run_button.setToolTipText ("run lights");
                run_button.setEnabled (true);
                step_button.setEnabled (true);
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
                reset_button.setEnabled (true);
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
    @Override
    public void actionPerformed (ActionEvent event) {
        String  command = event.getActionCommand();
        setToolbarState (command);

        switch (command)
            {
            case "animate":
                // TODO(kbongort): not sure what to do here.
//                JCheckBox checkBox = (JCheckBox)event.getSource();
//                my_exec.setAnimate(checkBox.isSelected());
                break;

            case "pause":
                my_exec.pause();
                break;

            case "reset":
                my_exec.stop();
                break;

            case "run":
                my_exec.start();
                break;

            case "step":
                // TODO(kbongort): I guess stop, and then increment time by some delta?
//                my_exec.step();
                setToolbarState("pause");
                break;
            }
        }

  // ========== support for ItemListener ==============================
  //
    @Override
    public void itemStateChanged (ItemEvent event)
        {
        Object src = event.getSource();

        if (src == speed_cbx &&
                event.getStateChange() == ItemEvent.SELECTED) {
            my_exec.setSpeed (getSpeed());
        } else if (src == frame_rate_cbx &&
                event.getStateChange() == ItemEvent.SELECTED) {
            // TODO(kbongort): Probably remove framerate control.
            // my_exec.setFrameRate (getFrameRate());
        } else if (src == controller_cbx &&
                event.getStateChange() == ItemEvent.SELECTED) {
            log.info("toolbar event: " + event.getSource());
                my_exec.setController(getSelectedController());
        }
    }

}

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
