package lightsim;

import java.awt.Color;

public class Colors {
    // yellow, magenta, cyan are in-between colors.
    static Color SIX_COLOR_PROG[] = {Color.red, Color.magenta, Color.blue, Color.cyan, Color.green, Color.yellow};
    
    public static Color interpolate(Color a, Color b, float t) {
        float[] aRGB = new float[3];
        float[] bRGB = new float[3];
        a.getColorComponents(aRGB);
        b.getColorComponents(bRGB);
        float red = aRGB[0] * (1 - t) + bRGB[0] * t;
        float green = aRGB[1] * (1 - t) + bRGB[1] * t;
        float blue = aRGB[2] * (1 - t) + bRGB[2] * t;
        return new Color(red, green, blue);
    }
    
    public static Color sixColorProg(double t) {
        double t6 = t * 6;
        int aIndex = ((int)t6) % 6;
        int bIndex = (aIndex + 1) % 6;
        Color a = SIX_COLOR_PROG[aIndex];
        Color b = SIX_COLOR_PROG[bIndex];
        
        return interpolate(a, b, (float)(t6 - aIndex));
    }
}
