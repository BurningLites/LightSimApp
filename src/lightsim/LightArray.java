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
    enum Axis
        {
        X_AXIS,
        Y_AXIS,
        Z_AXIS
        }

    public class Light implements Comparable
        {
        public int    ix, iy, iz;
        public double x, y, z, xt, yt, zt;
        public boolean on;
        public Color color;

        public Light()
            {
            color = color.BLACK;
            }
        public Light (double _x, double _y, double _z, Color _color)
            {
            x = _x;
            y = _y;
            z = _z;
            color = _color;
            on = false;
            }
        public void setState (Light light)
            {
            this.on = light.on;
            this.color = light.color;
            }
        public void setState (Color _color, boolean _on)
            {
            color = _color;
            on = _on;
            }

        public boolean isOn() {
            return true;
        }
        
        public void setColor (Color value)  {
            color = value;
        }
        
        public Color getColor () {
            return color;
        }
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
    private ArrayList<Light> temp_lights;
    private ArrayList  temp_objects;

    private int temp_idx;
    private Light[][][] all_lights, left_lights, right_lights;

    private Light[][] strings;
    private static final Color whites[] =
        {
            new Color(155,155,155), new Color (180,180,180),
            new Color (205,205,205), new Color(230,230,230),
            Color.WHITE
        };
    private static final int DIMENSIONS[] = { 16, 9, 4 };
    private static final int ALL_DIMENSIONS[] = { 10, 10, 5 };
    private static final int LEFT_DIMENSIONS[] = { 5, 10, 5 };
    private static final int RIGHT_DIMENSIONS[] = { 5, 10, 5 };


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
        temp_lights = new ArrayList<>();
        temp_objects = new ArrayList();

        all_lights = new Light[ALL_DIMENSIONS[0]]
                              [ALL_DIMENSIONS[1]]
                              [ALL_DIMENSIONS[2]];
        left_lights = new Light[LEFT_DIMENSIONS[0]]
                               [LEFT_DIMENSIONS[1]]
                               [LEFT_DIMENSIONS[2]];
        right_lights = new Light[RIGHT_DIMENSIONS[0]]
                                [RIGHT_DIMENSIONS[1]]
                                [RIGHT_DIMENSIONS[2]];
        strings = new Light[50][10];
        
        for (int ix=0; ix<5; ix++)
            {
            for (int iy=0; iy<10; iy++)
                {
                for (int iz=0; iz<5; iz++)
                    {
                    Light l = new Light (ix,iy,iz, whites[iz]);
                    l.setIndices (ix,iy,iz);
                    my_lights.add (l);
                    all_lights[ix][iy][iz] = l;
                    left_lights[ix][iy][iz] = l;
                    strings[ix + 5 * iz][9 - iy] = l;

                    l = new Light (ix+12,iy,iz, whites[iz]);
                    l.setIndices (ix+12,iy,iz);
                    my_lights.add (l);
                    all_lights[ix+5][iy][iz] = l;
                    right_lights[ix][iy][iz] = l;
                    strings[25 + ix + 5 * iz][9 - iy] = l;
                    }
                }
            }
        }

  // ----- access methods ---------------------------------------
  //
    public int[] getDimensions()        { return DIMENSIONS; }
    public int[] getAllDimensions()     { return ALL_DIMENSIONS; }
    public int[] getLeftDimensions()    { return LEFT_DIMENSIONS; }
    public int[] getRightDimensions()   { return RIGHT_DIMENSIONS; }

    public ArrayList<Light> getLights() { return my_lights; }
    
    public Light[][][] getAllLights()   { return all_lights; }
    public Light[][][] getLeftLights()  { return left_lights; }
    public Light[][][] getRightLights() { return right_lights; }
    public Light[][] getStrings() { return strings; }

  // ----- fill() ----------------------------------------------------
  //
    public void fill (Color color)
        {
        fill (color, true);
        }
    public void fill (Color color, boolean on)
        {
        for (Light l : my_lights)
            {
            l.color = color;
            l.on = on;
            }
        }

  // ----- fillXPlane() -----------------------------------------------
  //
    public void fillXPlane (int x_idx, Light[][][] lights, Color color)
        {
        fillXPlane (x_idx, lights, color, true);
        }

    public void fillXPlane (int x_idx, Light[][][] lights,
                            Color color, boolean on)
        {
        if (x_idx < 0 || lights.length <= x_idx)
            return;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        for (int iy=0; iy<ny; iy++)
            for (int iz=0; iz<nz; iz++)
              { Light l = lights[x_idx][iy][iz];
                l.setColor (color);
                l.on = on;
                }
        }
    
  // ----- fillYPlane() -----------------------------------------------
  //
    public void fillYPlane (int y_idx, Light[][][] lights, Color color)
        {
        fillYPlane (y_idx, lights, color, true);
        }

    public void fillYPlane (int y_idx, Light[][][] lights,
                            Color color, boolean on)
        {
        if (y_idx < 0 || lights[0].length <= y_idx)
            return;
        int nx = lights.length;
        int nz = lights[0][0].length;
        for (int ix=0; ix<nx; ix++)
            for (int iz=0; iz<nz; iz++)
              { Light l = lights[ix][y_idx][iz];
                l.setColor (color);
                l.on = on;
                }
        }
    
  // ----- fillZPlane() -----------------------------------------------
  //
    public void fillZPlane (int z_idx, Light[][][] lights, Color color)
        {
        fillYPlane (z_idx, lights, color, true);
        }

    public void fillZPlane (int z_idx, Light[][][] lights,
                            Color color, boolean on)
        {
        if (z_idx < 0 || lights[0][0].length <= z_idx)
            return;
        int nx = lights.length;
        int ny = lights[0].length;
        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
              { Light l = lights[ix][iy][z_idx];
                l.setColor (color);
                l.on = on;
                }
        }
    
  // ----- get_temp_light() -------------------------------------------
  //
    private Light get_temp_light()
        {
        if (temp_idx > temp_lights.size())
            throw new IndexOutOfBoundsException(
                            "in LightArray.get_temp_light()");
        if (temp_idx == temp_lights.size())
            temp_lights.add (new Light());
        return temp_lights.get (temp_idx++);
        }

  // ----- getTempPlane() ----------------------------------------------
  //
    public Light[][] getTempPlane (int nx, int ny)
        {
        if (nx <= 0 || ny <= 0)
            return null;

        Light[][] lights = new Light[nx][ny];
        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
                lights[ix][iy] = get_temp_light();
        temp_objects.add (lights);
        return lights;
        }

  // ----- releaseTemps() ---------------------------------------------
  //
    public void releaseTemps()
        {
        for (Object obj : temp_objects)
          { if (obj instanceof Light[][])
              { Light[][] lights = (Light[][]) obj;
                int nx = lights.length;
                int ny = lights[0].length;
                for (int ix=0; ix<nx; ix++)
                    for (int iy=0; iy<ny; iy++)
                        lights[ix][iy] = null;
                }

            }
        while (!temp_objects.isEmpty())
            temp_objects.remove(0);
        temp_idx = 0;
        }

  // ----- reset() ----------------------------------------------
  //
    public void reset()
        {
        for (Light l : my_lights)
            {
            l.color = Color.LIGHT_GRAY;
            l.on = false;
            }
        }

  // ----- save_plane_check() ----------------------------------------
  //
    private boolean save_plane_check (int plane_idx, Axis axis,
                                        Light[][][] lights)
        {
        if (lights.length == 0)     return false;
        if (lights[0].length == 0)  return false;
        if (lights[0][0].length == 0)  return false;

        if (plane_idx < 0)  return false;

        boolean result = true;
        switch (axis)
          { case X_AXIS:
                result = plane_idx < lights.length;
                break;
            case Y_AXIS:
                result = plane_idx < lights[0].length;
                break;
            case Z_AXIS:
                result = plane_idx < lights[0][0].length;
                break;
            default:
                result = false;
            }
        return result;
        }
    
  // ----- saveXPlane() ----------------------------------------------
  //
    public Light[][] saveXPlane (int x_idx, Light[][][] lights)
        {
        if (!save_plane_check (x_idx, Axis.X_AXIS, lights))
            return null;

        int ny = lights[0].length;
        int nz = lights[0][0].length;
        Light[][] saved_plane = getTempPlane (ny, nz);
        for (int iy=0; iy<ny; iy++)
            for (int iz=0; iz<nz; iz++)
              { saved_plane[iy][iz].setState (lights[x_idx][iy][iz]);
                }

        return saved_plane;
        }

  // ----- saveYPlane() ----------------------------------------------
  //
    public Light[][] saveYPlane (int y_idx, Light[][][] lights)
        {
        if (!save_plane_check (y_idx, Axis.Y_AXIS, lights))
            return null;

        int nx = lights.length;
        int nz = lights[0][0].length;
        Light[][] saved_plane = getTempPlane (nx, nz);
        for (int ix=0; ix<nx; ix++)
            for (int iz=0; iz<nz; iz++)
              { saved_plane[ix][iz].setState (lights[ix][y_idx][iz]);
                }

        return saved_plane;
        }

  // ----- saveZPlane() ----------------------------------------------
  //
    public Light[][] saveZPlane (int z_idx, Light[][][] lights)
        {
        if (!save_plane_check (z_idx, Axis.Z_AXIS, lights))
            return null;

        int nx = lights.length;
        int ny = lights[0].length;
        Light[][] saved_plane = getTempPlane (nx, ny);
        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
              { saved_plane[ix][iy].setState (lights[ix][iy][z_idx]);
                }

        return saved_plane;
        }

  // ----- set_plane_check() -----------------------------------------
  //
    private boolean set_plane_check (int plane_idx, Axis axis,
                                        Light[][] plane,
                                        Light[][][] lights)
        {
        int npx,npy, nx,ny,nz;
        if ((nx=lights.length) == 0)     return false;
        if ((ny=lights[0].length) == 0)  return false;
        if ((nz=lights[0][0].length) == 0)  return false;

        if ((npx=plane.length) == 0)    return false;
        if ((npy=plane[0].length) == 0)    return false;

        if (plane_idx < 0)  return false;

      // Are the array and grid dimensions commensurate?
      //
        boolean result;
        switch (axis)
          { case X_AXIS:
                result =    npx == ny && npy == nz
                         && plane_idx < lights.length;
                break;
            case Y_AXIS:
                result =    npx == nx && npy == nz
                         && plane_idx < lights[0].length;
                break;
            case Z_AXIS:
                result =    npx == nx && npy == ny
                         && plane_idx < lights[0][0].length;
                break;
            default:
                result = false;
            }
        return result;
        }

  // ----- setXPlane() ----------------------------------------------
  //
    public void setXPlane (Light[][] src_plane,
                            int x_idx, Light[][][] dest_lights)
        {
        if (!set_plane_check (x_idx, Axis.X_AXIS, src_plane, dest_lights))
            return;

        int ny = src_plane.length;
        int nz = src_plane[0].length;
        for (int iy=0; iy<ny; iy++)
            for (int iz=0; iz<nz; iz++)
              { dest_lights[x_idx][iy][iz].color
                        = src_plane[iy][iz].color;
                dest_lights[x_idx][iy][iz].on
                        = src_plane[iy][iz].on;
                }
        }

  // ----- setYPlane() ----------------------------------------------
  //
    public void setYPlane (Light[][] src_plane,
                            int y_idx, Light[][][] dest_lights)
        {
        if (!set_plane_check (y_idx, Axis.Y_AXIS, src_plane, dest_lights))
            return;

        int nx = src_plane.length;
        int nz = src_plane[0].length;
        for (int ix=0; ix<nx; ix++)
            for (int iz=0; iz<nz; iz++)
              { dest_lights[ix][y_idx][iz].color
                        = src_plane[ix][iz].color;
                dest_lights[ix][y_idx][iz].on
                        = src_plane[ix][iz].on;
                }
        }

  // ----- setZPlane() ----------------------------------------------
  //
    public void setZPlane (Light[][] src_plane,
                            int z_idx, Light[][][] dest_lights)
        {
        if (!set_plane_check (z_idx, Axis.Z_AXIS, src_plane, dest_lights))
            return;

        int nx = src_plane.length;
        int ny = src_plane[0].length;
        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
              { dest_lights[ix][iy][z_idx].color
                        = src_plane[ix][iy].color;
                dest_lights[ix][iy][z_idx].on
                        = src_plane[ix][iy].on;
                }
        }

  // ----- shift_check() ----------------------------------------------
  //
    private boolean shift_check (int shift, int axis, Light lights[][][])
        {
        if (lights.length == 0)     return false;
        if (lights[0].length == 0)  return false;
        if (lights[0][0].length == 0)  return false;
        if (shift != -1 && shift != 1)  return false;

        boolean result = true;
        switch (axis)
          { case 0:  result = lights.length > 0;  break;
            case 1:  result = lights[0].length > 0;  break;
            case 2:  result = lights[0][0].length > 0;  break;
            default: result = false;
          }
        return result;
        }
    
  // ----- shiftAlongXAxis() ------------------------------------------
  //
  // Shift the light states along the x-axis with wrap around.
  //
    public void shiftAlongXAxis (int shift, Light lights[][][])
        {
        if (!shift_check (shift, 0, lights))  return;
        
        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        if (nx < 2)  return;
        
        int x_src, x_dest;
        int nxm1 = nx - 1;

      // Save the light states in the wrap around layer.
      //
        Light[][] saved_plane;
        if (shift == -1)
          { x_src = 1;
            x_dest = 0;
            saved_plane = saveXPlane (0, lights);
            }
          else
          { x_src = nxm1-1;
            x_dest = nxm1;
            saved_plane = saveXPlane (nxm1, lights);
            }
        
      // Shift the light states.
      //
        for (int i=0; i<nxm1; i++)
          { for (int iy=0; iy<ny; iy++)
                for (int iz=0; iz<nz; iz++)
                    lights[x_dest][iy][iz].setState (lights[x_src][iy][iz]);
            x_src -= shift;
            x_dest -= shift;
            }

      // Set final plane of lights from the saved values.
      //
        if (shift == -1)
          { setXPlane (saved_plane, nxm1, lights);
            }
          else
          { setXPlane (saved_plane, 0, lights);
            }

        releaseTemps();
        }

  // ----- shiftAlongYAxis() ------------------------------------------
  //
  // Shift the light states along the y-axis with wrap around.
  //
    public void shiftAlongYAxis (int shift, Light lights[][][])
        {
        if (!shift_check (shift, 0, lights))  return;

        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        if (ny < 2)  return;

        int y_src, y_dest;
        int nym1 = ny - 1;

      // Save the light states in the wrap around layer.
      //
        Light[][] saved_plane;
        if (shift == -1)
          { y_src = 1;
            y_dest = 0;
            saved_plane = saveYPlane (0, lights);
            }
          else
          { y_src = nym1-1;
            y_dest = nym1;
            saved_plane = saveYPlane (nym1, lights);
            }

      // Shift the light states.
      //
        for (int i=0; i<nym1; i++)
          { for (int ix=0; ix<nx; ix++)
                for (int iz=0; iz<nz; iz++)
                    lights[ix][y_dest][iz].setState (lights[ix][y_src][iz]);
            y_src -= shift;
            y_dest -= shift;
            }

      // Set final plane of lights from the saved values.
      //
        if (shift == -1)
          { setYPlane (saved_plane, nym1, lights);
            }
          else
          { setYPlane (saved_plane, 0, lights);
            }

        releaseTemps();
        }

  // ----- shiftAlongZAxis() ------------------------------------------
  //
  // Shift the light states along the x-axis with wrap around.
  //
    public void shiftAlongZAxis (int shift, Light lights[][][])
        {
        if (!shift_check (shift, 0, lights))  return;

        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        if (nz < 2)  return;

        int z_src, z_dest;
        int nzm1 = nz - 1;

      // Save the light states in the wrap around layer.
      //
        Light[][] saved_plane;
        if (shift == -1)
          { z_src = 1;
            z_dest = 0;
            saved_plane = saveZPlane (0, lights);
            }
          else
          { z_src = nzm1-1;
            z_dest = nzm1;
            saved_plane = saveZPlane (nzm1, lights);
            }

      // Shift the light states.
      //
        for (int i=0; i<nzm1; i++)
          { for (int ix=0; ix<nx; ix++)
                for (int iy=0; iy<ny; iy++)
                    lights[ix][iy][z_dest].setState (lights[ix][iy][z_src]);
            z_src -= shift;
            z_dest -= shift;
            }

      // Set final plane of lights from the saved values.
      //
        if (shift == -1)
          { setZPlane (saved_plane, nzm1, lights);
            }
          else
          { setZPlane (saved_plane, 0, lights);
            }

        releaseTemps();
        }

  // ----- shiftOutAlongXAxis() ---------------------------------------
  //
  // Shift the light states along the x-axis, and fill behind the shift
  // with lights that are black and turned off.
  //
    public void shiftOutAlongXAxis (int shift, Light lights[][][])
        {
        if (!shift_check (shift, 0, lights))  return;

        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        if (nx < 2)  return;

        int x_src, x_dest;
        int nxm1 = nx - 1;

      // Set up the source and destination indices.
      //
        if (shift == -1)
          { x_src = 1;
            x_dest = 0;
            }
          else
          { x_src = nxm1 - 1;
            x_dest = nxm1;
            }

      // Shift the light states.
      //
        for (int i=0; i<nxm1; i++)
          { for (int iy=0; iy<ny; iy++)
                for (int iz=0; iz<nz; iz++)
                    lights[x_dest][iy][iz].setState (lights[x_src][iy][iz]);
            x_src -= shift;
            x_dest -= shift;
            }

      // Set final plane of lights from the saved values.
      //
        if (shift == -1)
          { fillXPlane (nxm1, lights, Color.BLACK, false);
            }
          else
          { fillXPlane (0, lights, Color.BLACK, false);
            }
        }

  // ----- shiftOutAlongXAxis() ---------------------------------------
  //
  // Shift the light states along the x-axis, and fill behind the shift
  // with lights that are black and turned off.
  //
    public void shiftOutAlongYAxis (int shift, Light lights[][][])
        {
        if (!shift_check (shift, 0, lights))  return;

        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        if (nx < 2)  return;

        int y_src, y_dest;
        int nym1 = ny - 1;

      // Set up the source and destination indices.
      //
        if (shift == -1)
          { y_src = 1;
            y_dest = 0;
            }
          else
          { y_src = nym1 - 1;
            y_dest = nym1;
            }

      // Shift the light states.
      //
        for (int i=0; i<nym1; i++)
          { for (int ix=0; ix<nx; ix++)
                for (int iz=0; iz<nz; iz++)
                    lights[ix][y_dest][iz].setState (lights[ix][y_src][iz]);
            y_src -= shift;
            y_dest -= shift;
            }

      // Set final plane of lights from the saved values.
      //
        if (shift == -1)
          { fillYPlane (nym1, lights, Color.BLACK, false);
            }
          else
          { fillYPlane (0, lights, Color.BLACK, false);
            }
        }

  // ----- shiftOutAlongZAxis() ---------------------------------------
  //
  // Shift the light states along the x-axis, and fill behind the shift
  // with lights that are black and turned off.
  //
    public void shiftOutAlongZAxis (int shift, Light lights[][][])
        {
        if (!shift_check (shift, 0, lights))  return;

        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        if (nx < 2)  return;

        int z_src, z_dest;
        int nzm1 = nz - 1;

      // Set up the source and destination indices.
      //
        if (shift == -1)
          { z_src = 1;
            z_dest = 0;
            }
          else
          { z_src = nzm1 - 1;
            z_dest = nzm1;
            }

      // Shift the light states.
      //
        for (int i=0; i<nzm1; i++)
          { for (int ix=0; ix<nx; ix++)
                for (int iy=0; iy<nz; iy++)
                    lights[ix][iy][z_dest].setState (lights[ix][iy][z_src]);
            z_src -= shift;
            z_dest -= shift;
            }

      // Set final plane of lights from the saved values.
      //
        if (shift == -1)
          { fillZPlane (nzm1, lights, Color.BLACK, false);
            }
          else
          { fillZPlane (0, lights, Color.BLACK, false);
            }
        }

    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
