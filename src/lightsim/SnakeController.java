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

public class SnakeController extends LightController
    {
  //-------------------------------------------------------------------
  // inner class Sprite
  //
    protected class Sprite
        {
        protected boolean alive = true;

        public boolean hasLight (Light l)       { return false; }
        public void setAlive (boolean value)    { alive = value; }
        public boolean step (int clock)         { return alive; }
        }
  //
  //-------------------------------------------------------------------

  //-------------------------------------------------------------------
  // inner class Snake
  //
    protected class Apple extends Sprite
        {
        private int t_next, cycle;
        private static final int MY_DT = 50,
                        BLINK_PERIOD = 5, BLINK_LENGTH = 1;
        Light my_light;

        public Apple()
            {
            my_light = pickRandomLight();
            my_light.setState (Color.RED, true);
            cycle = pick_number (0, BLINK_PERIOD-1);
            t_next = 0;
            }

        @Override
        public boolean hasLight (Light l)   { return l == my_light; }

        @Override
        public boolean step (int time)
            {
            if (alive && time > t_next)
              { t_next = time + MY_DT;
                cycle = (cycle + 1) % BLINK_PERIOD;
                my_light.on = cycle >= BLINK_LENGTH;
                }
            if (my_light.color != Color.RED)
                alive = false;
            return alive;
            }
        }

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
        private Color  color, darker_color;
        private int t_next;
        private int my_dt = 125;
        private int grow, eat_cycle;
        private boolean eating_snake;
        private Snake my_big_meal;
        private boolean died;
        
        public Snake (Color color)
            {
            this.color = color;
            darker_color = color.darker();
            body = new ArrayList<>();
            build_snake();
            t_next = 0;
            }

        public boolean died()   { return died; }
        public Color getColor() { return color; }
        public boolean isGrowing()  { return grow > 0; }
        public int getSize()    { return body.size(); }

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

              // Starting with a randomly chosen initial position, create
              // the snake one light at a time.
              //
                Light l = pickRandomLight();
                addLight (l);

              // Add three more points to the snake.  If we can't find a
              // string of six adjacent lights, try again.
              //
                int ix = l.ix, iy = l.iy, iz = l.iz;
                for (int n=0; n<5; n++)
                  { find_adjacent_lights (ix, iy, iz, this);
                    if (adjacent_lights.size() > 0)
                      { int pick = pick_number (0, adjacent_lights.size()-1);
                        Light next_light = adjacent_lights.get (pick);
                        ix = next_light.ix;
                        if (ix > 4)  ix = ix - 7;
                        iy = next_light.iy;
                        iz = next_light.iz;
                        addLight (next_light);
                        }
                      else
                      { continue build_snake;
                        }
                    }
                snake_is_complete = true;
                }
            while (!snake_is_complete);
            }

      // ----- clear() --------------------------
      //
        public void clear()             
            {
            for (Light l : body)
                l.setState (Color.BLACK, false);
            body.clear();
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

      // ----- eat_snake() --------------------
      //
        private void eat_snake()
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
                if (eat_cycle%2 == 0)  grow++;
                eat_cycle++;
                }
              else
              { eating_snake = false;
                }
            }
        
      // ----- eat_something() ------------------
      //
        private void eat_something (Sprite food)
            {
            food.setAlive (false);
            if (food instanceof Apple)
              { grow = 1;
                }
            else if (food instanceof Snake)
              { eating_snake = true;
                my_big_meal = (Snake) food;
                my_big_meal.update_color (my_big_meal.darker_color);
                eat_cycle = 0;
                }
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
              { int pick = pick_number (0, adjacent_lights.size()-1);
                Light next_light = adjacent_lights.get (pick);
                if (next_light.getColor() != Color.BLACK)
                    {
                    Sprite food = findSprite (next_light);
                    if (food != null)
                        eat_something (food);
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
            if (grow <= 0)
              { Light tail = body.get(0);
                removeLight (tail);
                }
              else
              { grow--;
                my_dt += 10;     // As snake grows, it moves slower.
                }
            
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
        private void update_color (Color color)
            {
            int nm1 = body.size() - 1;   // Avoid head.
            for (int i=0; i<nm1; i++)
                body.get(i).setColor (color);
            }
        }
  //
  //-------------------------------------------------------------------
    
    Light[][][] lights;
    ArrayList<Light>  adjacent_lights;
    ArrayList<Sprite>  my_sprites, dead_sprites, new_sprites;
    int  nx, ny, nz, nxm1, nym1, nzm1;

    private static final int N_APPLES = 10;
    private Snake snake_victim;
    private Color dead_snake_color;
    
  // ----- constructor -------------------------------------------------
  //
    public SnakeController()
        {
        adjacent_lights = new ArrayList<>();
        my_sprites = new ArrayList<>();
        dead_sprites = new ArrayList<>();
        new_sprites = new ArrayList<>();
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
        nx = lights.length;
        ny = lights[0].length;
        nz = lights[0][0].length;
        nxm1 = nx - 1;
        nym1 = ny - 1;
        nzm1 = nz - 1;
        
        my_light_array.fill (Color.BLACK, false);
        my_sprites.add (new Snake (Color.GREEN));
        my_sprites.add (new Snake (Color.ORANGE));
        for (int i=0; i<N_APPLES; i++)
            my_sprites.add (new Apple());
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

    private void find_adjacent_lights (int ix, int iy, int iz,
                                        Sprite self)
        {
        adjacent_lights.clear();
        
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
                    Light adjacent_light = lights[x][y][z];
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
    public Sprite findSprite (Light light)
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
    public Light pickRandomLight()
        {
        boolean found_light = false;
        Light light = null;
        while (!found_light)
          { int ix = pick_number (0, nxm1);
            int iy = pick_number (0, nym1);
            int iz = pick_number (0, nzm1);
            light = lights[ix][iy][iz];
            found_light = light.color == Color.BLACK;
            }
        return light;
        }
    
  // ----- step() ------------------------------------------------------
  //
    @Override
    public boolean step (int time)
        {
      // Step each sprite and check that it is alive after its step.
      // When an Apple is no longer alive (i.e., it's been eaten by
      // a snake, create a new one.  Snakes can die if they no longer
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
        for (Sprite sprite : my_sprites)
          { boolean alive = sprite.step (time);
            if (!alive)
              { if (sprite instanceof Snake)
                  { Snake dead_snake = (Snake) sprite;
                    dead_snake_color = dead_snake.getColor();
                    if (dead_snake.died())
                      { dead_snake.clear();
                        new_sprites.add (new Snake (dead_snake_color));
                        }
                      else
                        snake_victim = dead_snake;
                    }
                  else if (sprite instanceof Apple)
                  { new_sprites.add (new Apple());
                    }
                dead_sprites.add (sprite);
                }
            }

        if (snake_victim != null && snake_victim.getSize() == 0)
          { boolean snake_is_growing = false;
            for (Sprite sprite : my_sprites)
              { if (sprite instanceof Snake)
                    snake_is_growing 
                        = snake_is_growing || ((Snake) sprite).isGrowing();
                }
            if (!snake_is_growing)
              { new_sprites.add (new Snake (dead_snake_color));
                snake_victim = null;
                }
            }

        for (Sprite dead_sprite : dead_sprites)
            my_sprites.remove (dead_sprite);
        dead_sprites.clear();
        for (Sprite new_sprite : new_sprites)
            my_sprites.add (new_sprite);
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
