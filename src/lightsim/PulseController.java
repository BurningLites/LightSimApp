//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- PulseController.java -----

package lightsim;

import java.awt.Color;

//======================================================================
// class PulseController
//======================================================================

public class PulseController extends LightController
    {
    private static final int WAVE_LENGTH = 9;
    private static final double PI = 3.141592;
    private static final double DW = 2.0*PI / (WAVE_LENGTH+4);
    private boolean on_wave[];
    private Color blue_wave[];
    private static final Color OFF_COLOR = new Color (155,155,155);
    private int i0;
    
    public PulseController()
        {
        on_wave = new boolean[WAVE_LENGTH+4];
        blue_wave = new Color[WAVE_LENGTH+4];
        }
    
    public void init (LightArray light_array)
        {
        super.init (light_array);
        i0 = -1;
        step (0,0);
        }
    
    public String name()
        {
        return "Pulsed Wave";
        }

  // The total peak-to-peak length of the wave is longer than the
  // height of the light array, and the curve is more of a pulse
  // than a sinusoid.
  //
    public boolean step (int time, int step)
        {
        i0 = ++i0 % (WAVE_LENGTH+4);
// System.out.println ("i0=" + i0);
        for (int i=0; i<=WAVE_LENGTH; i++)
            {
            double cos_idw = (Math.cos ((i+i0)*DW) + 1.0) / 2.0;
            double wave = Math.pow (cos_idw, 4.0);
// System.out.println (String.format("%f %f %f", DW*(i+i0), cos_idw, wave));
            if (wave > 0.5)
                {
                on_wave[i] = true;
                blue_wave[i] = new Color (0, 0, (int) (254.0*wave));
                }
              else
                {
                on_wave[i] = false;
                blue_wave[i] = OFF_COLOR;
                }
            }
        return true;
        }
    
    public void setLights (int time, int step)
        {
        for (LightArray.Light light : my_light_array.getLights())
            {
            light.on = on_wave[light.iy];
            light.color = blue_wave[light.iy];
            }
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
