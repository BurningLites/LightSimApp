package lightsim;

/**
 *
 * @author kbongort
 */
public interface ExecListener {
    
    public void execStateChanged(boolean running);
    
    public void newFrameReady();
}
