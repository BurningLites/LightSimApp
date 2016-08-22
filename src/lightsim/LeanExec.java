package lightsim;

import java.util.concurrent.*;

public class LeanExec implements Runnable {
    static final int FRAMERATE_HZ = 60;

    ScheduledExecutorService executor;
    LightArray lights;
    
    // Speed multiplier. 1 is real time, 0.5 is half time, etc.
    double speed = 1;
    
    // Last wallclock time in seconds.
    double lastWallclockTimeSeconds = 0;
    
    // Current "abstract" time in seconds. At each tick, this is incremented
    // by the delta in wallclock time multiplied by the current speed.
    double currentAbstractTimeSeconds = 0;
    
    // Current step number.
    int step;
    
    LightController controller;
    
    public LeanExec(ScheduledExecutorService executor, LightArray lights) {
        this.executor = executor;
        this.lights = lights;
    }
    
    public void start() {
        lastWallclockTimeSeconds = 0;
        currentAbstractTimeSeconds = 0;
        step = 0;
        
        long updateIntervalMicros = (long)(1e6 / FRAMERATE_HZ);
        executor.scheduleAtFixedRate(this, 0, updateIntervalMicros, TimeUnit.MICROSECONDS);
    }
    
    public void stop() {
        // TODO
    }
    
    public void setController(LightController controller) {
        controller.init(lights);
        this.controller = controller;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    @Override
    public void run() {
        double now = wallclockTimeSeconds();
        if (lastWallclockTimeSeconds > 0) {
           double timeDelta = (now - lastWallclockTimeSeconds) * speed;
           currentAbstractTimeSeconds += timeDelta;
        }
        lastWallclockTimeSeconds = now;
        controller.step ((int)(currentAbstractTimeSeconds * 1000));
        step++;
        
        // TODO(write lights to serial!!
    }
    
    private double wallclockTimeSeconds() {
        return System.currentTimeMillis() / 1e3;
    }
}
