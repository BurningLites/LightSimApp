package lightsim;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class SpiWriter {
    
    static final boolean ENABLE_SLOW_MODE = false; 
    static final int SPI_RATE = (int)(ENABLE_SLOW_MODE ? 500e3 : 2000e3);
    
    // Dynamically-loaded Spi.wiringPiSPIDataRW method.
    Method dataRwMethod;
    
    public static SpiWriter getWriter() {
        Method dataRwMethod;
        try {
            URL classLoaderUrls[] = new URL[]{new URL("file:///opt/pi4j/lib/pi4j-core.jar")};
            
            URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls, SpiWriter.class.getClassLoader());
            Class spiClass = urlClassLoader.loadClass("com.pi4j.wiringpi.Spi");
            Method setupModeMethod = spiClass.getMethod("wiringPiSPISetupMode", int.class, int.class, int.class);
            dataRwMethod = spiClass.getMethod("wiringPiSPIDataRW", int.class, byte[].class, int.class);
            
            setupModeMethod.invoke(null, 0, SPI_RATE, 0);
            
        } catch (Exception e) {
            Console.log("Error initializing SpiWriter: " + e);
            return null;
        }
        return new SpiWriter(dataRwMethod);
    }
    
    private SpiWriter(Method dataRwMethod) {
        this.dataRwMethod = dataRwMethod;
    };
    
    private SpiWriter() {};
    
    public void writeLights(LightArray lights) {
        ArrayList<LightArray.Light> lightArray = lights.getLights();
        
        byte color_data[] = new byte[lightArray.size() * 3];
        int writeIndex = 0;
        
        int count = 0;
        for (LightArray.Light light : lights.getLights()) {
            Color color = light.isOn() ? light.getColor() : Color.BLACK;
            color_data[writeIndex] = oneToZero((byte)color.getRed());
            color_data[writeIndex + 1] = oneToZero((byte)color.getGreen());
            color_data[writeIndex + 2] = oneToZero((byte)color.getBlue());
            writeIndex += 3;

            // TEMP: Only send 25 lights for now.
            count++;
            if (count == 25) {
                break;
            }
        }

        // Send magic start sequence.
        byte start_byte[] = {0x01, 0x01, 0x01, 0x01};
        try {
            dataRwMethod.invoke(null, 0, start_byte, start_byte.length);

            // System.out.println("Sending " + color_data.length + " bytes of color data.");
            dataRwMethod.invoke(null, 0, color_data, color_data.length);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Console.log("Error invoking SPIDataRW: " + e);
        }
    }

    // Convert 0x01 to 0x00; otherwise return the original value. 0x01 is
    // reserved for the magic start sequence so we never send it as a light
    // color component.
    public static byte oneToZero(byte value) {
        return value == 0x01 ? 0x00 : value;
    }
}
