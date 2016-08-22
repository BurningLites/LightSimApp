package lightsim;

import com.pi4j.wiringpi.Spi;
import java.awt.Color;
import java.util.ArrayList;

public class SpiWriter {
    
    static final boolean ENABLE_SLOW_MODE = false; 
    static final int SPI_RATE = (int)(ENABLE_SLOW_MODE ? 500e3 : 2000e3);
    
    public static SpiWriter getWriter() {
        try {
            int fd = Spi.wiringPiSPISetupMode(0, SPI_RATE, 0);
            if (fd <= -1) {
                Console.log(" ==>> SPI SETUP FAILED");
                return null;
            }
        } catch (UnsatisfiedLinkError e) {
            return null;
        }
        return new SpiWriter();
    }
    
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
        Spi.wiringPiSPIDataRW(0, start_byte, start_byte.length);
        
        // System.out.println("Sending " + color_data.length + " bytes of color data.");
        Spi.wiringPiSPIDataRW(0, color_data, color_data.length);
    }

    // Convert 0x01 to 0x00; otherwise return the original value. 0x01 is
    // reserved for the magic start sequence so we never send it as a light
    // color component.
    public static byte oneToZero(byte value) {
        return value == 0x01 ? 0x00 : value;
    }
}
