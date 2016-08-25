//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 24, 2016
//
//*************************************************************************

//----------------------------------------- StarBurstController.java -----

package lightsim;

import java.awt.Color;
import java.util.ArrayList;

import lightsim.LightArray.Light;

//======================================================================
// class StarBurstController
//======================================================================

public class StarBurstController extends SpriteController
    {
    private Light[][][] lights;
    private ArrayList<Sprite>  dead_sprites, new_sprites;
    private static final int N_STARS = 1;

  // ----- constructor -------------------------------------------------
  //
    public StarBurstController()
        {
        dead_sprites = new ArrayList<>();
        new_sprites = new ArrayList<>();
        }

  // ----- name() ------------------------------------------------------
  //
    @Override
    public String name()    { return "Star Bursts"; }

  // ----- init() ------------------------------------------------------
  //
    @Override
    public void init (LightArray light_array)
        {
        super.init (light_array);
        lights = my_light_array.getAllLights();

        for (int i=0; i<N_STARS; i++)
          { Light l = pickRandomLight (lights);
            l.setState (Color.GREEN, true);
            StarBurstSprite star = new StarBurstSprite (l, lights);
            my_sprites.add (star);
            }
        }

  // ----- step() ------------------------------------------------------
  //
    @Override
    public boolean step (int clock)
        {
        for (Sprite sprite : my_sprites)
          { boolean alive = sprite.step(clock);
            if (!alive)
              { dead_sprites.add (sprite);
                Light l = pickRandomLight (lights);
                l.setState (Color.GREEN, true);
                StarBurstSprite star = new StarBurstSprite (l, lights);
                new_sprites.add (star);
                }
            }

        for (Sprite sprite : dead_sprites)
            {
            my_sprites.remove (sprite);
            }
        dead_sprites.clear();

        for (Sprite sprite : new_sprites)
            {
            my_sprites.add (sprite);
            }
        new_sprites.clear();

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
