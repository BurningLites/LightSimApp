//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 24, 2016
//
//*************************************************************************

//----------------------------------------- Sprite.java -----

package lightsim;

import lightsim.LightArray.Light;

//======================================================================
// class Sprite
//======================================================================

public class Sprite
    {
    protected boolean alive = true;

    public void clear()                     { }
    public boolean hasLight (Light l)       { return false; }
    public void setAlive (boolean value)    { alive = value; }
    public boolean step (int clock)         { return alive; }

    }


//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
