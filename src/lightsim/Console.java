package lightsim;

/**
 *
 * @author kennybongort
 */
public class Console {
    public static void log(String format, Object... args) {
        System.out.println(String.format(format, args));
    }
}
