//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 22, 2016
//
//*************************************************************************

//----------------------------------------- String_IDsController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class String_IDsController
//======================================================================

public class String_IDsController extends LightController
    {
    private final static Color BRIGHTER_BLUE = new Color (55,55,255);
    private final static Color LIGHTER_BLUE = new Color (110,110,255);

  // ----- name() ------------------------------------------------------
  //
    @Override
    public String name()    { return "String IDs"; }
    
  // ----- init() ------------------------------------------------------
  //
    public void init (LightArray light_array)
        {
        super.init (light_array);
        Light left_lights[][][] = light_array.getLeftLights();
        Light right_lights[][][] = light_array.getRightLights();
        
      // We assume that the left and right arrays have the same 
      // dimensions.
      //
        int nx = left_lights.length;
        int ny = left_lights[0].length;
        int nym1 = ny - 1;
        int nz = left_lights[0][0].length;
        
      // Set the bottom and top layers of lights to white and blue.
      //
        my_light_array.reset();
        
        my_light_array.fillYPlane (0, left_lights, Color.WHITE);
        my_light_array.fillYPlane (4, left_lights, LIGHTER_BLUE);
        my_light_array.fillYPlane (5, left_lights, LIGHTER_BLUE);
        my_light_array.fillYPlane (nym1, left_lights, BRIGHTER_BLUE);
        my_light_array.fillYPlane (0, right_lights, Color.WHITE);
        my_light_array.fillYPlane (4, right_lights, LIGHTER_BLUE);
        my_light_array.fillYPlane (5, right_lights, LIGHTER_BLUE);
        my_light_array.fillYPlane (nym1, right_lights, BRIGHTER_BLUE);
        
      //
        for (int ix=0; ix<nx; ix++)
            for (int iz=0; iz<nz; iz++)
              { int base_iy = 0;
                for (int mask=0b1; mask<0b10000; mask <<= 1)
                  { base_iy++;
                    if ( (mask & (ix+1)) != 0)
                      { left_lights[ix][base_iy][iz].setColor (Color.RED);
                        left_lights[ix][base_iy][iz].on = true;
                        right_lights[ix][base_iy][iz].setColor (Color.GREEN);
                        right_lights[ix][base_iy][iz].on = true;
                        }
                    if ( (mask & (iz+1)) != 0)
                      { left_lights[ix][base_iy+5][iz].setColor (Color.RED);
                        left_lights[ix][base_iy+5][iz].on = true;
                        right_lights[ix][base_iy+5][iz].setColor (Color.GREEN);
                        right_lights[ix][base_iy+5][iz].on = true;
                        }
                    }
                }
        }
    
  // ----- step() ------------------------------------------------------
  //
    public boolean step (int clock)
        {
        increment_step();
        return true;
        }

    }


//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
