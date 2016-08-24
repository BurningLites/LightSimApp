//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//     Project: LightSim
//     Created: Aug 24, 2016
//
//*************************************************************************

//----------------------------------------- LSUtils.java -----

package lightsim;

//======================================================================
// class LSUtils
//======================================================================
//
// Static utility methods for LightSim.
//

public class LSUtils
    {
  // ----- pick_number() -------------------------------------------
  //
  // Randomly pick a number within the given range (inclusive).  The
  // probably of selecting a particular number is 1 / (uprbnd-lwrbnd+1).
  // Thus,
  //        pickNumber (0, 1)
  // returns 0 or 1, each with a probably of 0.5.
  //
    public static int pickNumber (int lwrbnd, int uprbnd)
        {
        double pick = (uprbnd - lwrbnd + 1) * Math.random();
        int ipick = (int) pick;
        return lwrbnd + ipick;
        }


    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
