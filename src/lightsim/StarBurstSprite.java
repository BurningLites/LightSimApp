//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 24, 2016
//
//*************************************************************************

//----------------------------------------- StarBurstSprite.java -----

package lightsim;

import java.awt.Color;

import lightsim.LightArray.Light;

//======================================================================
// class StarBurstSprite
//======================================================================

public class StarBurstSprite extends Sprite
    {
    Light light;
    Light[][][] lights;
    int xc, yc, zc;
    int nx, ny, nz, nxm1, nym1, nzm1;
    int delay, t_next, step_count, step_limit, dt = 200;
    Color[] colors;

  // ----- constructor ------------------------------------------------
  //
    public StarBurstSprite (Light light, Light[][][] lights)
        {
        this.light = light;
        xc = light.ix;
        if (xc > 4)  xc = xc - 7;
        yc = light.iy;
        zc = light.iz;

        this.lights = lights;
        nx = lights.length;
        ny = lights[0].length;
        nz = lights[0][0].length;
        nxm1 = nx - 1;
        nym1 = ny - 1;
        nzm1 = nz - 1;

        delay = 1;
        t_next = 0;
        step_count = 0;
        step_limit = 2;

        colors = new Color[step_limit];
        for (int i=0; i<step_limit; i++)
          { float factor = 1.0f - 0.25f*(i+1);
            colors[i] =
                new Color (factor*light.color.getRed()/255.0f,
                           factor*light.color.getGreen()/255.0f,
                           factor*light.color.getBlue()/255.0f);
            }
        }

  // ----- access methods ---------------------------------------------
  //
    public int  getDelay()              { return delay; }
    public void setDelay (int value)    { delay = value; }

  // ----- step() -----------------------------------------------------
  //
    public boolean step (int clock)
        {
        if (t_next == 0)
            t_next = clock + delay;
        if (clock >= t_next)
          { t_next = clock + dt;

          // If this is not the beginning of the burst, try to erase
          // the previous set of voxels.
          //
            int delta = step_count;
            int d_inc = delta == 1 ? 2*delta : delta;
            if (step_count == 0)
              { light.setState (Color.WHITE, true);
                }
              else
              { if (delta-1 >= colors.length)
                    System.out.println ("step_count,delta=" + step_count + " "
                                        + delta);
                int old_rgb = colors[delta-1].getRGB();
                for (int ix=-delta; ix<=delta; ix+=d_inc)
                    for (int iy=-delta; iy<=delta; iy+=d_inc)
                        for (int iz=-delta; iz<=delta; iz+=d_inc)
                          { if (ix==0 && iy==0 && iz==0)
                                continue;
                            int x = xc + ix;
                            int y = yc + iy;
                            int z = zc + iz;
                            if (in_bounds (x, y, z))
                              { Light l = lights[x][y][z];
                                int rgb = l.color.getRGB();
                                rgb = rgb ^ old_rgb;
                                lights[x][y][z].setState (
                                            new Color(rgb), true);
                                }
                            }
                }

            delta++;
            d_inc = delta == 1 ? 2*delta : delta;
            if (step_count == step_limit)
              { light.setState (Color.BLACK, false);
                }
              else
              { for (int ix=-delta; ix<=delta; ix+=d_inc)
                    for (int iy=-delta; iy<=delta; iy+=d_inc)
                        for (int iz=-delta; iz<=delta; iz+=d_inc)
                          { if (ix==0 && iy==0 && iz==0)
                                continue;
                            int x = xc + ix;
                            int y = yc + iy;
                            int z = zc + iz;
                            if (in_bounds (x, y, z))
                                lights[x][y][z].setState (
                                            colors[step_count], true);
                            }
                }
            alive = ++step_count <= step_limit;
            }
        return alive;
        }

    private boolean in_bounds (int x, int y, int z)
        {
        return     0 <= x && x < nx
                && 0 <= y && y < ny
                && 0 <= z && z < nz;
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
