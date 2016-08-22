package lightsim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import lightsim.LightArray.Light;

public class ShootingStarController extends LightController {
    // Average frequency of star generation, in stars/second.
    static final double STAR_FREQUENCY = 10;
    
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
            animation.setDuration(3);
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
            light.color = Color.black;
        }
    }
    
    public class StarAnimation extends Animation {
    
        int columnIndex = 0;
    
        public StarAnimation(int columnIndex) {
            this.columnIndex = columnIndex;
        }
        
        public void update(double t) {
            double starHeight = 11 * t - 0.5;  // starHeight ranges from -0.5 to 10.5
            double intensity = 0.25 + t * 0.75;  // intensity ranges from 0.25 to 1.
            
            double lowerLight = Math.floor(starHeight);
            double upperLight = Math.ceil(starHeight);
            
            // Interpolate intensity. Conveniently, lights are 1 unit apart so
            // no need to normalize.
            double lowerIntensity = intensity * (1 - (starHeight - lowerLight));
            double upperIntensity = intensity * (1 - (upperLight - starHeight));
            
            // Now write to the lights.
            if (lowerLight >= 0 && lowerLight < 10) {
                strings[columnIndex][9 - (int)lowerLight]
                    .setColor(colorForIntensity(lowerIntensity));
            }
            if (upperLight >=0 && upperLight < 10) {
                strings[columnIndex][9 - (int)upperLight]
                    .setColor(colorForIntensity(upperIntensity));
            }
        }
        
        private Color colorForIntensity(double intensity) {
            int value = Math.min(255, (int)(intensity * 400));
            return new Color(value, value, value);
        }
    }
}
