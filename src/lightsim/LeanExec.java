package lightsim;

import java.util.ArrayList;
import java.util.concurrent.*;

public class LeanExec implements Runnable {
    static final double FRAMERATE_HZ = 60;

    ScheduledExecutorService executor;
    LightArray lights;
    AnimationClock clock;
    SpiWriter spiWriter;
    ArrayList<ExecListener> listeners;
    
    boolean isRunning;
    
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
        if (isRunning) {
            return;
        }
        isRunning = true;
        
        executor = new ScheduledThreadPoolExecutor(1);
        
        long updateIntervalMicros = (long)(1e6 / FRAMERATE_HZ);
        executor.scheduleAtFixedRate(this, 0, updateIntervalMicros, TimeUnit.MICROSECONDS);
    }
    
    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        executor.shutdown();
    }
    
    public void reset() {
        clock.reset();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void addListener(ExecListener listener) {
        if (!stateListeners.contains(listener)) {
            stateListeners.add(listener);
        }
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
            controller.step((int)(clock.getCurrentTime() * 1000));
        }
        
        if (spiWriter != null) {
            spiWriter.writeLights(lights);
        }
        
        for (ExecListener listener : listeners) {
            listener.newFrameReady();
        }
    }
}
