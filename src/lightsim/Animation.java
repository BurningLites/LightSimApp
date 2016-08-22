/*
 * The MIT License
 *
 * Copyright 2016 kennybongort.
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

/**
 *
 * @author kennybongort
 */
public class Animation {
    
    double startTimeSeconds = 0;
    double duration = 0;
    
    public Animation() {
        
    }
    
    public void setDuration(double duration) {
        this.duration = duration;
    }
    
    public void start(double startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }
    
    public boolean tick(double clockTime) {
        double dt = clockTime - startTimeSeconds;
        if (dt > duration) {
            return false;
        }
        update(dt / duration);
        return true;
    }
    
    /**
     * Updates the animator's effect given animation time t.
     * @param t The animation time, a value in the range [0..1].
     */
    public void update(double t) {
    }
    
    
}
