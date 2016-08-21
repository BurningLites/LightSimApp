//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- GameOfLifeController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class GameOfLifeController
//======================================================================

public class GameOfLifeController extends LightController
    {
    Light[][]  left_lights, right_lights;
    byte[][][] left_data, right_data;
    int nx, ny;
    int left_wait = 0, right_wait = 0;
    
    public String name()    { return "Game of Life"; }
    Color[] colors = { Color.GREEN, Color.CYAN,
                        new Color(125,125,255), new Color(255,125,255),
                        Color.WHITE };
    
    byte[] spaceship = 
      { 0b10100,
        0b00010,
        0b00010,
        0b10010,
        0b01110
        };

    int t_next;
    static final int MY_DT = 250;

  // ----- init() -----------------------------------------------------
  //
    public void init (LightArray light_array)
        {
        super.init (light_array);
        
        nx = 5;
        ny = 50;
        left_lights = new Light[nx][ny];
        right_lights = new Light[nx][ny];
        
        for (Light l : my_light_array.getLights())
            {
            if (l.ix < 5)
              { left_lights[l.ix][l.iy + 10*(4-l.iz)] = l;
                l.color = colors[l.iz];
                }
              else
              { right_lights[16-l.ix][l.iy + 10*l.iz] = l;
                l.color = colors[4-l.iz];
                }
            }
        
        left_data = new byte[2][nx][ny];
        right_data = new byte[2][nx][ny];

        setLights (0);
        t_next = 0;
        }
    
  // ----- step() -----------------------------------------------------
  //
    @Override
    public boolean step (int clock)
        {
        if (clock >= t_next)
            {
            if (my_step > 0)
                {
                int n = my_step % 2;
                int nm1 = (my_step-1) % 2;

                left_wait = insert_spaceship (left_data[nm1], left_wait);
                right_wait = insert_spaceship (right_data[nm1],right_wait);

                calc_next_gen (left_data[nm1], left_data[n]);
                calc_next_gen (right_data[nm1], right_data[n]);
                }

            increment_step();
            t_next = clock + MY_DT;
            }
        return true;
        }

  // ----- calc_next_gen() --------------------------------------------
  //
    private void calc_next_gen (byte[][] data, byte[][] data_tp1)
        {
        int n_neighbors;
        int nxm1 = nx-1, nxm2 = nx-2;
        int nym1 = ny-1, nym2 = ny-2;

      // Calculate corners
      //
        n_neighbors = data[0][1] + data[1][1] + data[1][0];
        data_tp1[0][0] = next_gen (n_neighbors, data[0][0]);

        n_neighbors = data[nxm2][0] + data[nxm2][1] + data[nxm1][1];
        data_tp1[nxm1][0] = next_gen (n_neighbors, data[nxm1][0]);

        n_neighbors = data[0][nym2] + data[1][nym2] + data[1][nym1];
        data_tp1[0][nym1] = next_gen (n_neighbors, data[0][nym1]);

        n_neighbors = data[nxm2][nym1] + data[nxm2][nym2] + data[nxm1][nym2];
        data_tp1[nxm1][nym1] = next_gen (n_neighbors, data[nxm1][nym1]);

      // Calculate bottom and top
      //
        for (int ix=1; ix<nxm1; ix++)
            {
            n_neighbors = data[ix-1][0] + data[ix-1][1] + data[ix][1]
                            + data[ix+1][0] + data[ix+1][1];
            data_tp1[ix][0] = next_gen (n_neighbors, data[ix][0]);

            n_neighbors = data[ix-1][nym1] + data[ix-1][nym2]
                            + data[ix][nym2]
                            + data[ix+1][nym2] + data[ix+1][nym1];
            data_tp1[ix][nym1] = next_gen (n_neighbors, data[ix][nym1]);
            }

      // Calculate sides
      //
        for (int iy=1; iy<nym1; iy++)
            {
            n_neighbors = data[0][iy-1] + data[0][iy+1]
                            + data[1][iy-1] + data[1][iy] + data[1][iy+1];
            data_tp1[0][iy] = next_gen (n_neighbors, data[0][iy]);

            n_neighbors = data[nxm2][iy-1] + data[nxm2][iy]
                            + data[nxm2][iy+1]
                            + data[nxm1][iy-1] + data[nxm1][iy+1];
            data_tp1[nxm1][iy] = next_gen (n_neighbors, data[nxm1][iy]);
            }

      // Calculate main space
      //
        for (int ix=1; ix<nxm1; ix++)
            {
            for (int iy=1; iy<nym1; iy++)
                {
                n_neighbors
                    = data[ix-1][iy-1] + data[ix-1][iy] + data[ix-1][iy+1]
                      + data[ix][iy-1] + data[ix][iy+1]
                      + data[ix+1][iy-1] + data[ix+1][iy] + data[ix+1][iy+1];
                data_tp1[ix][iy] = next_gen (n_neighbors, data[ix][iy]);
                }
            }
        }

    private byte next_gen (int n_neighbors, byte alive)
        {
        if (alive == 1)
            {
            if (n_neighbors == 2 || n_neighbors == 3)   return 1;
            }
        else if (n_neighbors == 3)
            {
            return 1;
            }
        return 0;
        }

    private int insert_spaceship (byte[][] game_cells, int wait)
        {
        if (wait > 0)
            {
            return --wait;
            }
          else if (Math.random() > 0.10)
            {
            return wait;
            }
          else
            {
            int nrows = spaceship.length;
            for (int iy=0; iy<nrows; iy++)
              { int mask = 0b10000;
                int ship_row = spaceship[iy];
                for (int ix=0; ix<nx; ix++)
                  { game_cells[ix][iy] =
                            (byte) (((mask & ship_row) != 0) ? 1 : 0);
                    mask = mask >>> 1;
                    }
                }
            return 27;
            }
        }
    
  // ----- setLights() ------------------------------------------------
  //
    @Override
    public void setLights (int time)
        {
        int data_idx = my_step % 2;
        set_lights (left_data[data_idx], right_data[data_idx]);
        }

    private void set_lights (byte[][] left, byte[][] right)
        {
        for (int ix=0; ix<nx; ix++)
            {
            for (int iy=0; iy<ny; iy++)
                {
                left_lights[ix][iy].on = left[ix][iy] != 0;
                right_lights[ix][iy].on = right[ix][iy] != 0;
                }
            }
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
