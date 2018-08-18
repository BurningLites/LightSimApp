package lightsim;

/**
 *
 * @author kbongort
 */
public interface ExecListener {

    public void execStateChanged(boolean running, boolean paused, LightController controller);

    public void newFrameReady();
}
