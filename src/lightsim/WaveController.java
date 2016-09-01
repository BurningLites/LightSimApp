package lightsim;

import java.awt.Color;
import java.util.Random;
import lightsim.LightArray.Light;

public class WaveController extends LightController {

    static final double X_HALF_AMP_MIN = 0.5;
    static final double X_HALF_AMP_MAX = 1.2;
    static final double X_AMP_PERIOD = 20;  // Period of x amplitutude modulation.
    
    static final double X_LENGTH_MIN = 5;
    static final double X_LENGTH_MAX = 5;
    static final double X_LENGTH_PERIOD = 67 ;  // Period of x wavelength modulation.
    
    static final double X_WIDTH_MIN = 2;
    static final double X_WIDTH_MAX = 8;
    static final double X_WIDTH_PERIOD = 241;
    
    static final double Y_OFFSET_MIN = -8;
    static final double Y_OFFSET_MAX = 12;
    static final double Y_OFFSET_PERIOD = 437;
    
    static final double Y_OFFSET_PERIOD_MUL_MIN = 3;
    static final double Y_OFFSET_PERIOD_MUL_MAX = 0.1;
    static final double Y_OFFSET_PERIOD_MUL_PERIOD = 1239;
    
    static final double COLOR_PERIOD = 893;
    
    @Override
    public String name() {
        return "Wave";
    }
    
    @Override
    public void init(LightArray lightArray) {
        super.init(lightArray);
    }
    
    @Override
    public boolean step(double time) {
        
        clearLights();
        
        double theta = time * 3;  // Can add multiplier here.
        
        
        int width = 17;
        int depth = 5;
//        double xWaveMax[] = new double[width];
//        double xWaveMin[] = new double[width];
        double xCenter[] = new double[width];
        double zCenter[] = new double[depth];
        
        double xHalfAmp = modulateValue(X_HALF_AMP_MIN, X_HALF_AMP_MAX, X_AMP_PERIOD, theta);
        double xLength = modulateValue(X_LENGTH_MIN, X_LENGTH_MAX, X_LENGTH_PERIOD, theta);
        double xWidth = modulateValue(X_WIDTH_MIN, X_WIDTH_MAX, X_WIDTH_PERIOD, theta);
        double yOffsetPeriodMul = modulateValue(Y_OFFSET_PERIOD_MUL_MIN, Y_OFFSET_PERIOD_MUL_MAX, Y_OFFSET_PERIOD_MUL_PERIOD, theta);
        double yOffset = modulateValue(Y_OFFSET_MIN, Y_OFFSET_MAX, Y_OFFSET_PERIOD * yOffsetPeriodMul, theta);
        
        double colorProgTime = time / COLOR_PERIOD; 
        Color currentColor = Colors.sixColorProg(colorProgTime - Math.floor(colorProgTime));
        
        // Precompute the wave center at each point.
        for (int i = 0; i < width; i++) {
//            xWaveMax[i] = X_HALF_WIDTH + X_HALF_AMP * Math.sin(theta + i);
//            xWaveMin[i] = -X_HALF_WIDTH + X_HALF_AMP * Math.sin(theta + i);
            xCenter[i] = xHalfAmp * Math.sin(theta + i * Math.PI / xLength);
//            xWaveMax[i] = xCenter[i] + X_HALF_WIDTH;
//            xWaveMin[i] = xCenter[i] - X_HALF_WIDTH;
        }
        for (int i = 0; i < depth; i++) {
            zCenter[i] = xHalfAmp * Math.cos(theta + i * Math.PI / xLength);
        }
        
        for (Light light : my_light_array.getLights()) {
            int lightX = (int)Math.round(light.x + 8);
            int lightZ = (int)Math.round(light.z + 2);
            double waveCenter = (xCenter[lightX] + zCenter[lightZ] + yOffset);
            while (waveCenter > 5) {
                waveCenter -= 10;
            }
            while (waveCenter < -5) {
                waveCenter += 10;
            }
//            if (waveCenter < -5) {
//                waveCenter += 10;
//            }
//            if (lightX == 0 && lightZ == 0) {
//                Console.log("center: %f", waveCenter);
//            }  
            
            double distanceFromCenter = 0;
            if (light.y > waveCenter) {
                distanceFromCenter = Math.min(light.y - waveCenter, waveCenter + 10 - light.y);
            } else {
                distanceFromCenter = Math.min(waveCenter - light.y, light.y - (waveCenter - 10));
            }
//            distanceFromCenter = Math.abs(waveCenter - light.y);
            
            double intensity = clamp(2 - (distanceFromCenter / (xWidth / 4)), 0, 1);
            light.setColor(colorWithIntensity(currentColor, intensity));
//            if (light.y < xWaveMax[lightX] && light.y > xWaveMin[lightX]) {
//                light.setColor(Color.white);
//            }
        }
        return true;
    }
    
    private Color colorWithIntensity(Color color, double intensity) {
        if (intensity == 1) {
            return color;
        }
        float RGB[] = new float[3];
        color.getColorComponents(RGB);
        float fIntensity = (float)intensity;
        return new Color(RGB[0] * fIntensity, RGB[1] * fIntensity, RGB[2] * fIntensity);
    }
    
    private double modulateValue(double min, double max, double period, double theta) {
        double halfWidth = (max - min) / 2;
        return min + halfWidth + Math.sin(theta * Math.PI / period) * halfWidth;
    }    
}
