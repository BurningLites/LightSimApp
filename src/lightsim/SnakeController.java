//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 22, 2016
//
//*************************************************************************

//----------------------------------------- SnakeController.java -----

package lightsim;

import java.awt.Color;
import java.util.ArrayList;

import lightsim.LightArray.Light;

//======================================================================
// class SnakeController
//======================================================================
//
// This controller displays two "snakes" that roam across both light
// arrays.  The light arrays are also populated with red "apples" that
// provide food for the snakes allowing them to grow.  The snakes are
// also cannabalistic.  When a snake dies, it is replaced and the
// effect continues.
//
// This class introduces and is based on Sprites.
//

public class SnakeController extends SpriteController
    {

  //-------------------------------------------------------------------
  // inner class Snake
  //
    protected class Snake extends Sprite
        {
      // Container for the body "segments".  The body head is at
      // the beginning of the list.
      //
        private ArrayList<Light> body;
        private Light  head;
        private Color  color;
        private int t_next;
        private int my_dt = 125, eat_dt = 33;
        private int countdown, eat_cycle;
        private boolean eating_snake;
        private Snake my_big_meal;
        private boolean died;
        private Color[] color_ramp = new Color[1000/eat_dt];
        private static final int EAT_COUNTDOWN = 4;
        
        public Snake (Color color)
            {
            this.color = color;
            body = new ArrayList<>();
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
        public ArrayList<Light> getBody()   { return body; }
        public Color getColor() { return color; }
        public int getSize()    { return body.size(); }
        public boolean isEating()   { return eating_snake; }

        public void addLight (Light light)
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
                Light l = pick_random_light();
                addLight (l);

              // Add three more points to the snake.  If we can't find a
              // string of six adjacent lights, try again.
              //
                int ix = l.ix, iy = l.iy, iz = l.iz;
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
            for (Light l : body)
                l.setState (Color.BLACK, false);
            body.clear();
            head = null;
            my_big_meal = null;
            }

        public Light getHead()              { return head; }
        public Light getTail()              
            { 
            if (!body.isEmpty())
                return body.get(0);
              else
                return null;
            }

        @Override
        public boolean hasLight (Light l)   { return body.contains (l); }

      // ----- grow() -------------------------
      //
        private void grow()
            {
            Light tail = getTail();
            int ix = tail.ix;
            if (ix > 4)  ix = ix - 7;
            int iy = tail.iy;
            int iz = tail.iz;
            find_adjacent_lights (ix, iy, iz, this);
            if (adjacent_lights.size() > 0)
              { int pick = LSUtils.pickNumber (0, adjacent_lights.size()-1);
                Light new_tail = adjacent_lights.get (pick);
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
            Light its_tail = my_big_meal.getTail();
            if (its_tail != null)
              { if (its_tail == head)
                  { int n = my_big_meal.getSize();
                    if (n > 2)
                      { Light new_head = my_big_meal.body.get(n-2);
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
        private Light attack_snake_maybe (
                                ArrayList<Light> adjacent_lights)
            {
            int pick = LSUtils.pickNumber (0, adjacent_lights.size()-1);
            Light l = adjacent_lights.get (pick);
            if (l.color != Color.BLACK)
              { Sprite sprite = findSprite (l);
                if (sprite != null && sprite instanceof Snake)
                  { eating_snake = true;
                    my_big_meal = (Snake) sprite;
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
        private Light eat_something_maybe (
                                ArrayList<Light> adjacent_lights)
            {
            for (Light l : adjacent_lights)
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
    private void explode (ArrayList<Sprite> new_sprites, Light[][][] lights)
        {
        for (Light l : body)
            {
            StarBurstSprite star = new StarBurstSprite (l, lights);
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
            if (ix > 4)  ix = ix - 7;
            int iy = head.iy;
            int iz = head.iz;
            find_adjacent_lights (ix, iy, iz, this);
            if (adjacent_lights.size() > 0)
              { Light next_light = eat_something_maybe (adjacent_lights);
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
        public void nextLight (Light light)
            {
            head.setColor (color);
            Light tail = body.get(0);
            removeLight (tail);
            
            light.setState (Color.MAGENTA, true);
            head = light;
            body.add (light);
            }

      // ----- removeLight() --------------------
      //
        public void removeLight (Light l)
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
  //
  //-------------------------------------------------------------------
    
    Light[][][] lights;
    ArrayList<Sprite>  dead_sprites;
    ArrayList new_sprites;

    private static final int N_APPLES = 5;
    private int dead_snake_count;
    private int new_delay = 7000, t_new;
    private Color dead_snake_color;
    private boolean exploding, start_explosion;
    private int burst_count;
    
  // ----- constructor -------------------------------------------------
  //
    public SnakeController()
        {
        dead_sprites = new ArrayList<>();
        new_sprites = new ArrayList();
        }
    
  // ----- name() ------------------------------------------------------
  //
    @Override
    public String name()    { return "Snake"; }
    
  // ----- init() ------------------------------------------------------
  //
    @Override
    public void init (LightArray light_array)
        {
        super.init (light_array);
        lights = my_light_array.getAllLights();

      // Clean out everything
      //
        for (Sprite sprite : my_sprites)  sprite.clear();
        my_sprites.clear();
        for (Sprite sprite : dead_sprites)  sprite.clear();
        dead_sprites.clear();
        for (Object obj : new_sprites)
            if (obj instanceof Sprite)  ((Sprite) obj).clear();
        new_sprites.clear();
        dead_snake_count = 0;
        t_new = 0;

        exploding = false;
        start_explosion = false;
        burst_count = 0;

        my_sprites.add (new Snake (Color.GREEN));
        my_sprites.add (new Snake (new Color(0xFFAA22))); // Redish orange
        for (int i=0; i<N_APPLES; i++)
            my_sprites.add (new AppleSprite(pick_random_light()));
        }

  // ----- clear_lights() ----------------------------------------------
  //
    private void clear_lights()
        {
        my_light_array.reset();
        for (Sprite sprite : my_sprites)
          { if (sprite instanceof Snake)
                ((Snake) sprite).update_color();
            }
        }

  // ----- find_adjacent_lights() --------------------------------------
  //
  // Find lights adjacent to a given position that are (1) inside the
  // grid of lights, (2) not already part of the snake, (3) not an
  // adjacent corner or diagonal.
  //
    private void find_adjacent_lights (int ix, int iy, int iz,
                                        Sprite self)
        {
        findAdjacentLights (ix, iy, iz, lights, self, adjacent_lights);
        }

  // ----- pick_random_light() -------------------------------------------
  //
  // Randomly pick a light that is not part of any other sprite.  
  // A light that is not part of a sprite has its color set to BLACK.
  //
    private Light pick_random_light()
        {
        return pickRandomLight (lights);
        }

  // ----- step() ------------------------------------------------------
  //
    @Override
    public boolean step (int time)
        {
      // Step each sprite and check that it is alive after its step.
      // When an AppleSprite is no longer alive (i.e., it's been eaten
      // by a snake, create a new one.  Snakes can die if they no longer
      // can find a valid move.  For example, the head gets trapped
      // in a corner.  It this case, create a new snake.  A snake
      // may also become the victim of another snake.  When this
      // happens wait until the victim has been ingested and its
      // consumptor has finished its subsequent growth spurt before
      // creating a replacement snake.
      //
      // When a snake is being consumed, its animation is the
      // responsibility of the ingesting snake.
      //
      // The dead_sprite and new_sprite lists are used to avoid
      // ArrayList comodifications in the main animation loop.
      //
        Snake dead_snake = null;
        
        for (Sprite sprite : my_sprites)
          { boolean alive = sprite.step (time);
            if (!alive)
                {
                dead_sprites.add (sprite);
                if (sprite instanceof Snake)
                  { dead_snake_count++;
                    dead_snake = (Snake) sprite;
                    dead_snake_color = dead_snake.getColor();
                    if (dead_snake.died())
                      { dead_snake.explode (new_sprites, lights);
                        exploding = true;
                        start_explosion = true;
                        }
                    new_sprites.add (dead_snake_color);
                    t_new = 0;
                    }
                  else if (sprite instanceof AppleSprite)
                  { new_sprites.add (new AppleSprite(pick_random_light()));
                    }
                  else if (sprite instanceof StarBurstSprite)
                  { --burst_count;
                    exploding = burst_count > 0;
                    clear_lights();
                    }
                }
            }

      // Always remove dead sprites from the list that we're animating.
      //
        for (Sprite dead_sprite : dead_sprites)
            my_sprites.remove (dead_sprite);
        dead_sprites.clear();

      // If both snakes have died, it's time to reset.
      //
        if (dead_snake_count >= 2 && !exploding)
          { dead_snake_count = 0;
            if (dead_snake != null)
              { dead_snake.explode (new_sprites, lights);
                exploding = true;
                start_explosion = true;
                }
              else
              { init (my_light_array);
                return true;
                }
            }

      // Only add newly created sprites when no snake is eating.
      //
        if (start_explosion)
          { start_explosion = false;
            for (Object obj : new_sprites)
              { if (obj instanceof StarBurstSprite)
                  { burst_count++;
                    Sprite star = (StarBurstSprite) obj;
                    my_sprites.add (star);
                    dead_sprites.add (star);    // borrow use of dead_sprites
                    }
                }
            for (Sprite dead_sprite : dead_sprites) // remove StarBurstSprites
                new_sprites.remove (dead_sprite);   // and keep everything else
            dead_sprites.clear();
            }
        else if (!exploding)
          { boolean snake_is_eating = false;
            for (Sprite sprite : my_sprites)
              { if (sprite instanceof Snake)
                  { snake_is_eating = ((Snake) sprite).isEating();
                    if (snake_is_eating)
                      { t_new = 0;
                        break;
                        }
                    }
                }
            if (!snake_is_eating)
              { if (t_new == 0)
                    t_new = time + new_delay;
                if (time > t_new)
                  { for (Object obj : new_sprites)
                      { if (obj instanceof Sprite)
                            my_sprites.add ((Sprite) obj);
                          else if (obj instanceof Color)
                          { my_sprites.add (new Snake ((Color) obj));
                             --dead_snake_count;
                            }
                        }
                    new_sprites.clear();
                    }
                }
            }
        
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
