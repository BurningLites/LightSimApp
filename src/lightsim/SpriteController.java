//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 24, 2016
//
//*************************************************************************

//----------------------------------------- SpriteController.java -----

package lightsim;

import java.awt.Color;
import java.util.ArrayList;

import lightsim.LightArray.Light;

//======================================================================
// class SpriteController
//======================================================================
//
// Base class for controllers that use Sprites.
//

public class SpriteController extends LightController
    {
    protected ArrayList<Light>  adjacent_lights;
    protected ArrayList<Sprite>  my_sprites;

  // ----- constructor -----------------------------------------------
  //
    public SpriteController()
        {
        adjacent_lights = new ArrayList<>();
        my_sprites = new ArrayList<>();
        }

  // ----- name() ------------------------------------------------------
  //
    @Override
    public String name()    { return "base Sprite Controller"; }

  // ----- init() ------------------------------------------------------
  //
    @Override
    public void init (LightArray light_array)
        {
        super.init (light_array);
        my_light_array.fill (Color.BLACK, false);
        }
    
  // ----- step() ------------------------------------------------------
  //
    @Override
    public boolean step (int clock)
        {
        increment_step();
        return true;
        }
    
    
      // ----- find_adjacent_lights() --------------------------------------
  //
  // Find lights adjacent to a given position that are (1) inside the
  // grid of lights, (2) not already part of the snake, (3) not an
  // adjacent corner or diagonal.
  //
    private static final boolean[][][] NOT_OK =
      { { { true,  true, true  },
          { true, false, true },
          { true,  true, true  } },
        { { true, false, true },
          { false, false, false },
          { true, false, true } },
        { { true,  true, true  },
          { true, false, true },
          { true,  true, true  } }
      };

    public static void findAdjacentLights (int ix, int iy, int iz,
                                        Light[][][] lights, Sprite self,
                                        ArrayList<Light> adjacent_lights)
        {
        adjacent_lights.clear();

        int nx = lights.length;
        int ny = lights[0].length;
        int nz = lights[0][0].length;
        int nxm1 = nx - 1;
        int nym1 = ny - 1;
        int nzm1 = nz - 1;
        
        for (int dx=-1; dx<=1; dx++)
            for (int dy=-1; dy<=1; dy++)
                for (int dz=-1; dz<=1; dz++)
                    {
                  // Exclude the original position
                  //
                    if (dx==0 && dy==0 && dz==0)
                        continue;

                  // Exclude diagnonals and corners.
                  //
                    if (NOT_OK[dx+1][dy+1][dz+1])
                        continue;
                    
                  // Calculcate a set of adjacent coordinates and
                  // check that they are within the light grid.
                  //
                    int x = ix+dx;
                    if (x < 0 || nxm1 < x)  continue;
                    int y = iy+dy;
                    if (y < 0 || nym1 < y)  continue;
                    int z=iz+dz;
                    if (z < 0 || nzm1 < z)  continue;
                    
                  // Check that the position is not part of the 
                  // snake or any other object.
                  //
                    LightArray.Light adjacent_light = lights[x][y][z];
                    if (    (self != null && !self.hasLight(adjacent_light))
                         || adjacent_light.color == Color.BLACK
                         )
                        adjacent_lights.add (adjacent_light);
                    }
        }

  // ----- findSprite() ------------------------------------------------
  //
  // Find the sprite that has the given light.
  //
    public Sprite findSprite (LightArray.Light light)
        {
        for (Sprite sprite : my_sprites)
          { if (sprite.hasLight (light))
                return sprite;
            }
        return null;
        }

  // ----- pickRandomLight() -------------------------------------------
  //
  // Randomly pick a light that is not part of any other sprite.
  // A light that is not part of a sprite has its color set to BLACK.
  //
    public static Light pickRandomLight (Light[][][] lights)
        {
        int nxm1 = lights.length - 1,
            nym1 = lights[0].length - 1,
            nzm1 = lights[0][0].length - 1;
        boolean found_light = false;
        Light light = null;
        while (!found_light)
          { int ix = LSUtils.pickNumber (0, nxm1);
            int iy = LSUtils.pickNumber (0, nym1);
            int iz = LSUtils.pickNumber (0, nzm1);
            light = lights[ix][iy][iz];
            found_light = light.color == Color.BLACK;
            }
        return light;
        }

  // ----- reset() -----------------------------------------------------
  //
  // Reset lights with BLACK to indicate absence of a sprite and with
  // their state set to off.
  //
    public void reset (Light[][][] lights)
        {
        int nx = lights.length,
            ny = lights[0].length,
            nz = lights[0][0].length;
        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
                for (int iz=0; iz<nz; iz++)
          { Light l = lights[ix][iy][iz];
            l.color = Color.BLACK;
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
