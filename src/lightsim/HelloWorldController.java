//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- HelloWorldController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class HelloWorldController
//======================================================================

public class HelloWorldController extends LightController
    {
    private static final String HELLO_WORLD = "Hello World ! ";
    private static final Color[]
        RAINBOW = { Color.RED, Color.ORANGE, Color.YELLOW,
                    Color.GREEN, new Color(55,55,255), Color.MAGENTA};

    Light[][][]  lights;
    int nx, nxm1, ny, nz;
    int t_next = -1;
    int ic, color_idx = -1, my_step;

    public String name()    { return "Hello World"; }

  // ----- init() -----------------------------------------------------
  //
  // Organize light into arrays that will facilitate the manipulation
  // of the light values.
  //
    public void init (LightArray light_array)
        {
        super.init (light_array);

        nx = 10; ny = 10; nz = 5;
        nxm1 = nx - 1;
        lights = new Light[10][5][10];
        for (Light l : my_light_array.getLights())
            {
            if (l.ix < 5)
                {
                lights[l.ix][l.iz][l.iy] = l;
                }
              else
                {
                lights[5+(l.ix-12)][l.iz][l.iy] = l;
                }
            }

        ic = 0;
        color_idx = -1;
        t_next = 0;
        my_step = 0;
        }

  // ----- step() -----------------------------------------------------
  //
    public boolean step (int clock, int step)
        {
        if (t_next < 0)
            t_next = clock + 1000;

        if (clock >= t_next)
            {
            for (int ix=0; ix<nxm1; ix++)
                copy_layer (lights[ix+1], lights[ix]);

            if ( (my_step++ % 2) == 0)
                {
                char c = HELLO_WORLD.charAt(ic);
                if (c != ' ')
                    color_idx = ++color_idx % RAINBOW.length;
                set_5x7_char (c, lights[nxm1], 0,1,
                                Color.BLACK, RAINBOW[color_idx]);
                ic = ++ic % HELLO_WORLD.length();
                }
              else
                {
                clear_layer (lights[nxm1]);
                }
            t_next = clock + 1000;
            }
        return true;
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
