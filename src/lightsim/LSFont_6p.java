//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 18, 2016
//
//*************************************************************************

//----------------------------------------- LSFont_6p.java -----

package lightsim;


//======================================================================
// class LSFont_6p
//======================================================================

public class LSFont_6p extends LSFont
    {

  // The raster bytes for each character are prefixed by
  // its width.
  //
  // Characters are described column-by-column in
  // the low order bits of the bytes.  Bit 6 is the
  // bottom of the character and bit 0 is the top.
  // The columns are given from left to right.
  //
    static final byte[][]  CHAR_RASTERS = {
	/*   */  { 0x00, 0x00, 0x00, 0x00 },
    /* ! */  { 0x17, 0x17 },
    /* " */  { 0x03, 0x03 },
    /* # */  { 0x0C, 0x1E, 0x0C, 0x1E, 0x0C },
    /* $ */  { 0x0A, 0x1F, 0x0A },
    /* % */  { 0x13, 0x0B, 0x04, 0x1A, 0x19 },
    /* & */  { 0x10, 0x2A, 0x25, 0x2A, 0x10, 0x28 },
    /* ' */  { 0x03 },
    /* ( */  { 0x0E, 0x11 },
    /* ) */  { 0x11, 0x0E },
    /* * */  { 0x02, 0x07, 0x02 },
    /* + */  { 0x04, 0x0E, 0x04 },
    /* , */  { 0x18, 0x38 },
    /* - */  { 0x04, 0x04, 0x04 },
    /* . */  { 0x18, 0x18 },
    /* / */  { 0x10, 0x08, 0x04, 0x02, 0x01 },
    /* 0 */  { 0x0E, 0x15, 0x13, 0x0E },
    /* 1 */  { 0x12, 0x1F, 0x10, 0x00 },
    /* 2 */  { 0x12, 0x19, 0x15, 0x12 },
    /* 3 */  { 0x0A, 0x11, 0x15, 0x0A },
    /* 4 */  { 0x08, 0x0C, 0x0A, 0x1F },
    /* 5 */  { 0x17, 0x15, 0x15, 0x09 },
	/* 6 */  { 0x0E, 0x15, 0x15, 0x08 },
    /* 7 */  { 0x01, 0x19, 0x05, 0x03 },
    /* 8 */  { 0x0A, 0x15, 0x15, 0x0A },
    /* 9 */  { 0x02, 0x15, 0x15, 0x0E },
    /* : */  { 0x14, 0x14 },
    /* ; */  { 0x14, 0x34 },
    /* < */  { 0x04, 0x0A, 0x11 },
    /* = */  { 0x0A, 0x0A, 0x0A },
    /* > */  { 0x11, 0x0A, 0x04 },
    /* ? */  { 0x02, 0x01, 0x2D, 0x02},
    /* @ */  { 0x0E, 0x11, 0x1D, 0x15, 0x0E },
    /* A */  { 0x1E, 0x05, 0x05, 0x1E },
    /* B */  { 0x1F, 0x15, 0x15, 0x0A },
    /* C */  { 0x0E, 0x11, 0x11, 0x0A },
    /* D */  { 0x1F, 0x11, 0x11, 0x0E },
    /* E */  { 0x1F, 0x15, 0x15, 0x11 },
    /* F */  { 0x1F, 0x05, 0x05, 0x01 },
    /* G */  { 0x0E, 0x11, 0x19, 0x0A },
    /* H */  { 0x1F, 0x04, 0x04, 0x1F },
    /* I */  { 0x11, 0x1F, 0x11 },
    /* J */  { 0x08, 0x10, 0x10, 0x0F },
    /* K */  { 0x1F, 0x04, 0x0A, 0x11 },
    /* L */  { 0x1F, 0x10, 0x10, 0x10 },
    /* M */  { 0x1F, 0x02, 0x04, 0x02, 0x1F },
    /* N */  { 0x1F, 0x02, 0x04, 0x1F },
    /* O */  { 0x0E, 0x11, 0x11, 0x0E },
    /* P */  { 0x1F, 0x05, 0x05, 0x02 },
    /* Q */  { 0x0E, 0x11, 0x19, 0x1E },
    /* R */  { 0x1F, 0x05, 0x0D, 0x12 },
    /* S */  { 0x12, 0x15, 0x15, 0x09 },
    /* T */  { 0x01, 0x01, 0x1F, 0x01, 0x01 },
    /* U */  { 0x1F, 0x10, 0x1F },
    /* V */  { 0x0F, 0x10, 0x0F },
    /* W */  { 0x0F, 0x10, 0x08, 0x10, 0x0F },
    /* X */  { 0x1B, 0x04, 0x1B },
    /* Y */  { 0x03, 0x1C, 0x03 },
    /* Z */  { 0x11, 0x19, 0x15, 0x13, 0x11 },
    /* [ */  { 0x1F, 0x11 },
    /* \ */  { 0x01, 0x02, 0x04, 0x08, 0x10 },
    /* ] */  { 0x11, 0x1F },
    /* ^ */  { 0x02, 0x01, 0x02 },
    /* _ */  { 0x10, 0x10, 0x10, 0x10 },
    /* ` */  { 0x01, 0x02 },
    /* a */  { 0x1A, 0x1A, 0x1C },
    /* b */  { 0x1F, 0x14, 0x14, 0x08 },
    /* c */  { 0x0C, 0x12, 0x12, 0x04 },
    /* d */  { 0x08, 0x14, 0x14, 0x1F },
    /* e */  { 0x0C, 0x16, 0x16, 0x04 },
    /* f */  { 0x04, 0x1E, 0x05, 0x01 },
    /* g */  { 0x04, 0x2A, 0x2A, 0x1E },
    /* h */  { 0x1F, 0x04, 0x04, 0x18 },
    /* i */  { 0x1D },
    /* j */  { 0x08, 0x10, 0x0D },
    /* k */  { 0x1F, 0x08, 0x14 },
    /* l */  { 0x1F },
    /* m */  { 0x1C, 0x04, 0x18, 0x04, 0x18 },
    /* n */  { 0x1C, 0x04, 0x18 },
    /* o */  { 0x08, 0x14, 0x14, 0x08 },
    /* p */  { 0x3C, 0x14, 0x14, 0x08 },
    /* q */  { 0x08, 0x14, 0x14, 0x3C },
    /* r */  { 0x1E, 0x04, 0x02, 0x04 },
    /* s */  { 0x14, 0x16, 0x16, 0x0A },
    /* t */  { 0x04, 0x1E, 0x14, 0x04 },
    /* u */  { 0x1C, 0x10, 0x1C },
    /* v */  { 0x0C, 0x10, 0x0C },
    /* w */  { 0x0C, 0x10, 0x08, 0x10, 0x0C },
    /* x */  { 0x14, 0x08, 0x14 },
    /* y */  { 0x2C, 0x10, 0x0C },
    /* z */  { 0x14, 0x1C, 0x14, 0x10 },
    /* { */  { 0x04, 0x1F, 0x11 },
    /* | */  { 0x3F },
    /* } */  { 0x11, 0x1F, 0x04 },
    /* ->*/  { 0x04, 0x15, 0x0E, 0x04 },
    /* <-*/  { 0x04, 0x0E, 0x15, 0x04 }
        };

  // ----- getCharWidth() ---------------------------------------------
  //
    public int getCharHeight()          { return 6; }
    public int getCharMaxWidth()        { return 6; }
    public int getCharWidth (char c)    { return CHAR_RASTERS[c-' '].length; }
    public byte[] getCharRaster (char c) { return CHAR_RASTERS[c-' ']; }
    public boolean isFixedWidth()       { return false; }
    
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
