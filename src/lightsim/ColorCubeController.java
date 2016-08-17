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
    public void init (LightArray light_array)
        {
        super.init (light_array);
        int dims[] = light_array.getDimensions();
        float dx = 1.0f / dims[0];
        float dy = 1.0f / dims[1];
        float dz = 1.0f / dims[2];
        for (Light l : light_array.getLights())
            {
            if (l.ix==0 && l.iy==0 && l.iz==0)
                {
                l.setColor (Color.GRAY);
                }
              else
                {
                l.setColor (new Color (l.ix*dx, l.iy*dy, l.iz*dz));
                l.on = true;
                }
            }
        }

    public String name()
        {
        return "Color Cube";
        }

    public boolean step (int time, int step)
        {
        return true;
        }
    public void setLights (int time, int step)
        {}
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
