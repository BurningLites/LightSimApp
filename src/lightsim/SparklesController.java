package lightsim;

import java.awt.Color;
import java.util.Random;
import lightsim.LightArray.Light;

public class SparklesController extends LightController {
    
    final double SPARKLE_RATE = 120;  // Sparkles per second
    final double SPARKLE_RATE_RANGE = 0.4;  // Amount of possible variance in sparkle rate.
    
    final double FADE_OUT_TIME_MIN = 2;
    final double FADE_OUT_TIME_MAX = 6;
    
    final int COLOR_WHEEL_RANGE = 40;
    
    double sparklesToAdd = 0;
    double lastStepTime = 0;
    
    double colorWheelPos = 0;  // Position on color wheel in [0..1)
    double colorWheelDuration = 60;  // Seconds to progress through full wheel
    double colorWheelVelocity = 0.1;  // Rate at which we move along the color wheel.
    
    Animation animations[] = new Animation[500];
    
    public SparklesController() {
    }
    
    @Override
    public String name() {
        return "Sparkles";
    }
    
    @Override
    public void init(LightArray lightArray) {
        super.init(lightArray);
    }
    
    @Override
    public boolean step(double time) {
        clearLights();
        
        // Timekeeping
        double dt = time - lastStepTime;
        lastStepTime = time;
        
        // How many sparkles should we add?
        double targetNumSparkles = SPARKLE_RATE * dt;
        
        sparklesToAdd +=
            randomDoubleInRange(targetNumSparkles * (1 - SPARKLE_RATE_RANGE),
                                targetNumSparkles * (1 + SPARKLE_RATE_RANGE));
        
        colorWheelPos += dt / colorWheelDuration;
//        colorWheelPos += colorWheelVelocity * dt;
//        
//        colorWheelVelocity += randomDoubleInRange(-.01, .01);
//        if (colorWheelVelocity > .2) {
//            colorWheelVelocity = .2;
//        } else if (colorWheelVelocity < 0) {
//            colorWheelVelocity = 0;
//        }
//        Console.log("Velocity: %f", colorWheelVelocity);
        
        if (colorWheelPos > 1) {
            colorWheelPos = 0;
        }
        int colorWheelCenter = (int)Math.round(colorWheelPos * 255);
        
        while (sparklesToAdd >= 1) {
            // Pick a random light for the sparkle.
            int lightIndex = random.nextInt(500);
            
           
            int colorIndex = colorWheelCenter - COLOR_WHEEL_RANGE / 2 +
                random.nextInt(COLOR_WHEEL_RANGE);
            colorIndex = (colorIndex + 255) % 255;
            double duration = randomDoubleInRange(FADE_OUT_TIME_MIN, FADE_OUT_TIME_MAX);
            
            Animation anim = new SparkleAnimation(lightIndex, colorWheel(colorIndex));
            anim.setDuration(duration);
            anim.start(time);
            
            if (animations[lightIndex] != null) {
                removeAnimation(animations[lightIndex]);
            }
            animations[lightIndex] = anim;
            addAnimation(anim);

            sparklesToAdd -= 1;
        }

        updateAnimations(time);
        return true;
    }
    
    
    
    // Returns a color from a rainbow wheel.
    private Color colorWheel(int i) {
        int r, g, b;
        if (i > 255) {
            return Color.black;
        }
        if (i < 85) {
            r = i * 3;
            g = 255 - (i * 3);
            b = 255;
        } else if (i < 170) {
            r = 255;
            g = (i - 85) * 3;
            b = 255 - (i - 85) * 3;
        } else {
            r = 255 - (i - 170) * 3;
            g = 255;
            b = (i - 170) * 3;
        }
//        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
//            Console.log("bad color");
//        }
//        Console.log("Starting light with color: <%d, %d, %d>", r, g, b);
        return new Color(r, g, b);
    }
    
    class SparkleAnimation extends Animation {
        int lightIndex;
        Light light;
        float initialColorComponents[] = new float[3];
        
        public SparkleAnimation(int lightIndex, Color initialColor) {
            this.lightIndex = lightIndex;
            light = my_light_array.getLights().get(lightIndex);
            initialColor.getRGBColorComponents(initialColorComponents);
        }
        
        @Override
        public void update(double t) {
            
            float fadeAmount = (float)(1 - t);
            
            Color currentColor = new Color(
                initialColorComponents[0] * fadeAmount,
                initialColorComponents[1] * fadeAmount,
                initialColorComponents[2] * fadeAmount);
            
//            Console.log("Setting color of light <%d, %d, %d> to <%d, %d, %d>",
//                        light.ix, light.iy, light.iz,
//                        currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
            light.setColor(currentColor);
            
        }
        
        @Override
        public void onComplete() {
            animations[lightIndex] = null;
        }
    }
    
    
}
