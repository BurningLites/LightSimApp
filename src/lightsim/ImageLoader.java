//*************************************************************************
//
// Copyright (c) 2016 Ken Bongort. All rights reserved.
//
//      Author: Ken Bongort
//      Project: LightSim
//     Created: Aug 16, 2016
//
//*************************************************************************

//----------------------------------------- ImageLoader.java -----

package lightsim;

import java.net.URL;
import javax.swing.ImageIcon;

//======================================================================
// class ImageLoader
//======================================================================

public class ImageLoader
    {
  // ----- loadIcon() --------------------------------------------
  //
    public static ImageIcon loadIcon (String image_url_spec)
        {
        ImageIcon image_icon = null;

      // Turn the URL spec into a URL instance, and load the image.
      //
        URL url = ClassLoader.getSystemResource (image_url_spec);
        if (url != null)
            image_icon = new ImageIcon (url);

      // If the image did not load properly, issue a message and
      // return something that can serve was a placeholder so that the
      // application doesn't crash.
      //
        if (image_icon == null)
          { System.out.println ("Image load failed.  URL: " + image_url_spec);
            image_icon = new ImageIcon();
            }

        return image_icon;
        }
    }

//*************************************************************************
//
//       Use or disclosure of the information contained herein is
//      subject to the restrictions provided in this file's header.
//
//*************************************************************************
