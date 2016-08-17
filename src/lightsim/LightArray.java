//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------------- LightArray.java -----

package lightsim;

import java.awt.Color;
import java.util.ArrayList;

//======================================================================
// class LightArray
//======================================================================

public class LightArray
    {
    public class Light implements Comparable
        {
        public int    ix, iy, iz;
        public double x, y, z, xt, yt, zt;
        public boolean on;
        public Color color;

        public Light (double _x, double _y, double _z, Color _color)
            {
            x = _x;
            y = _y;
            z = _z;
            color = _color;
            }
        public void setColor (Color value)  { color = value; }
        public void setIndices (int _ix, int _iy, int _iz)
            {
            ix = _ix;
            iy = _iy;
            iz = _iz;
            }
        public int compareTo (Object other)
            {
            Light l = (Light) other;
            if (zt < l.zt)
                return -1;
              else if (zt == l.zt)
                return 0;
              else
                return 1;
            }
        }

    private ArrayList<Light> my_lights;
    private static final Color whites[] =
        {
            new Color(155,155,155), new Color (180,180,180),
            new Color (205,205,205), new Color(230,230,230),
            Color.WHITE
        };
    private static final int DIMENSIONS[] = { 16, 9, 4 };

  // ----- constructor ------------------------------------------------
  //
    public LightArray()
        {
        // Axes:
        //          ^ y
        //          |
        //          |
        //          *----->x
        //         /
        //        /
        //       V z
        //
        // Define two light arrays separated by 8 units of space.
        // Each array is 5x5 at the bottom and 10 lights high.
        //
        my_lights = new ArrayList<>();
        for (int ix=0; ix<5; ix++)
            {
            for (int iy=0; iy<10; iy++)
                {
                for (int iz=0; iz<5; iz++)
                    {
                    Light l = new Light (ix,iy,iz, whites[iz]);
                    l.setIndices (ix,iy,iz);
                    my_lights.add (l);

                    l = new Light (ix+12,iy,iz, whites[iz]);
                    l.setIndices (ix+12,iy,iz);
                    my_lights.add (l);
                    }
                }
            }
        }

    public int[] getDimensions()        { return DIMENSIONS; }
    public ArrayList<Light> getLights() { return my_lights; }

    public void resetLights()
        {
        for (Light l : my_lights)
            {
            l.color = Color.LIGHT_GRAY;
            l.on = false;
            }
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
