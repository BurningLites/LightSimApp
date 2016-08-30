//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 25, 2016
//
//*************************************************************************

//----------------------------------------- SimpleSnakesController.java -----

package lightsim;

import java.awt.Color;
import java.util.ArrayList;
import lightsim.LightArray.Light;

//======================================================================
// class SimpleSnakesController
//======================================================================
//
// This controller runs one snake each in the left and right arrays.
// The snakes wiggle around eating apples and growing until they can
// no longer move or grow at which point they explode.  Then a new
// snake begins.
//

public class SimpleSnakesController extends SpriteController
    {
    private Light[][][] left_lights, right_lights;
    private ArrayList<Sprite>  left_sprites, right_sprites,
                        left_dead_sprites, right_dead_sprites;
    private ArrayList left_new_sprites, right_new_sprites;

    private static final int N_APPLES = 5;
    private int new_delay = 2500, left_t_new, right_t_new;
    private boolean left_exploding, right_exploding,
                left_start_explosion, right_start_explosion;
    private int left_burst_count, right_burst_count;

  // ----- constructor -------------------------------------------------
  //
    public SimpleSnakesController()
        {
        left_sprites = new ArrayList<>();
        left_dead_sprites = new ArrayList<>();
        left_new_sprites = new ArrayList();

        right_sprites = new ArrayList<>();
        right_dead_sprites = new ArrayList<>();
        right_new_sprites = new ArrayList();
        }

  // ----- name() ------------------------------------------------------
  //
    @Override
    public String name()    { return "Simple Snakes"; }

  // ----- init() ------------------------------------------------------
  //
    @Override
    public void init (LightArray light_array)
        {
        super.init (light_array);
        left_lights = my_light_array.getLeftLights();
        right_lights = my_light_array.getRightLights();
        init_left();
        init_right();
        }

  // ----- init_left() -------------------------------------------------
  //
    public void init_left()
        {
      // Clean out everything
      //
        for (Sprite sprite : left_sprites)  sprite.clear();
        left_sprites.clear();
        for (Object obj : left_new_sprites)
            if (obj instanceof Sprite) ((Sprite) obj).clear();
        left_new_sprites.clear();
        for (Sprite sprite : left_dead_sprites)  sprite.clear();
        left_dead_sprites.clear();
        
        left_exploding = false;
        left_start_explosion = false;
        left_burst_count = 0;
        left_t_new = 0;
        reset (left_lights);
        
        for (int i=0; i<N_APPLES; i++)
            {
            Light l = SpriteController.pickRandomLight (left_lights);
            left_sprites.add (new AppleSprite(l));
            }

        SnakeSprite snake = new SnakeSprite (Color.GREEN, left_lights);
        left_sprites.add (snake);
        snake.setSprites (left_sprites);
        }

  // ----- init_right() -------------------------------------------------
  //
    public void init_right()
        {
      // Clean out everything
      //
        for (Sprite sprite : right_sprites)  sprite.clear();
        right_sprites.clear();
        for (Object obj : right_new_sprites)
            if (obj instanceof Sprite) ((Sprite) obj).clear();
        right_new_sprites.clear();
        for (Sprite sprite : right_dead_sprites)  sprite.clear();
        right_dead_sprites.clear();

        right_exploding = false;
        right_start_explosion = false;
        right_burst_count = 0;
        right_t_new = 0;
        reset (right_lights);

        for (int i=0; i<N_APPLES; i++)
          { Light l = SpriteController.pickRandomLight (right_lights);
            right_sprites.add (new AppleSprite(l));
            }

        SnakeSprite snake = new SnakeSprite (Color.ORANGE, right_lights);
        right_sprites.add (snake);
        snake.setSprites (right_sprites);
        }

  // ----- step() ------------------------------------------------------
  //
    @Override
    public boolean step (int clock)
        {
        step_left (clock);
        step_right (clock);

        increment_step();
        return true;
        }

  // ----- step_left() ------------------------------------------------
  //
    private boolean step_left (int clock)
        {
        for (Sprite sprite : left_sprites)
          { boolean alive = sprite.step (clock);
            if (!alive)
                {
                left_dead_sprites.add (sprite);
                if (sprite instanceof SnakeSprite)
                  { 
                    SnakeSprite dead_snake = (SnakeSprite) sprite;
                    if (dead_snake.died())
                      { dead_snake.explode (left_new_sprites);
                        left_exploding = true;
                        left_start_explosion = true;
                        }
                    left_new_sprites.add (dead_snake.getColor());
                    left_t_new = 0;
                    }
                  else if (sprite instanceof AppleSprite)
                  { left_new_sprites.add (
                        new AppleSprite(
                            SpriteController.pickRandomLight(left_lights)));
                    }
                  else if (sprite instanceof StarBurstSprite)
                  { --left_burst_count;
                    left_exploding = left_burst_count > 0;
                    if (!left_exploding)
                        reset (left_lights);
                    }
                }
            }

      // Always remove dead sprites from the list that we're animating.
      //
        for (Sprite dead_sprite : left_dead_sprites)
            left_sprites.remove (dead_sprite);
        left_dead_sprites.clear();

        if (left_start_explosion)
          { left_start_explosion = false;
            for (Object obj : left_new_sprites)
              { if (obj instanceof StarBurstSprite)
                  { StarBurstSprite star = (StarBurstSprite) obj;
                    left_burst_count++;
                    left_sprites.add (star);
                    left_dead_sprites.add (star);    // borrow use of dead_sprites
                    }
                }
            for (Sprite dead_sprite : left_dead_sprites) // remove StarBurstSprites
                left_new_sprites.remove (dead_sprite);   // and keep everything else
            left_dead_sprites.clear();
            }
        else if (!left_exploding)
          { if (left_t_new == 0)
                left_t_new = clock + new_delay;
            if (clock > left_t_new)
              { for (Object obj : left_new_sprites)
                  { if (obj instanceof Sprite)
                        left_sprites.add ((Sprite) obj);
                    if (obj instanceof Color)
                      { SnakeSprite snake = new SnakeSprite (
                                        ((Color) obj), left_lights);
                        snake.setSprites (left_sprites);
                        left_sprites.add (snake);
                        }
                    }
                left_new_sprites.clear();
                }
            }
        return true;
        }

  // ----- step_right() ------------------------------------------------
  //
    private boolean step_right (int clock)
        {
        for (Sprite sprite : right_sprites)
          { boolean alive = sprite.step (clock);
            if (!alive)
                {
                right_dead_sprites.add (sprite);
                if (sprite instanceof SnakeSprite)
                  {
                    SnakeSprite dead_snake = (SnakeSprite) sprite;
                    if (dead_snake.died())
                      { dead_snake.explode (right_new_sprites);
                        right_exploding = true;
                        right_start_explosion = true;
                        }
                    right_new_sprites.add (dead_snake.getColor());
                    right_t_new = 0;
                    }
                  else if (sprite instanceof AppleSprite)
                  { right_new_sprites.add (
                        new AppleSprite(
                            SpriteController.pickRandomLight(right_lights)));
                    }
                  else if (sprite instanceof StarBurstSprite)
                  { --right_burst_count;
                    right_exploding = right_burst_count > 0;
                    if (!right_exploding)
                        reset (right_lights);
                    }
                }
            }

      // Always remove dead sprites from the list that we're animating.
      //
        for (Sprite dead_sprite : right_dead_sprites)
            right_sprites.remove (dead_sprite);
        right_dead_sprites.clear();

        if (right_start_explosion)
          { right_start_explosion = false;
            for (Object obj : right_new_sprites)
              { if (obj instanceof StarBurstSprite)
                  { StarBurstSprite star = (StarBurstSprite) obj;
                    right_burst_count++;
                    right_sprites.add (star);
                    right_dead_sprites.add (star);    // borrow use of dead_sprites
                    }
                }
            for (Sprite dead_sprite : right_dead_sprites) // remove StarBurstSprites
                right_new_sprites.remove (dead_sprite);   // and keep everything else
            right_dead_sprites.clear();
            }
        else if (!right_exploding)
          { if (right_t_new == 0)
                right_t_new = clock + new_delay;
            if (clock > right_t_new)
              { for (Object obj : right_new_sprites)
                  { if (obj instanceof Sprite)
                        right_sprites.add ((Sprite) obj);
                    if (obj instanceof Color)
                      { SnakeSprite snake = new SnakeSprite (
                                        ((Color) obj), right_lights);
                        snake.setSprites (right_sprites);
                        right_sprites.add (snake);
                        }
                    }
                right_new_sprites.clear();
                }
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
