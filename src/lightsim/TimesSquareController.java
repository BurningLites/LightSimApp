//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- TimesSquareController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================

// class TimesSquareController
//======================================================================

public class TimesSquareController extends LightController
    {
    private static final String HELLO_WORLD = "WELCOME HOME! ";

    private Light[][] lights;
    private LSFont my_font;

    private int nx, ny, nxm1;
    private int t_next;
    private int  dt = 250;
    private int  ic, color_idx;
    private Color current_color;
    private char  current_char;
    private int   n_char_columns, column_idx;

  // ----- constructor ------------------------------------------------
  //
    public TimesSquareController()
        {
//        my_font = new LSFont_5x7();
//        my_font = new LSFont_6p();
        my_font = new LSFont_6pn();
        }

  // ----- name() -----------------------------------------------------
  //
    public String name()    { return "Times Square"; }

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
            nx = 26; ny = 10;
            nxm1 = nx - 1;

            lights = new Light[nx][ny];
            for (Light l : my_light_array.getLights())
                {
                if (l.ix == 0)
                  { lights[l.iz+12][l.iy] = l;
                    }
                  else if (l.ix == 16 && l.iz<4)
                  { lights[3-l.iz][l.iy] = l;
                    }
                  else if (l.ix == 16 && l.iz == 4)
                  { lights[25][l.iy] = l;
                    }
                  else if (l.iz == 0 && l.ix >= 12)
                  { lights[19-l.ix][l.iy] = l;
                    }
                  else if (l.iz == 0)
                  { lights[12-l.ix][l.iy] = l;
                    }
                  else if (l.iz == 4 && l.ix <= 4)
                  { lights[16+l.ix][l.iy] = l;
                    }
                  else if (l.iz == 4)
                  { lights[9+l.ix][l.iy] = l;
                    }
                }
            }

        my_light_array.reset();

        ic = 0;
        color_idx = -1;
        t_next = -1;

        n_char_columns = column_idx = 0;
        }

  // ----- step() -----------------------------------------------------
  //
    public boolean step (int clock)
        {
        if (t_next < 0)
            t_next = clock + dt;

        if (clock >= t_next)
            {
          // Move the lights to the left by one column.
          //
            for (int ix=0; ix<nxm1; ix++)
                for (int iy=0; iy<ny; iy++)
                  { Light l_to = lights[ix][iy];
                    Light l_from = lights[ix+1][iy];
                    l_to.color = l_from.color;
                    }

          // Set the rightmost column.  If we're starting a new
          // character, then we get set to render it and reset the
          // lights in the rightmost column.  This introduces a
          // an inter-character space.
          //
            if (column_idx >= n_char_columns)
                {
              // Setup for the next character.
              //
                current_char = HELLO_WORLD.charAt(ic);
                ic = (ic+1) % HELLO_WORLD.length();
                column_idx = 0;
                n_char_columns = my_font.getCharWidth (current_char);
                color_idx = (color_idx + 1) % RAINBOW.length;
                current_color = RAINBOW[color_idx];

              // Draw a single column of space between characters.
              //
                for (int iy=0; iy<ny; iy++)
                  { lights[nxm1][iy].color = Color.BLACK;
                    }
                }
              else
              { my_font.fillWithCharColumn (current_char, column_idx,
                                    lights[nxm1], 2,
                                    Color.BLACK, current_color);
                column_idx++;
                }
            t_next = clock + dt;
            increment_step();
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
