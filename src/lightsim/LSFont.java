//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 17, 2016
//
//*************************************************************************

//----------------------------------------- LSFont.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// class LSFont
//======================================================================

public abstract class LSFont
    {
    abstract public int getCharHeight();
    abstract public int getCharMaxWidth();
    abstract public byte[] getCharRaster (char c);
    abstract public int getCharWidth (char c);
    abstract public boolean isFixedWidth();


  // ----- fillWithChar() -------------------------------------------
  //
    protected void fillWithChar (char c, Light lights[][],
                                int x_offset, int y_offset,
                                Color background, Color foreground)
        {
      // Check that everything will fit.
      //
        int nx = lights.length;
        int ny = lights[0].length;
        int cw = getCharWidth(c);
        int ch = getCharHeight();

        if ( !(    (0 <= x_offset && x_offset+cw <= nx)
                && (0 <= y_offset && y_offset+ch <= ny)
                && (' ' <= c && c < 128)
                )
            )  return;

      // Do the fill.
      //
        byte[] char_raster = getCharRaster (c);
        int ic = 0;
        for (int ix=x_offset; ix<x_offset+cw; ix++)
            {
            int mask = 1 << ch;
            int column = char_raster[ic++];
            for (int iy=y_offset; iy<y_offset+ch; iy++)
                {
                Light light = lights[ix][iy];
                if ((mask & column) !=0 )
                  {
                    light.color = foreground;
                    }
                  else
                  {
                    light.color = Color.BLACK;
                    }
                mask = mask >>> 1;
                }
            }
        }

  // ----- fillWithCharColumn() ---------------------------------------
  //
  // Columns are numbered left to right from 0;
  //
    public void fillWithCharColumn (char c, int column,
                        Light[] lights, int y_offset,
                        Color background, Color foreground)
    {
      // Check that everything will fit.
      //
        int ch = getCharHeight();
        int cw = getCharWidth(c);
        int ny = lights.length;
        if ( !(    (0 <= y_offset && y_offset+ch <= ny)
                && (0 <= column && column < cw)
                && (' ' <= c && c < 128)
                )
            )  return;

      // Do the fill.
      //
        byte[] char_raster = getCharRaster (c);
        int column_raster = char_raster[column];
        int mask = 1 << (ch-1);          // Start at char base
        for (int iy=y_offset; iy<y_offset+ch; iy++)
            {
            Light light = lights[iy];
            if ((mask & column_raster) !=0 )
              {
                light.color = foreground;
                }
              else
              {
                light.color = background;
                }
            mask = mask >>> 1;
            }
    }

  // ----- fillWithCharRow() ------------------------------------------
  //
  // Set just one row from from the raster character.  This supports
  // adding a character row-by-row to a scrolling display.
  //
    public void fillWithCharRow (char c, int row,
                        Light lights[][], int x_offset, int y_offset,
                        Color background, Color foreground)
        {
      // Check that everything will fit.
      //
        int ch = getCharHeight();
        int cw = getCharWidth(c);
        int nx = lights.length;
        int ny = lights[0].length;

        if ( !(    (0 <= x_offset && x_offset+cw <= nx)
                && (0 <= y_offset && y_offset <= ny)
                && (0 <= row && row < ch)
                && (' ' <= c && c < 128)
                )
            )  return;

      // Do the fill.
      //
        int mask = (1 << (ch-1)) >>> row;
        byte[] char_raster = getCharRaster(c);
        int ic = 0;
        for (int ix=x_offset; ix<x_offset+cw; ix++)
            {
            int column = char_raster[ic++];
            Light light = lights[ix][y_offset];
            if ((mask & column) !=0 )
              {
                light.color = foreground;
                }
              else
              {
                light.color = background;
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
