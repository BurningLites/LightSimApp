package lightsim;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import lightsim.LightArray.Light;

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
            
            // Open both SPI channels. One channel is dedicated to each microcontroller.
            setupModeMethod.invoke(null, 0, SPI_RATE, 0);
            setupModeMethod.invoke(null, 1, SPI_RATE, 0);
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
        Light strings[][] = lights.getStrings();
        
        int numBytesPerSide = 250 * 3;
        byte color_data_left[] = new byte[numBytesPerSide];
        byte color_data_right[] = new byte[numBytesPerSide];
        
        int count = 0;
        for (int sideNum = 0; sideNum < 2; sideNum++) {
            int baseStringIndex = sideNum * 25;
            int writeIndex = 0;
            byte array_to_write[] = sideNum == 0 ? color_data_left : color_data_right;
            for (int stringIndex = 0; stringIndex < 25; stringIndex++) {
                for (Light light : strings[baseStringIndex + stringIndex]) {
                    Color color = light.isOn() ? light.getColor() : Color.BLACK;
                    array_to_write[writeIndex] = oneToZero((byte)color.getRed());
                    array_to_write[writeIndex + 1] = oneToZero((byte)color.getGreen());
                    array_to_write[writeIndex + 2] = oneToZero((byte)color.getBlue());
                    writeIndex += 3;
              }
            }
        }

        try {
            byte start_sequence[] = {0x01, 0x01, 0x01, 0x01};

            // Write left array.
            byte start_sequence_to_send[] = start_sequence.clone();  // Data is mutated when sent so send a copy.
            dataRwMethod.invoke(null, 0, start_sequence_to_send, start_sequence_to_send.length);

            // System.out.println("Sending " + color_data_left.length + " bytes of left color data: " +
            //     bytesToHex(color_data_right));
            dataRwMethod.invoke(null, 0, color_data_left, color_data_left.length);

            // Write right array.
            start_sequence_to_send = start_sequence.clone();
            dataRwMethod.invoke(null, 1, start_sequence_to_send, start_sequence_to_send.length);

            // System.out.println("Sending " + color_data_left.length + " bytes of right color data: " +
            //     bytesToHex(color_data_right));
            dataRwMethod.invoke(null, 1, color_data_right, color_data_right.length);
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

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
