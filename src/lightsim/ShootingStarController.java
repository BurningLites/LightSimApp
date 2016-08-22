package lightsim;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import lightsim.LightArray.Light;

public class ShootingStarController extends LightController {
    // Average frequency of star generation, in stars/second.
    static final double STAR_FREQUENCY = 10;
    static final double STAR_DURATION = 2;
    static final double INTENSITY_MULTIPLIER = 200;
    
    static Color bgColor = new Color(0, 0, 100);
    static Color starColor = new Color(255, 255, 0);

    //static Color bgColor = new Color(0, 0, 0);
    //static Color starColor = new Color(255, 255, 255);
    
    LightArray.Light strings[][];
    LightArray lightArray;
    ArrayList<StarAnimation> animations;
    Random random;
    
    double lastStepTime = 0;
    
    public void init(LightArray light_array) {
        this.lightArray = light_array;
        strings = light_array.getStrings();
        random = new Random();
        animations = new ArrayList<>();
        StarAnimation animation = new StarAnimation(0);
        animation.setDuration(STAR_DURATION);
        animation.start(0);
        animations.add(animation);

    }
    
    @Override
    public String name() {
        return "Shooting Stars";
    }
    
    @Override
    public boolean step(int time) {
        clearLights();
        
        double timeSeconds = (double)time / 1e3;
        // Randomly add some star animations.
        double dt = timeSeconds - lastStepTime;
        lastStepTime = timeSeconds;
        addNewStars(dt, timeSeconds);
        updateAnimations(timeSeconds);
        return true;
    }
    
    private void addNewStars(double dt, double time) {
        double targetStarsToAdd = dt * STAR_FREQUENCY;
        
        // Calculate how many stars to add. For fractional stars, roll a random
        // number to see whether to include a star.
        int starsToAdd = (int)Math.floor(targetStarsToAdd) +
            (random.nextDouble() < (targetStarsToAdd % 1) ? 1 : 0);
        for (int i = 0; i < starsToAdd; i++) {
            int stringNum = random.nextInt(50);
            StarAnimation animation = new StarAnimation(stringNum);
            animation.setDuration(STAR_DURATION);
            animation.start(time);
            animations.add(animation);
        }
    }
    
    private void updateAnimations(double time) {
        ArrayList<Animation> animationsToRemove = new ArrayList<>();
        for (Animation animation : animations) {
            if (animation.tick(time) == false) {
                animationsToRemove.add(animation);
            }
        }
        animations.removeAll(animationsToRemove);
    }
    
    private void clearLights() {
        for (Light light : lightArray.getLights()) {
            light.on = true;
            light.color = bgColor;
        }
    }
    
    public class StarAnimation extends Animation {
    
        int columnIndex = 0;
        int trailLength = 2;
    
        public StarAnimation(int columnIndex) {
            this.columnIndex = columnIndex;
        }
        
        public void update(double t) {
            double starHeight = 10 * t - 0.5;  // starHeight ranges from -0.5 to 9.5
            double intensity = 0.1 + t * 0.9;  // intensity ranges from 0.1 to 1.
            // double intensity = 1.0;
 
           
            double lowerLight = Math.floor(starHeight);
            double upperLight = Math.ceil(starHeight);
            
            // Interpolate intensity. Conveniently, lights are 1 unit apart so
            // no need to normalize.
            double lowerIntensity = intensity * (1 - (starHeight - lowerLight));
            double upperIntensity = intensity * (1 - (upperLight - starHeight));

            String ts = new DecimalFormat("0.000").format(t);

            Console.log(
                "time " + formatLogDouble(t) +
                // " lh " + Math.floor(starHeight) +
                // " uh " + Math.ceil(starHeight) +
                " lh " +  (int)lowerLight +
                " uh " + (int)upperLight +
                " li " + formatLogDouble(lowerIntensity) +
                " ui " + formatLogDouble(upperIntensity)
            );
            
            // Now write to the lights.
            if (lowerLight >= 0 && lowerLight < 10) {
                addIntensityToLight(strings[columnIndex][9 - (int)lowerLight], lowerIntensity);
            }
            if (upperLight >=0 && upperLight < 10) {
                addIntensityToLight(strings[columnIndex][9 - (int)upperLight], upperIntensity);
            }
        }
        
        private String formatLogDouble(double f) {
            return new DecimalFormat("0.000").format(f);
        }
        
        private int interpolateInts(int x, int y, double p) {
            return (int) Math.round(x * (1.0 - p) + y * p);
        }
        private Color interpolateColors(Color x, Color y, double p) {
            return new Color(
                interpolateInts(x.getRed(), y.getRed(), p),
                interpolateInts(x.getGreen(), y.getGreen(), p),
                interpolateInts(x.getBlue(), y.getBlue(), p));                
        }

        private void addIntensityToLight(Light light, double intensity) {
            Color currentColor = light.getColor();
            Color newColor = interpolateColors(currentColor, starColor, intensity);
            Console.log("interpolate: orig " +
                currentColor.toString() + " target " + starColor.toString() + " interp " + newColor.toString());
            light.setColor(newColor);
        }
    }
}
