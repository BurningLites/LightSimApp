package lightsim;

import java.awt.Color;
import java.util.*;
import static java.util.Calendar.*;
import java.util.concurrent.*;

public class LeanExec implements Runnable {
    static final double FRAMERATE_HZ = 60;

    ScheduledExecutorService executor;
    LightArray lights;
    AnimationClock clock;
    SpiWriter spiWriter;
    ArrayList<ExecListener> listeners;
    
    boolean isRunning;
    boolean isPaused;
    boolean isScheduled;
    
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
        Console.log("start called.");
        if (isRunning) {
            return;
        }
        Console.log("starting");
        if (!isPaused) {
            controller.init(lights);
        }
        
        isRunning = true;
        
        executor = new ScheduledThreadPoolExecutor(1);
        
        long updateIntervalMicros = (long)(1e6 / FRAMERATE_HZ);
        executor.scheduleAtFixedRate(this, 0, updateIntervalMicros, TimeUnit.MICROSECONDS);

        notifyStateChange();
    }
    
    public void pause() {
        Console.log("pause called.");
        if (!isRunning) {
            return;
        }
        Console.log("pausing");
        isRunning = false;
        isPaused = true;
        executor.shutdown();

        notifyStateChange();
    }
    
    public void stop() {
        Console.log("stop called.");
        if (!isPaused && !isRunning) {
            return;
        }
        Console.log("stopping");
        isRunning = false;
        isPaused = false;

        executor.shutdown();
        clock.reset();

        lights.fill(Color.BLACK, false);
        writeFrame();

        notifyStateChange();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isScheduled() {
        return isScheduled;
    }
    
    public void setIsScheduled(boolean isScheduled) {
        if (this.isScheduled == isScheduled) {
            return;
        }
        this.isScheduled = isScheduled;
        if (isScheduled) {
            // Start a timer for the next sunset or sunrise event.
            Calendar now = Calendar.getInstance();
            int hour = now.get(HOUR_OF_DAY);
            int minute = now.get(MINUTE);
            
            // Sunrise is 6:20-6:26.
            // Sunset is 7:34-7:24.
            Calendar todaySunrise = Calendar.getInstance();
            todaySunrise.set(HOUR_OF_DAY, 6);
            todaySunrise.set(MINUTE, 0); // Stop at 6:00 am.
            todaySunrise.set(SECOND, 0);
            
            Calendar todaySunset = Calendar.getInstance();
            todaySunset.set(HOUR_OF_DAY, 19);
            todaySunset.set(MINUTE, 50); // Start at 7:50 pm.
            todaySunset.set(SECOND, 0);
            
            Calendar tomorrowSunrise = (Calendar)todaySunrise.clone();
            tomorrowSunrise.roll(DAY_OF_YEAR, 1);
            
            Date nextEvent = null;
            boolean isSunrise = true;
            if (now.compareTo(todaySunrise) < 0) {
                nextEvent = todaySunrise.getTime();
                isSunrise = true;
            } else if (now.compareTo(todaySunset) < 0) {
                nextEvent = todaySunset.getTime();
                isSunrise = false;
            } else if (now.compareTo(tomorrowSunrise) < 0) {
                nextEvent = tomorrowSunrise.getTime();
                isSunrise = true;
            } else {
                Console.log("Current time not before tomorrow sunrise. O.o");
                assert(false);
            }
            
            if (isSunrise) {
                // If the next event is sunrise, turn the lights on.
                start();
            }
            
            final boolean isSunrise_f = isSunrise;
            if (nextEvent != null) {
                Console.log("It is %d:%02d.", hour, minute);
                String eventName = isSunrise ? "sunrise" : "sunset";
                Console.log("Next event is %s at %s", eventName, nextEvent.toString());
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Console.log("Timer fired. isSunrise? %s", isSunrise_f ? "true" : "false");
                        if (isSunrise_f) {
                            start();
                        } else {
                            stop();
                        }
                    }
                }, nextEvent);
            }
        } else {
            
        }
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
        writeFrame();
    }
    
    private void writeFrame() {
        if (spiWriter != null) {
            spiWriter.writeLights(lights);
        }
        notifyNewFrame();
    }
    
    private void notifyStateChange() {
        for (ExecListener listener : listeners) {
            listener.execStateChanged(isRunning, isPaused);
        }
    }
    private void notifyNewFrame() {
        for (ExecListener listener : listeners) {
            listener.newFrameReady();
        }
    }
}
