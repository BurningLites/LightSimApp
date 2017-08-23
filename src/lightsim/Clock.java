/*
 * The MIT License
 *
 * Copyright 2017 kbongort.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lightsim;

import java.util.Calendar;
import static java.util.Calendar.*;
import java.util.Date;
import java.util.TimeZone;
import static lightsim.Clock.EventType.*;


/**
 *
 * @author kbongort
 */
public class Clock {
    
    public Event getNextEvent() {
        Date now = new Date();

        for (Event event : eventsToday()) {
            if (now.compareTo(event.date) < 0) {
                return event;
            }
        }
        Console.log("Current time not before tomorrow sunrise. O.o");
        assert(false);
        return null;
    }
    
    private Calendar now() {
        return Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private Event[] eventsToday() {
        // Sunrise is 6:20-6:26.
        // Sunset is 7:34-7:24.
        Calendar now = now();
        Calendar todaySunrise = (Calendar)now.clone();
        todaySunrise.set(HOUR_OF_DAY, 6);
        todaySunrise.set(MINUTE, 0); // Stop at 6:00 am.
        todaySunrise.set(SECOND, 0);

        Calendar todaySunset = (Calendar)now.clone();
        todaySunset.set(HOUR_OF_DAY, 19);
        todaySunset.set(MINUTE, 50); // Start at 7:50 pm.
        todaySunset.set(SECOND, 0);

        Calendar tomorrowSunrise = (Calendar)todaySunrise.clone();
        tomorrowSunrise.add(DAY_OF_YEAR, 1);
        
        Event[] events = {
            new Event(todaySunrise.getTime(), SUNRISE),
            new Event(todaySunset.getTime(), SUNSET),
            new Event(tomorrowSunrise.getTime(), SUNRISE)
        };
        return events;
    }
    
    private Event[] fakeEvents() {
        Calendar todaySunrise = Calendar.getInstance();
        int minute = todaySunrise.get(MINUTE);
        int base = minute / 10 * 10;
        int riseDelta = base + 2 - minute;
        int setDelta = riseDelta + 5;
        int nextRiseDelta = riseDelta + 10;
        Console.log("riseDelta: %d, setDelta: %d, nextRiseDelta: %d", riseDelta, setDelta, nextRiseDelta);
        todaySunrise.add(MINUTE, riseDelta);
        Calendar todaySunset = (Calendar)todaySunrise.clone();
        todaySunset.add(MINUTE, 5);
        Calendar tomorrowSunrise = (Calendar)todaySunrise.clone();
        tomorrowSunrise.add(MINUTE, 10);
        
        Event[] events = {
            new Event(todaySunrise.getTime(), SUNRISE),
            new Event(todaySunset.getTime(), SUNSET),
            new Event(tomorrowSunrise.getTime(), SUNRISE)
        };
        return events;
    }
    
    private Event[] fakeEventsSeconds() {
        Calendar todaySunrise = Calendar.getInstance();
        int second = todaySunrise.get(SECOND);
        int base = second / 10 * 10;
        int riseDelta = base + 2 - second;
        int setDelta = riseDelta + 5;
        int nextRiseDelta = riseDelta + 10;
        Console.log("riseDelta: %d, setDelta: %d, nextRiseDelta: %d", riseDelta, setDelta, nextRiseDelta);
        todaySunrise.add(SECOND, riseDelta);
        Calendar todaySunset = (Calendar)todaySunrise.clone();
        todaySunset.add(SECOND, 5);
        Calendar tomorrowSunrise = (Calendar)todaySunrise.clone();
        tomorrowSunrise.add(SECOND, 10);
        
        Event[] events = {
            new Event(todaySunrise.getTime(), SUNRISE),
            new Event(todaySunset.getTime(), SUNSET),
            new Event(tomorrowSunrise.getTime(), SUNRISE)
        };
        return events;
    }
    
    public enum EventType {
        SUNRISE,
        SUNSET
    }
    
    public class Event {
        public final Date date;
        public final EventType type;

        public Event(Date date, EventType type) {
            this.date = date;
            this.type = type;
        }
    }
}
