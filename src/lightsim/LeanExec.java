package lightsim;

import java.util.concurrent.*;

public class LeanExec implements Runnable {
    static final int FRAMERATE_HZ = 60;

    ScheduledExecutorService executor;
    LightArray lights;
    AnimationClock clock;
    SpiWriter spiWriter;
    
    // Current step number.
    int step;
    
    LightController controller;
    
    public LeanExec(ScheduledExecutorService executor, LightArray lights) {
        this.executor = executor;
        this.lights = lights;
        clock = new AnimationClock();
        spiWriter = SpiWriter.getWriter();
        if (spiWriter == null) {
            Console.log("Couldn't get writer. :(");
        }
    }
    
    public void start() {
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
        clock.setSpeed(speed);
    }
    
    @Override
    public void run() {
        if (controller != null) {
            Console.log("calling step with time: %f", clock.getCurrentTime());
            controller.step((int)(clock.getCurrentTime() * 1000));
        }
        step++;
        
        if (spiWriter != null) {
            spiWriter.writeLights(lights);
        }
    }
}
