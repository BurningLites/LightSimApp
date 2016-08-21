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
//
// This controller displays a series of characters by marching them
// through light arrays along the x-axis.
// To improve the ability of an observe to discern the individual
// characters, the controller inserts a layer of "off" lights between
// every character and draws each character in a different color.
//

public class HelloWorldController extends LightController
    {
    private static final String HELLO_WORLD = "Hello World ! ";
    private static final Color[]
        RAINBOW = { Color.RED, Color.ORANGE, Color.YELLOW,
                    Color.GREEN, new Color(101,101,255), Color.MAGENTA};

    Light[][][]  lights;
    int nx, nxm1, ny, nz;
    LSFont my_font;

    int ic, color_idx;
    int t_next;
    static final int MY_DT = 500;

  // ----- init() -----------------------------------------------------
  //
  // Organize the lights into arrays that facilitate the manipulation
  // of the light values for this controller.
  //
    public void init (LightArray light_array)
        {
        super.init (light_array);

        if (lights == null)
            {
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
            }

        my_font = new LSFont_5x7();

        ic = 0;
        color_idx = -1;
        t_next = 0;
        }

  // ----- name() -----------------------------------------------------
  //
    public String name()    { return "Hello World"; }

  // ----- step() -----------------------------------------------------
  //
    @Override
    public boolean step (int clock)
        {
        if (clock >= t_next)
            {
            for (int ix=0; ix<nxm1; ix++)
                copy_layer (lights[ix+1], lights[ix]);

            if ( (my_step % 2) == 0)
                {
                char c = HELLO_WORLD.charAt(ic);
                if (c != ' ')
                    color_idx = ++color_idx % RAINBOW.length;
                my_font.fillWithChar (c, lights[nxm1], 0,1,
                                Color.BLACK, RAINBOW[color_idx]);
                ic = ++ic % HELLO_WORLD.length();
                }
              else
                {
                clear_layer (lights[nxm1]);
                }
            increment_step();
            t_next = clock + MY_DT;
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
