//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- SpiralController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class SpiralController
//======================================================================

public class SpiralController extends LightController
    {
    //   +-----> z
    //   |
    //   |
    //   V x
    //
    private static final byte[][] LOOP =
        {
          { 0b00001111,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00000000 },
          { 0b00000111,
            0b00000001,
            0b00000000,
            0b00000000,
            0b00000000 },
          { 0b00000011,
            0b00000001,
            0b00000001,
            0b00000000,
            0b00000000 },

          { 0b00000001,
            0b00000001,
            0b00000001,
            0b00000001,
            0b00000000 },
          { 0b00000000,
            0b00000001,
            0b00000001,
            0b00000001,
            0b00000001 },
          { 0b00000000,
            0b00000000,
            0b00000001,
            0b00000001,
            0b00000011 },
          { 0b00000000,
            0b00000000,
            0b00000000,
            0b00000001,
            0b00000111 },

          { 0b00000000,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00001111 },
          { 0b00000000,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00011110 },
          { 0b00000000,
            0b00000000,
            0b00000000,
            0b00010000,
            0b00011100 },
          { 0b00000000,
            0b00000000,
            0b00010000,
            0b00010000,
            0b00011000 },

          { 0b00000000,
            0b00010000,
            0b00010000,
            0b00010000,
            0b00010000 },
        };

    private static final byte TRANSITION_LAYER1[][] =
        {
          { 0b00000000,
            0b00010000,
            0b00010000,
            0b00010000,
            0b00000000 },
          { 0b00000000,
            0b00010000,
            0b00010000,
            0b00000000,
            0b00000000 },
          { 0b00000000,
            0b00010000,
            0b00000000,
            0b00000000,
            0b00000000 },
          { 0b00000000,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00000000 },
        };

    private static final byte TRANSITION_LAYER2[][] =
        {
          { 0b00010000,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00000000 },
          { 0b00011000,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00000000 },
          { 0b00011100,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00000000 },
          { 0b00011110,
            0b00000000,
            0b00000000,
            0b00000000,
            0b00000000 },
        };
    
    Light[][][] left_lights, right_lights;
    int iy, iyp1, nx, ny, nz;

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
        my_light_array.reset ();

        iy = 0;
        iyp1 = 1;
        }

  // ----- name() -----------------------------------------------------
  //
    public String name()    { return "Spiral"; }
    
  // ----- step() -----------------------------------------------------
  //
    public boolean step (int clock)
        {
        int i = my_step % 16;
        if (0 <= i && i < 12)
            {
            set_y_layer (left_lights[iy], LOOP[i]);
            mirror_y_layer (right_lights[iy], LOOP[i]);
            }
          else if (12 <= i && i < 15)
            {
            set_y_layer (left_lights[iy], TRANSITION_LAYER1[i-12]);
            set_y_layer (left_lights[iyp1], TRANSITION_LAYER2[i-12]);
            mirror_y_layer (right_lights[iy], TRANSITION_LAYER1[i-12]);
            mirror_y_layer (right_lights[iyp1], TRANSITION_LAYER2[i-12]);
            }
          else
            {
            set_y_layer (left_lights[iy], TRANSITION_LAYER1[3]);
            set_y_layer (left_lights[iyp1], TRANSITION_LAYER2[3]);
            mirror_y_layer (right_lights[iy], TRANSITION_LAYER1[3]);
            mirror_y_layer (right_lights[iyp1], TRANSITION_LAYER2[3]);
            iy = (iy + 1) % ny;
            iyp1 = (iyp1 + 1) % ny;
            }

        increment_step();
        return true;
        }
    
  // ----- mirror_y_layer() ---------------------------------------------
  //
    private void mirror_y_layer (Light layer[][], byte pattern[])
        {
        for (int ix=0; ix<5; ix++)
            {
            int mask = 0b1;
            int row_pattern = pattern[ix];
            for (int iz=0; iz<5; iz++)
                {
                Light l = layer[4-ix][4-iz];
                if ((mask & row_pattern) !=  0)
                    {
                    l.on = true;
                    l.color = Color.GREEN;
                    }
                  else
                    {
                    l.on = false;
                    l.color = Color.LIGHT_GRAY;
                    }
                mask = mask << 1;
                }
            }
        }

  // ----- set_y_layer() ---------------------------------------------
  //
    private void set_y_layer (Light layer[][], byte pattern[])
        {
        for (int ix=0; ix<5; ix++)
            {
            int mask = 0b10000;
            int row_pattern = pattern[ix];
            for (int iz=0; iz<5; iz++)
                {
                Light l = layer[ix][iz];
                if ((mask & row_pattern) !=  0)
                    {
                    l.on = true;
                    l.color = Color.CYAN;
                    }
                  else
                    {
                    l.on = false;
                    l.color = Color.LIGHT_GRAY;
                    }
                mask = mask >>> 1;
                }
            }
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
