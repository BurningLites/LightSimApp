package lightsim;

/**
 *
 * @author kbongort
 */
public interface ExecListener {
    
    public void execStateChanged(boolean running, boolean paused);
    
    public void newFrameReady();
}
