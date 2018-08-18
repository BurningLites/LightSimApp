package lightsim;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    Clock sunClock;
    Timer sunEventTimer;
    Clock.Event nextSunEvent;

    public LeanExec(LightArray lights) {

        this.lights = lights;

        clock = new AnimationClock();
        sunClock = new Clock();
        listeners = new ArrayList<>();
        spiWriter = SpiWriter.getWriter();
        if (spiWriter == null) {
            Console.log("Couldn't get writer. This is totes normal if not running on the Pi.");
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

        turnOffLightsWithDelay(200);
        notifyStateChange();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public Clock.Event nextSunEvent() {
        return nextSunEvent;
    }

    public void setIsScheduled(boolean isScheduled) {
        if (this.isScheduled == isScheduled) {
            return;
        }
        this.isScheduled = isScheduled;
        if (isScheduled) {
            nextSunEvent = scheduleSunEventTimer();

            if (nextSunEvent.type == Clock.EventType.SUNRISE) {
                // If the next event is sunrise, turn the lights on.
                start();
            } else {
                stop();
                turnOffLights();
            }
        } else {
            nextSunEvent = null;
            if (sunEventTimer != null) {
                sunEventTimer.cancel();
                sunEventTimer = null;
            }
        }
    }

    private Clock.Event scheduleSunEventTimer() {
        if (sunEventTimer != null) {
            sunEventTimer.cancel();
        }
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date now = new Date();
        Console.log("It is %s", dateFormat.format(now));

        final Clock.Event nextEvent = sunClock.getNextEvent();
        if (nextEvent != null) {
            final boolean isSunrise = nextEvent.type == Clock.EventType.SUNRISE;
            String eventName = isSunrise ? "sunrise" : "sunset";
            Console.log("Next event is %s at %s", eventName, dateFormat.format(nextEvent.date));
            sunEventTimer = new Timer();
            sunEventTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Console.log("Timer fired. isSunrise? %s", isSunrise ? "true" : "false");
                    if (isSunrise) {
                        stop();
                    } else {
                        start();
                    }
                    scheduleSunEventTimer();
                }
            }, nextEvent.date);
        }
        return nextEvent;
    }

    public void addListener(ExecListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void setController(LightController controller) {
        controller.init(lights);
        this.controller = controller;
        notifyStateChange();
    }

    public String getControllerName() {
        return controller.name();
    }

    public void setSpeed(double speed) {
        clock.setSpeed(speed);
    }

    @Override
    public void run() {
        if (!isRunning) {
            return;
        }
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
            listener.execStateChanged(isRunning, isPaused, controller);
        }
    }
    private void notifyNewFrame() {
        for (ExecListener listener : listeners) {
            listener.newFrameReady();
        }
    }

    private void turnOffLights() {
        lights.fill(Color.BLACK, false);
        writeFrame();
        lights.fill(Color.BLACK, false);
        writeFrame();
        lights.fill(Color.BLACK, false);
        writeFrame();
    }

    private void turnOffLightsWithDelay(int delay) {
        Calendar fireTime = Calendar.getInstance();
        fireTime.add(MILLISECOND, delay);
        Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    turnOffLights();
                }
            }, fireTime.getTime());
    }
}
