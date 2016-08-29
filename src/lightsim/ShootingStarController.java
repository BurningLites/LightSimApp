package lightsim;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import lightsim.LightArray.Light;

public class ShootingStarController extends LightController {
    // Average frequency of star generation, in stars/second.
    static final double STAR_FREQUENCY = 2;
    static final double STAR_DURATION = 20;
    
    static Color bgColor = new Color(0, 0, 100);
    static Color starColor = new Color(255, 255, 0);
    

    //static Color bgColor = new Color(0, 0, 0);
    //static Color starColor = new Color(255, 255, 255);
    
    LightArray.Light strings[][];
    LightArray lightArray;
    ArrayList<StarAnimation> animations;
    HashMap activeStrings;
    Random random;
    
    double lastStepTime = 0;
    
    public void init(LightArray light_array) {
        this.lightArray = light_array;
        strings = light_array.getStrings();
        random = new Random();
        animations = new ArrayList<>();
        activeStrings = new HashMap();
        createAnimation(0, 0); 
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
    
    private void createAnimation(int string, double startTime) {
        Console.log("starting string " + string);
        StarAnimation animation = new StarAnimation(string);
        animation.setDuration(STAR_DURATION);
        animation.start(startTime);
        animations.add(animation);
        activeStrings.put(string, Boolean.TRUE);

    }
    
    private void addNewStars(double dt, double time) {
        int numActive = animations.size();
        double targetStarsToAdd = dt * STAR_FREQUENCY;
        
        // Calculate how many stars to add. For fractional stars, roll a random
        // number to see whether to include a star.
        int starsToAdd = (int)Math.floor(targetStarsToAdd) +
            (random.nextDouble() < (targetStarsToAdd % 1) ? 1 : 0);
        // Console.log("target to add: " + starsToAdd);
        for (int i = 0; i < starsToAdd; i++) {
            int stringNum = random.nextInt(50);
            
            Boolean isActive = (Boolean) activeStrings.getOrDefault(stringNum, Boolean.FALSE);
            Console.log("string " + stringNum + " isActive " + isActive.booleanValue());
            if (!(isActive.equals(Boolean.TRUE))) {
                createAnimation(stringNum, time);
            }
        }
    }
    
    private void updateAnimations(double time) {
        ArrayList<StarAnimation> animationsToRemove = new ArrayList<>();
        for (StarAnimation animation : animations) {
            animation.tick(time);
            if (animation.isFinished(time)) {
                Console.log("removing string " + animation.getColumnIndex());
                animationsToRemove.add(animation);
                activeStrings.put(animation.getColumnIndex(), Boolean.FALSE);
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
    
        double intensityBase = 0.3;
        int columnIndex = 0;
        double stretch;
        int fadeDuration;
        int posOffset;
        Color varColor;
        
        public StarAnimation(int columnIndex) {
            Random r = new Random();
            this.columnIndex = columnIndex;
            this.stretch = 0.2 + (r.nextDouble() * 0.4);
            this.fadeDuration = 4 + (r.nextInt(4));
            // this.posOffset = 0 - (r.nextInt(5));
            this.posOffset = 0;
            
            // this.varColor = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            float colorBucket = random.nextFloat(); // 1/3 chance of white, yellow, orange
            if (colorBucket <= 0.33) {
              this.varColor = new Color(255, 255, 255);
            } else if (colorBucket <= 0.67) {
              this.varColor = new Color(255, 255, 0);
            } else {
              this.varColor = new Color(255, 230, 60);
            }

            Console.log("added string " + columnIndex + 
                " stretch " + stretch +
                " fadeDuration " + fadeDuration +
                " offset " + posOffset);
        }
        public int getColumnIndex() {
            return this.columnIndex;
        }
        
        private double getLightIntensity(int pos, double t) {
            t = t * STAR_DURATION;
            // Console.log("  getLightIntensity pos " + pos + " t " + formatLogDouble(t));
            double heightFactor = 0.05 + (pos - 1) * 0.1;
            double maxIntensity = intensityBase + (heightFactor * (1.0 - intensityBase));
            
            double startTime = (pos * stretch);
            double peakTime =  ((pos + 1) * stretch);
            double fadedTime = ((pos + 1 + fadeDuration) * stretch);
                     
            // e.g. fade in:  0 -> 1s
            // e.g. fade out: 1 -> 3s
            double intensity = 0.0;
            String state = "";
            if ((t < startTime) || (t > fadedTime)) {
                intensity = 0.0;
            } else if (startTime <= t && t < peakTime) {
                state = "pre-peak";
                intensity = maxIntensity - ((peakTime - t) / stretch * maxIntensity);
            } else if (peakTime <= t && t < fadedTime) {
                state = "fading";
                intensity = ((fadedTime - t) / (fadeDuration * stretch) * maxIntensity);
            }
            intensity = Math.min(intensity, 1.0);
            
            if (intensity > 0 && false) {
                Console.log(
                    "string " + this.columnIndex + " time " + formatLogDouble(t) +
                    " pos " + pos +
                    " maxIntensity " + formatLogDouble(maxIntensity) +
                    " s " + formatLogDouble(startTime) +
                    " e " + formatLogDouble(fadedTime) +
                    " intensity " + formatLogDouble(intensity) +
                        " " + state
                );
            }
            return intensity;
        }
        
        public boolean isFinished(double t) {
          return (t - this.startTimeSeconds) > (10 + fadeDuration) * stretch;
        }
        
        public void update(double t) {
            // double starHeight = 10 * t - 0.5;  // starHeight ranges from -0.5 to 9.5
            // double intensity = 0.1 + t * 0.9;  // intensity ranges from 0.1 to 1.
            // double lowerLight = Math.floor(starHeight);
            // double upperLight = Math.ceil(starHeight);
            // Interpolate intensity. Conveniently, lights are 1 unit apart so
            // no need to normalize.
            // double lowerIntensity = intensity * (1 - (starHeight - lowerLight));
            // double upperIntensity = intensity * (1 - (upperLight - starHeight));

            for (int i = 0; i < 10; ++i) {
                if (9 - i + posOffset >= 0) {
                  addIntensityToLight(strings[columnIndex][9 - i + posOffset], getLightIntensity(i, t));
                }
            }
        }
        
        private String formatLogDouble(double f) {
            return new DecimalFormat("0.000").format(f);
        }
        
        private int interpolateInts(int x, int y, double p) {
            return (int) Math.round(x * (1.0 - p) + y * p);
        }
        private Color interpolateColors(Color x, Color y, double p) {
            //Console.log ("interpolate " + x + " " + y + " " + formatLogDouble(p) + 
            //    interpolateInts(x.getRed(), y.getRed(), p) + " " +
            //    interpolateInts(x.getGreen(), y.getGreen(), p) + " " +
            //    interpolateInts(x.getBlue(), y.getBlue(), p));
            Color c = new Color(
                interpolateInts(x.getRed(), y.getRed(), p),
                interpolateInts(x.getGreen(), y.getGreen(), p),
                interpolateInts(x.getBlue(), y.getBlue(), p));
            return c;
        }

        private void addIntensityToLight(Light light, double intensity) {
            Color currentColor = light.getColor();
            // Color newColor = interpolateColors(currentColor, starColor, intensity);
            Color newColor = interpolateColors(currentColor, varColor, intensity);
            /*
            Console.log("interpolate: orig " + currentColor.toString() +
                " target " + starColor.toString() +
                " interp " + newColor.toString() +
                " p " + intensity);
*/
            light.setColor(newColor);
        }
    }
}
