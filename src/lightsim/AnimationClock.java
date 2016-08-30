package lightsim;

public class AnimationClock {
    // Speed multiplier. 1 is real time, 0.5 is half time, etc.
    double speed = 1;
    
    // Last wallclock time in seconds.
    double lastWallclockTimeSeconds = 0;
    
    // Current "abstract" time in seconds. At each tick, this is incremented
    // by the delta in wallclock time multiplied by the current speed.
    double currentAbstractTimeSeconds = 0;
    
    public AnimationClock() {
        lastWallclockTimeSeconds = System.currentTimeMillis() / 1e3;
    }
    
    /**
     * Get the current animation clock time.
     * @return The current animation clock time. This time is subject to the
     *     clock speed, so it may differ from real time.
     */
    public double getCurrentTime() {
        return updateAbstractTime();
    }
    
    public void setSpeed(double speed) {
        // Update the abstract time to bring us up to now, given the
        // previous clock speed.
        updateAbstractTime();
        this.speed = speed;
    }
    
    public double getSpeed() {
        return speed;
    }

    public void reset() {
        currentAbstractTimeSeconds = 0;
        lastWallclockTimeSeconds = System.currentTimeMillis() / 1e3;
    }
    
    /**
     * Updates the abstract time based on elapsed wallclock time and the
     * clock's speed.
     */
    private double updateAbstractTime() {
        double now = getWallclockTime();
        if (lastWallclockTimeSeconds > 0) {
           double timeDelta = (now - lastWallclockTimeSeconds) * speed;
           currentAbstractTimeSeconds += timeDelta;
        }
        lastWallclockTimeSeconds = now;
        return currentAbstractTimeSeconds;
    }
    
    private double getWallclockTime() {
        return System.currentTimeMillis() / 1e3;
    }
    
    
}
