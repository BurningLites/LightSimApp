//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- ColorCubeController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class ColorCubeController
//======================================================================

public class ColorCubeController extends LightController
    {
    private Light[][][] all_lights;
    private int nx, ny, nz;

    private int next_t = 0;
    private static final int MY_DT = 100;

  // ----- init() -----------------------------------------------------
  //
    public void init (LightArray light_array)
        {
        super.init (light_array);

        all_lights = my_light_array.getAllLights();
        nx = all_lights.length;
        ny = all_lights[0].length;
        nz = all_lights[0][0].length;
        float dc_x = 1.0f / (nx-1);
        float dc_y = 1.0f / (ny-1);
        float dc_z = 1.0f / (nz-1);

        for (int ix=0; ix<nx; ix++)
          { float cx = calc_color_component (ix, dc_x);
            for (int iy=0; iy<ny; iy++)
              { float cy = calc_color_component (iy, dc_y);
                for (int iz=0; iz<nz; iz++)
                  { float cz = calc_color_component (iz, dc_z);
                    Light l = all_lights[ix][iy][iz];
                    l.setColor (new Color (cx, cy, cz));
                    }
                }
            }
        all_lights[0][0][0].setColor(Color.BLACK);

        next_t = 0;
        }

  // ----- calc_color_component() -------------------------------------
  //
    private float calc_color_component (int i, float dcc)
        {
        float cc = i * dcc;
        if (cc > 1.0)  cc = 1.0f;
        return cc;
        }

  // ----- name() -----------------------------------------------------
  //
    public String name()
        {

        return "Color Cube";
        }

  // ----- step() -----------------------------------------------------
  //
    public boolean step (int time)
        {
        if (next_t == 0)
            next_t = time + MY_DT;

        while (next_t <= time)
            {
            next_t += MY_DT;

            if (my_step == 0)
                ;                       // Do nothing
            else if (my_step <= nx+ny+nz)
              { if (0 < my_step && my_step <= nx)
                    my_light_array.shiftAlongXAxis (1, all_lights);
                  else if (nx < my_step && my_step <= nx+ny)
                    my_light_array.shiftAlongYAxis (1, all_lights);
                  else
                    my_light_array.shiftAlongZAxis (1, all_lights);
                }
            else
              { switch (my_step % 3)
                  { case 0:
                        my_light_array.shiftAlongXAxis (1, all_lights);
                        break;
                    case 1:
                        my_light_array.shiftAlongYAxis (1, all_lights);
                        break;
                    case 2:
                        my_light_array.shiftAlongZAxis (1, all_lights);
                        break;
                    }
                if (!all_lights[0][0][0].getColor().equals(Color.BLACK))
                    my_step = 0;
                }

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
