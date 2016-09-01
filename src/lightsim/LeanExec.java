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
    
    public LeanExec(LightArray lights) {
        
        this.lights = lights;
        
        clock = new AnimationClock();
        listeners = new ArrayList<>();
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
        stop();
        clock.reset();
        controller.init(lights);
        notifyNewFrame();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void addListener(ExecListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
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
            try {
                controller.step(clock.getCurrentTime());
            } catch (Exception ex) {
                Console.log("Caught exception from controller step: " + ex);
            }
        }
        
        if (spiWriter != null) {
            spiWriter.writeLights(lights);
        }
        
        notifyNewFrame();
    }
    
    private void notifyNewFrame() {
        for (ExecListener listener : listeners) {
            listener.newFrameReady();
        }
    }
}
