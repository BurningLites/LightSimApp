//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 25, 2016
//
//*************************************************************************

//----------------------------------------- SnakeSprite.java -----

package lightsim;

//======================================================================

import java.awt.Color;
import java.util.ArrayList;
import lightsim.LightArray.Light;

// class SnakeSprite
//======================================================================

public class SnakeSprite extends Sprite
    {
  // Container for the body "segments".  The body head is at
  // the beginning of the list.
  //
    private ArrayList<LightArray.Light> body, adjacent_lights;
    private ArrayList<Sprite>  fellow_sprites;
    private LightArray.Light  head;
    private Color  color;
    private int t_next;
    private int my_dt = 125, eat_dt = 33;
    private int countdown, eat_cycle;
    private boolean eating_snake;
    private SnakeSprite my_big_meal;
    private boolean died;
    private Color[] color_ramp = new Color[1000/eat_dt];
    private static final int EAT_COUNTDOWN = 4;
    private Light[][][] my_lights;

    public SnakeSprite (Color color, Light[][][] lights)
        {
        this.color = color;
        my_lights = lights;

        body = new ArrayList<>();
        adjacent_lights = new ArrayList<>();
        build_snake();
        t_next = 0;

      // Set the snake's color, and create a color ramp to use
      // when eating another snake.
      //
        int istart = 0, iend = color_ramp.length / 2;
        float red   = color.getRed() / 255.0f,
              green = color.getGreen() / 255.0f,
              blue  = color.getBlue() / 255.0f;
        for (int i=istart; i<iend; i++)
          { float darker = 1.0f - 0.3f*i/(iend-istart-1);
            color_ramp[i]
                = new Color (darker*red, darker*green, darker*blue);
            }
        istart = iend;
        iend = color_ramp.length;
        for (int i=istart; i<iend; i++)
          { float darker = 1.0f - 0.3f*(iend-i-1)/(iend-istart);
            color_ramp[i]
                = new Color (darker*red, darker*green, darker*blue);
            }
        }

    public boolean died()   { return died; }
    public ArrayList<LightArray.Light> getBody()   { return body; }
    public Color getColor() { return color; }
    public int getSize()    { return body.size(); }
    public boolean isEating()   { return eating_snake; }
    public void setSprites (ArrayList<Sprite> sprites)
        { fellow_sprites = sprites; }

    public void addLight (LightArray.Light light)
        {
        if (body.isEmpty())
          { head = light;
            light.setState (Color.MAGENTA, true);
            body.add (light);
            }
          else
          { light.setState (color, true);
            body.add (0, light);
            }
        }

  // ----- build_snake() --------------------
  //
    private void build_snake()
        {
        boolean snake_is_complete = false;
      build_snake:
        do
            {
            clear();
            alive = true;

          // Starting with a randomly chosen initial position, create
          // the snake one light at a time.
          //
            LightArray.Light l 
                = SpriteController.pickRandomLight(my_lights);
            addLight (l);

          // Add three more points to the snake.  If we can't find a
          // string of six adjacent lights, try again.
          //
            int ix = l.ix, iy = l.iy, iz = l.iz;
            if (ix > 4)  ix = ix - 12;
            for (int n=0; n<5; n++)
              { grow();
                if (!alive)
                    continue build_snake;
                }
            snake_is_complete = true;
            }
        while (!snake_is_complete);
        update_color (color);
        }

  // ----- clear() --------------------------
  //
    @Override
    public void clear()
        {
        for (LightArray.Light l : body)
            l.setState (Color.BLACK, false);
        body.clear();
        head = null;
        my_big_meal = null;
        }

    public LightArray.Light getHead()              { return head; }
    public LightArray.Light getTail()
        {
        if (!body.isEmpty())
            return body.get(0);
          else
            return null;
        }

    @Override
    public boolean hasLight (LightArray.Light l)   { return body.contains (l); }

  // ----- findSprite() ------------------------------------------------
  //
  // Find the sprite that has the given light.
  //
    public Sprite findSprite (LightArray.Light light)
        {
        for (Sprite sprite : fellow_sprites)
          { if (sprite.hasLight (light))
                return sprite;
            }
        return null;
        }

  // ----- grow() -------------------------
  //
    private void grow()
        {
        LightArray.Light tail = getTail();
        int ix = tail.ix;
        if (ix > 4)  ix = ix - 12;
        int iy = tail.iy;
        int iz = tail.iz;
        SpriteController.findAdjacentLights (ix, iy, iz, my_lights, this,
                               adjacent_lights);
        if (adjacent_lights.size() > 0)
          { int pick = LSUtils.pickNumber (0, adjacent_lights.size()-1);
            LightArray.Light new_tail = adjacent_lights.get (pick);
            new_tail.setState (color, true);
            body.add (0, new_tail);
            }
          else
          { died = true;
            eating_snake = false;
            setAlive (false);
            }
        }

  // ----- eat_snake() --------------------
  //
    private void eat_snake()
        {
        eat_cycle = ++eat_cycle % (1000 / eat_dt);
        update_color (color_ramp[eat_cycle]);

      // Preliminaries - wait until the victim is really dead.
      //
        if (countdown > 0)
          { if (eat_cycle == 0)
              { --countdown;
                float fcd = countdown;
                float pale_factor
                    = (EAT_COUNTDOWN - fcd) / EAT_COUNTDOWN;
                float red = my_big_meal.color.getRed();
                red = Float.min (1.0f, red + pale_factor*(1.0f-red));
                float green = my_big_meal.color.getGreen();
                green = Float.min (1.0f, green + pale_factor*(1.0f-green));
                float blue = my_big_meal.color.getBlue();
                blue = Float.min (1.0f, blue + pale_factor*(1.0f-blue));
                my_big_meal.update_color (new Color(red,green,blue));
                }
            }
          else
          { if (eat_cycle % 3 == 0)
                eat_swallow();
            }
        }

  // ---- eat_swallow() ---------------------
  //
    private void eat_swallow()
        {
        LightArray.Light its_tail = my_big_meal.getTail();
        if (its_tail != null)
          { if (its_tail == head)
              { int n = my_big_meal.getSize();
                if (n > 2)
                  { LightArray.Light new_head = my_big_meal.body.get(n-2);
                    new_head.setState (my_big_meal.getHead());
                    my_big_meal.head.setState (Color.BLACK, false);
                    my_big_meal.head = new_head;
                    my_big_meal.body.remove (n-1);
                    }
                  else
                  { my_big_meal.head.setState (Color.BLACK, false);
                    while (my_big_meal.getSize() > 0)
                        my_big_meal.body.remove(0);
                    }
                }
              else
              { my_big_meal.removeLight (its_tail);
                }
            grow();
            }
          else
          { eating_snake = false;
            my_dt = 125 + 3*body.size();
            update_color (color);
            }
        }

  // ----- attack_snake() -------------------
  //
  // We didn't find an apple in the adjacent_lights, but there
  // may be a snake.  Pick a direction, and if it's a snake,
  // attack it.
  //
    private LightArray.Light attack_snake_maybe (
                          ArrayList<LightArray.Light> adjacent_lights)
        {
        int pick = LSUtils.pickNumber (0, adjacent_lights.size()-1);
        LightArray.Light l = adjacent_lights.get (pick);
        if (l.color != Color.BLACK)
          { Sprite sprite = findSprite (l);
            if (sprite != null && sprite instanceof SnakeSprite)
              { eating_snake = true;
                my_big_meal = (SnakeSprite) sprite;
                my_big_meal.setAlive (false);
                eat_cycle = 0;
                countdown = EAT_COUNTDOWN;
                my_dt = eat_dt;
                }
            }
        return l;
        }

      // ----- eat_something() ------------------
      //
      // Check the adjacent lights for food.  If found, eat
      // the food and return that light for use as the next light.
      // Otherwise, return null.
      //
        private LightArray.Light eat_something_maybe (
                            ArrayList<LightArray.Light> adjacent_lights)
            {
            for (LightArray.Light l : adjacent_lights)
              { if (l.color != Color.BLACK)
                  { Sprite food = findSprite (l);
                    if (food != null && food instanceof AppleSprite)
                      { food.setAlive (false);
                        grow();
                        return l;
                        }
                    }
                }
            return null;
            }

  // ----- explode() ----------------------------------------
  //
    public void explode (ArrayList<Sprite> new_sprites)
        {
        for (LightArray.Light l : body)
            {
            StarBurstSprite star = new StarBurstSprite (l, my_lights);
            l.setColor (Color.WHITE);
            int delay = (int) (1000*Math.random() + 0.5);
            star.setDelay (delay);
            new_sprites.add (star);
            }
        eating_snake = false;
        }

  // ----- move() ---------------------------
  //
    public void move()
        {
        int ix = head.ix;
        if (ix > 4)  ix = ix - 12;
        int iy = head.iy;
        int iz = head.iz;
        SpriteController.findAdjacentLights (ix, iy, iz,
                               my_lights, this, adjacent_lights);
        if (adjacent_lights.size() > 0)
          { LightArray.Light next_light
                           = eat_something_maybe (adjacent_lights);
            if (next_light == null)
              { next_light = attack_snake_maybe (adjacent_lights);
                }
            nextLight (next_light);
            }
          else
          { died = true;
            setAlive (false);
            }
        }

  // ----- nextLight() ----------------------
  //
    public void nextLight (LightArray.Light light)
        {
        head.setColor (color);
        LightArray.Light tail = body.get(0);
        removeLight (tail);

        light.setState (Color.MAGENTA, true);
        head = light;
        body.add (light);
        }

  // ----- removeLight() --------------------
  //
    public void removeLight (LightArray.Light l)
        {
        l.setState (Color.BLACK, false);
        body.remove (l);
        }

  // ----- step() ---------------------------
  //
    @Override
    public boolean step (int clock)
        {
        if (alive && clock >= t_next)
          { t_next = clock + my_dt;
            if (eating_snake)
                eat_snake();
              else
                move();
            }
        return alive;
        }

  // ----- update_color() ----------------------
  //
    public void update_color()
        {
        update_color (color);
        }
    private void update_color (Color color)
        {
        int nm1 = body.size() - 1;   // Avoid head.
        for (int i=0; i<nm1; i++)
            body.get(i).setColor (color);
        head.setState (Color.MAGENTA, true);
        }

    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
