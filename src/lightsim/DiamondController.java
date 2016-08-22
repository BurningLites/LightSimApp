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

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class DiamondController
//======================================================================

public class DiamondController extends LightController
    {
    //   +-----> z
    //   |
    //   |
    //   V x
    //
    private static final byte[][] DIAMOND =
        {
          { 0b00000000,
            0b00000000,
            0b00000100,
            0b00000000,
            0b00000000 },
          { 0b00000000,
            0b00001110,
            0b00001010,
            0b00001110,
            0b00000000 },
          { 0b00011111,
            0b00010001,
            0b00010001,
            0b00010001,
            0b00011111 },
          { 0b00000000,
            0b00001110,
            0b00001010,
            0b00001110,
            0b00000000 },
          { 0b00000000,
            0b00000000,
            0b00000100,
            0b00000000,
            0b00000000 },
        };
    
    Light[][][] left_lights, right_lights;
    int iy, iyp1, nx, ny, nz;

    int t_next;
    static final int MY_DT = 33;

  // ----- init() -----------------------------------------------------
  //
    public void init (LightArray light_array)
        {
        super.init (light_array);

        nx = 5; ny = 10; nz = 5;
        left_lights = new Light[ny][nx][nz];
        right_lights = new Light[ny][nx][nz];
        for (Light l : my_light_array.getLights())
            {
            if (l.ix < 5)
                left_lights[l.iy][l.ix][l.iz] = l;
              else
                right_lights[l.iy][l.ix-12][l.iz] = l;
            }

        my_light_array.reset();

        Color color = Color.GREEN;
        set_y_layer (left_lights[2], DIAMOND[2], color);
        set_y_layer (left_lights[1], DIAMOND[1], color);
        set_y_layer (left_lights[3], DIAMOND[3], color);
        set_y_layer (left_lights[0], DIAMOND[0], color);
        set_y_layer (left_lights[4], DIAMOND[4], color);

        color = Color.MAGENTA;
        set_y_layer (right_lights[2], DIAMOND[2], color);
        set_y_layer (right_lights[1], DIAMOND[1], color);
        set_y_layer (right_lights[3], DIAMOND[3], color);
        set_y_layer (right_lights[0], DIAMOND[0], color);
        set_y_layer (right_lights[4], DIAMOND[4], color);

        iy = 0;
        iyp1 = 1;
        t_next = 0;
        }

  // ----- name() -----------------------------------------------------
  //
    public String name()    { return "Diamonds"; }

  // ----- step() -----------------------------------------------------
  //
    public boolean step (int clock)
        {
        if (clock >= t_next)
            {
            int next_y = (my_step + 5) % ny;
            for (int i=0; i<5; i++)
                {
                int ym1 = next_y - 1;
                if (ym1 < 0) ym1 += ny;
                copy_layer (left_lights[ym1], left_lights[next_y]);
                copy_layer (right_lights[ym1], right_lights[next_y]);
                --next_y;
                if (next_y < 0)  next_y += ny;
                }
            clear_layer (left_lights[next_y]);
            clear_layer (right_lights[next_y]);

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

