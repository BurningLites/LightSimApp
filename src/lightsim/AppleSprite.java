//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 25, 2016
//
//*************************************************************************

//----------------------------------------- AppleSprite.java -----

package lightsim;

//======================================================================

import java.awt.Color;

// class AppleSprite
//======================================================================

public class AppleSprite  extends Sprite
    {
    private int t_next, cycle;
    private static final int MY_DT = 33;

    private static final Color[]  COLOR_RAMP = new Color[30];
    static
      { int istart = 0, iend = COLOR_RAMP.length / 2;
        for (int i=istart; i<iend; i++)
            COLOR_RAMP[i]
                = new Color (1.0f, 0.8f*i/(iend-istart), 0.8f*i/(iend-istart));
        istart = iend;
        iend = COLOR_RAMP.length;
        for (int i=istart; i<iend; i++)
            COLOR_RAMP[i]
                = new Color (1.0f, 0.8f*(iend-i-1)/(iend-istart),
                                    0.8f*(iend-i-1)/(iend-istart));
        }

    LightArray.Light my_light;

    public AppleSprite (LightArray.Light light)
        {
        my_light = light;
        my_light.setState (Color.RED, true);
        cycle = LSUtils.pickNumber (0, COLOR_RAMP.length-1);
        t_next = 0;
        }

    @Override
    public boolean hasLight (LightArray.Light l)   { return l == my_light; }

    @Override
    public boolean step (int time)
        {
        if (time > t_next)
          { t_next = time + MY_DT;
            cycle = ++cycle % COLOR_RAMP.length;
            alive = my_light.color != Color.BLACK;
            if (alive)
                my_light.color = COLOR_RAMP[cycle];
            }
        return alive;
        }
    }


//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
