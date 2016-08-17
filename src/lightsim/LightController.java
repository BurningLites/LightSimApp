//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- LightController.java -----

package lightsim;

import java.awt.Color;
import lightsim.LightArray.Light;

//======================================================================
// abstract class LightController
//======================================================================

/**
 * The light controllers support these methods:
 *
 *      init (LightArray light_array)
 *          Perform any startup tasks.
 *          Access is provided to the light array so that the controller
 *          has the option of storing computed values directly into
 *          the lights, and can get meta information about the structure
 *          of the light array.
 *      String name()
 *          Returns the name of the plug-in.
 *      boolean step (int clock)
 *      Returns true if the plug-in wants to continue running.
 *      setLight (LightArray.Light light)
 *
 * LightController is an abstract class that defines trivial, default
 * implementations for several of these methods.  It also contains
 * useful methods and data for setting light values.
 *
 * Controllers can use the x,y,z information contained in Light instances
 * to
 *     - Determine the current on/off status and color of the Light.
 *     - Arrange lights into arrays that facilitate manipulating their
 *       values for particular display patterns.
 */
public abstract class LightController
    {
    
// standard ascii 5x7 font
// defines ascii characters 0x20-0x7F (32-127)
//
// Characters are described column-by-column in
// the low order bits of the bytes.  Bit 6 is the
// bottom of the character and bit 0 is the top.
// The columns are given from left to right.
// 
static final byte[] FONTMAP_5x7 = {
    0x00, 0x00, 0x00, 0x00, 0x00,// (space)
    0x00, 0x00, 0x5F, 0x5F, 0x00,// !
    0x00, 0x07, 0x00, 0x07, 0x00,// "
    0x14, 0x7F, 0x14, 0x7F, 0x14,// #
    0x24, 0x2A, 0x7F, 0x2A, 0x12,// $
    0x23, 0x13, 0x08, 0x64, 0x62,// %
    0x36, 0x49, 0x55, 0x22, 0x50,// &
    0x00, 0x05, 0x03, 0x00, 0x00,// '
    0x00, 0x1C, 0x22, 0x41, 0x00,// (
    0x00, 0x41, 0x22, 0x1C, 0x00,// )
    0x08, 0x2A, 0x1C, 0x2A, 0x08,// *
    0x08, 0x08, 0x3E, 0x08, 0x08,// +
    0x00, 0x50, 0x30, 0x00, 0x00,// ,
    0x08, 0x08, 0x08, 0x08, 0x08,// -
    0x00, 0x60, 0x60, 0x00, 0x00,// .
    0x20, 0x10, 0x08, 0x04, 0x02,// /
    0x3E, 0x51, 0x49, 0x45, 0x3E,// 0
    0x00, 0x42, 0x7F, 0x40, 0x00,// 1
    0x42, 0x61, 0x51, 0x49, 0x46,// 2
    0x21, 0x41, 0x45, 0x4B, 0x31,// 3
    0x18, 0x14, 0x12, 0x7F, 0x10,// 4
    0x27, 0x45, 0x45, 0x45, 0x39,// 5
    0x3C, 0x4A, 0x49, 0x49, 0x30,// 6
    0x01, 0x71, 0x09, 0x05, 0x03,// 7
    0x36, 0x49, 0x49, 0x49, 0x36,// 8
    0x06, 0x49, 0x49, 0x29, 0x1E,// 9
    0x00, 0x36, 0x36, 0x00, 0x00,// :
    0x00, 0x56, 0x36, 0x00, 0x00,// ;
    0x00, 0x08, 0x14, 0x22, 0x41,// <
    0x14, 0x14, 0x14, 0x14, 0x14,// =
    0x41, 0x22, 0x14, 0x08, 0x00,// >
    0x02, 0x01, 0x51, 0x09, 0x06,// ?
    0x32, 0x49, 0x79, 0x41, 0x3E,// @
    0x7E, 0x11, 0x11, 0x11, 0x7E,// A
    0x7F, 0x49, 0x49, 0x49, 0x36,// B
    0x3E, 0x41, 0x41, 0x41, 0x22,// C
    0x7F, 0x41, 0x41, 0x22, 0x1C,// D
    0x7F, 0x49, 0x49, 0x49, 0x41,// E
    0x7F, 0x09, 0x09, 0x01, 0x01,// F
    0x3E, 0x41, 0x41, 0x51, 0x32,// G
    0x7F, 0x08, 0x08, 0x08, 0x7F,// H
    0x00, 0x41, 0x7F, 0x41, 0x00,// I
    0x20, 0x40, 0x41, 0x3F, 0x01,// J
    0x7F, 0x08, 0x14, 0x22, 0x41,// K
    0x7F, 0x40, 0x40, 0x40, 0x40,// L
    0x7F, 0x02, 0x04, 0x02, 0x7F,// M
    0x7F, 0x04, 0x08, 0x10, 0x7F,// N
    0x3E, 0x41, 0x41, 0x41, 0x3E,// O
    0x7F, 0x09, 0x09, 0x09, 0x06,// P
    0x3E, 0x41, 0x51, 0x21, 0x5E,// Q
    0x7F, 0x09, 0x19, 0x29, 0x46,// R
    0x46, 0x49, 0x49, 0x49, 0x31,// S
    0x01, 0x01, 0x7F, 0x01, 0x01,// T
    0x3F, 0x40, 0x40, 0x40, 0x3F,// U
    0x1F, 0x20, 0x40, 0x20, 0x1F,// V
    0x7F, 0x20, 0x18, 0x20, 0x7F,// W
    0x63, 0x14, 0x08, 0x14, 0x63,// X
    0x03, 0x04, 0x78, 0x04, 0x03,// Y
    0x61, 0x51, 0x49, 0x45, 0x43,// Z
    0x00, 0x00, 0x7F, 0x41, 0x41,// [
    0x02, 0x04, 0x08, 0x10, 0x20,// "\"
    0x41, 0x41, 0x7F, 0x00, 0x00,// ]
    0x04, 0x02, 0x01, 0x02, 0x04,// ^
    0x40, 0x40, 0x40, 0x40, 0x40,// _
    0x00, 0x01, 0x02, 0x04, 0x00,// `
    0x20, 0x54, 0x54, 0x54, 0x78,// a
    0x7F, 0x48, 0x44, 0x44, 0x38,// b
    0x38, 0x44, 0x44, 0x44, 0x20,// c
    0x38, 0x44, 0x44, 0x48, 0x7F,// d
    0x38, 0x54, 0x54, 0x54, 0x18,// e
    0x08, 0x7E, 0x09, 0x01, 0x02,// f
    0x08, 0x14, 0x54, 0x54, 0x3C,// g
    0x7F, 0x08, 0x04, 0x04, 0x78,// h
    0x00, 0x44, 0x7D, 0x40, 0x00,// i
    0x20, 0x40, 0x44, 0x3D, 0x00,// j
    0x00, 0x7F, 0x10, 0x28, 0x44,// k
    0x00, 0x01, 0x3E, 0x40, 0x00,// l
    0x7C, 0x04, 0x18, 0x04, 0x78,// m
    0x7C, 0x08, 0x04, 0x04, 0x78,// n
    0x38, 0x44, 0x44, 0x44, 0x38,// o
    0x7C, 0x14, 0x14, 0x14, 0x08,// p
    0x08, 0x14, 0x14, 0x18, 0x7C,// q
    0x7C, 0x08, 0x04, 0x04, 0x08,// r
    0x48, 0x54, 0x54, 0x54, 0x20,// s
    0x04, 0x3F, 0x44, 0x40, 0x20,// t
    0x3C, 0x40, 0x40, 0x20, 0x7C,// u
    0x1C, 0x20, 0x40, 0x20, 0x1C,// v
    0x3C, 0x40, 0x30, 0x40, 0x3C,// w
    0x44, 0x28, 0x10, 0x28, 0x44,// x
    0x0C, 0x50, 0x50, 0x50, 0x3C,// y
    0x44, 0x64, 0x54, 0x4C, 0x44,// z
    0x00, 0x08, 0x36, 0x41, 0x00,// {
    0x00, 0x00, 0x7F, 0x00, 0x00,// |
    0x00, 0x41, 0x36, 0x08, 0x00,// }
    0x08, 0x08, 0x2A, 0x1C, 0x08,// ->
    0x08, 0x1C, 0x2A, 0x08, 0x08 // <-
};

    protected LightArray  my_light_array;

  // ----- LightController API methods --------------------------------
  //
    public void init (LightArray light_array)
        {
        my_light_array = light_array;
        }
    abstract public String name();
    abstract public boolean step (int time, int step);

    public void setLights (int time, int step)  {}

  // ----- toString() -------------------------------------------------
  //
    @Override
    public String toString()    { return name(); }

  // ----- clear_layer() -------------------------------------------
  //
    protected void clear_layer (Light layer[][])
        {
        int nx = layer.length;
        int nz = layer[0].length;

        for (int ix=0; ix<nx; ix++)
            for (int iz=0; iz<nz; iz++)
                {
                Light l = layer[ix][iz];
                l.on = false;
                l.color = Color.LIGHT_GRAY;
                }
        }

  // ----- copy_layer() -------------------------------------------
  //
    protected void copy_layer (Light from[][], Light to[][])
        {
        int nx = from.length;
        int ny = from[0].length;

        for (int ix=0; ix<nx; ix++)
            for (int iy=0; iy<ny; iy++)
                {
                Light from_l = from[ix][iy];
                Light to_l = to[ix][iy];
                to_l.on = from_l.on;
                to_l.color = from_l.color;
                }
        }

  // ----- mirror_y_layer() -------------------------------------------
  //
    protected void mirror_y_layer (Light layer[][], byte pattern[],
                                    Color color)
        {
        for (int ix=0; ix<5; ix++)
            {
            int mask = 0b1;
            int row_pattern = pattern[ix];
            for (int iz=0; iz<5; iz++)
                {
                Light l = layer[4-ix][4-iz];
                if ((mask & row_pattern) !=  0)
                    {
                    l.on = true;
                    l.color = color;
                    }
                  else
                    {
                    l.on = false;
                    l.color = Color.LIGHT_GRAY;
                    }
                mask = mask << 1;
                }
            }
        }

  // ----- set_5x7_char() -------------------------------------------
  //
    protected void set_5x7_char (char c, Light lights[][], 
                                int x_offset, int y_offset, 
                                Color background, Color foreground)
        {
      // Check that everything will fit.
      //
        int nx = lights.length;
        int ny = lights[0].length;
        if ( !(    (0 <= x_offset && x_offset+5 <=nx)
                && (0 <= y_offset && y_offset+7 <= ny)
                && (' ' <= c && c < 128)
                )
            )  return;
        
        int ic = 5 * (c - ' ');
        for (int ix=x_offset; ix<x_offset+5; ix++)
            {
            int mask = 0b01000000;
            int column = FONTMAP_5x7[ic++];
            for (int iy=y_offset; iy<y_offset+7; iy++)
                {
                Light light = lights[ix][iy];
                if ((mask & column) !=0 )
                  { light.on = true;
                    light.color = foreground;
                    }
                  else
                  { light.on = false;
                    light.color = background;
                    }
                mask = mask >>> 1;
                }
            }
        }
    
  // ----- set_5x7_char_row() ---------------------------------------
  //
  // Set just one row from a 5x7 raster character.  This supports
  // adding a character row-by-row to a scrolling display.
  //
    protected void set_5x7_char_row (char c, int row, 
                        Light lights[][], int x_offset, int y_offset,
                        Color background, Color foreground)
        {
      // Check that everything will fit.
      //
        int nx = lights.length;
        int ny = lights[0].length;
        if ( !(    (0 <= x_offset && x_offset+5 <=nx)
                && (0 <= y_offset && y_offset+7 <= ny)
                && (0 <= row && row < 7)
                && (' ' <= c && c < 128)
                )
            )  return;
        
        int ic = 5 * (c - ' ');
        int mask = 0b01000000 >>> row;
        for (int ix=x_offset; ix<x_offset+5; ix++)
            {
            int column = FONTMAP_5x7[ic++];
            Light light = lights[ix][y_offset];
            if ((mask & column) !=0 )
              { light.on = true;
                light.color = foreground;
                }
              else
              { light.on = background != Color.BLACK;
                light.color = background;
                }
            }
        }
            
  // ----- set_y_layer() -------------------------------------------
  //
    protected void set_y_layer (Light layer[][], byte pattern[],
                                Color color)
        {
        for (int ix=0; ix<5; ix++)
            {
            int mask = 0b10000;
            int row_pattern = pattern[ix];
            for (int iz=0; iz<5; iz++)
                {
                Light l = layer[ix][iz];
                if ((mask & row_pattern) !=  0)
                    {
                    l.on = true;
                    l.color = color;
                    }
                  else
                    {
                    l.on = false;
                    l.color = Color.LIGHT_GRAY;
                    }
                mask = mask >>> 1;
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
