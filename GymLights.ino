#include <FastLED.h>
#include <SPI.h>

#define CLOCK_PIN 4
#define DATA_PIN 3
#define LED_PIN 13

#define USE_COMBINED_INTERRUPT 1
#define ENABLE_SERIAL 0

volatile int16_t writeIndex;
volatile boolean dataReady;
volatile boolean inTransfer;

static const int kNumLights = 10;
static const int kNumStrings = 25;
static const int kDataLength = kNumStrings * kNumLights * 3;

CRGB leds[kNumStrings][kNumLights];
volatile uint8_t colorData[kDataLength];
CRGB gLedsToWrite[kNumLights];

const int kSelectBus[] = {9, 8, 7, 6, 5};

const int kWireMap[] = {
  28, 25, 21, 18, 13,
  11 , 5,  2, 30, 27,
  24, 20, 17, 12,  6,
   3,  0, 29, 26, 22,
  19, 14, 10,  4,  1};

void setup() {
#if ENABLE_SERIAL
  Serial.begin(115200);  // For debugging.
#endif

  // Set up pins to control lights.
  pinMode(CLOCK_PIN, OUTPUT);
  pinMode(DATA_PIN, OUTPUT);
  pinMode(kSelectBus[0], OUTPUT);
  pinMode(kSelectBus[1], OUTPUT);
  pinMode(kSelectBus[2], OUTPUT);
  pinMode(kSelectBus[3], OUTPUT);
  pinMode(kSelectBus[4], OUTPUT);

  // Initialize LEDs to off.
  for (int i = 0; i < kNumStrings; i++) {
    setColor(i, CRGB::Black);
  }
  WriteLEDs();

  // Set up SPI
//  pinMode(MISO, OUTPUT);
  SPCR |= (1<<SPE);  // Enable SPI in slave mode.
  PrepareForSPI();

#if 0
  PCMSK0 = (1 << PCINT0);  // Select SS pin for pin change interrupt.
  PCICR |= (1 << PCIE0);  // Enable pin change interrupt.
#endif

  SPI.attachInterrupt();
}

void PrepareForSPI() {
  inTransfer = false;
  dataReady = false;
}

volatile uint16_t framesReceived = 0;
volatile uint16_t lastWriteIndex = 0;

// SPI interrupt routine

uint8_t startSequenceCount = 0;
uint32_t escapeCounter = 0;

void CheckStartSequence() {
  startSequenceCount = 0;
  while (true) {
    if (SPSR & 0b10000000) {
      if (SPDR == 0x01) {
        startSequenceCount++;
        if (startSequenceCount == 3) {
           ReceiveData();
           break;
        }
      } else {
        Serial.println(SPDR, HEX);
        break;
      }
    } else if (++escapeCounter == 1000000) {
      Serial.println("s\\");  // Because it's an escape character... get it?
      escapeCounter = 0;
      break;
    }
  }
}

void ReceiveData() {
  lastWriteIndex = writeIndex;
  writeIndex = 0;
  while (true) {
    if (SPSR & 0b10000000) {
      colorData[writeIndex++] = SPDR;

      if (writeIndex == kDataLength) {
        WriteLEDs();
        framesReceived++;
        break;
      }
    } else if (++escapeCounter == 1000000) {
      Serial.println("d\\");  // Because it's an escape character... get it?
      escapeCounter = 0;
      break;
    }
  }
}

#if USE_COMBINED_INTERRUPT
ISR(SPI_STC_vect) {
//  Serial.println(SPDR, HEX);
  if (SPDR == 0x01) {
    CheckStartSequence();
  }
}
#else  // USE_COMBINED_INTERRUPT
ISR(SPI_STC_vect) {
  if (SPDR == 0x01) {
    startSequenceCount
    lastWriteIndex = writeIndex;
    writeIndex = 0;
    inTransfer = true;
  } else if (inTransfer) {
    colorData[writeIndex++] = SPDR;

    if (writeIndex == kDataLength) {
      WriteLEDs();
      ePrepareForSPI();
      framesReceived++;
    }
  }
}
#endif  // USE_COMBINED_INTERRUPT

void PrintStats() {
  Serial.print("b ");
  Serial.println(lastWriteIndex);
  lastWriteIndex = 0;
  Serial.print("f ");
  Serial.println(framesReceived);
  framesReceived = 0;
}

void loop() { 
  delay(1000);
  #if ENABLE_SERIAL
  PrintStats();
  #endif
}

void setColor(int stringNum, CRGB color) {
  fill_solid(leds[stringNum], kNumLights, color);
}

void WriteLEDs() {
  uint16_t dataIndex = 0;
  for (uint8_t i = 0; i < kNumStrings; i++) {
    SelectString(i);
    for (uint8_t j = 0; j < kNumLights; j++) {
      sendByte(colorData[dataIndex]);
      sendByte(colorData[dataIndex + 1]);
      sendByte(colorData[dataIndex + 2]);
      dataIndex += 3;
    }
  }
}

void SelectString(uint8_t num) {
  num = kWireMap[num];

  PORTB = (PORTB & 0b11001111)
      | (num & 0x01 ? (1 << PB5) : 0)
      | (num & 0x02 ? (1 << PB4) : 0);
  PORTE = (PORTE & 0b10111111) | (num & 0x04 ? (1 << PE6) : 0);
  PORTD = (PORTD & 0b01111111) | (num & 0x08 ? (1 << PD7) : 0);
  PORTC = (PORTC & 0b10111111) | (num & 0x10 ? (1 << PC6) : 0);
}

void sendColor(CRGB color) {
  sendByte(color.red);
  sendByte(color.green);
  sendByte(color.blue);
}

inline void sendByte(uint8_t value) {
  sendBit(value & 0x80);
  sendBit(value & 0x40);
  sendBit(value & 0x20);
  sendBit(value & 0x10);
  sendBit(value & 0x08);
  sendBit(value & 0x04);
  sendBit(value & 0x02);
  sendBit(value & 0x01);
}

inline void sendBit(uint8_t value) {
  if (value) {
    sendHighBit();
  } else {
    sendLowBit();
  }
}

inline void sendHighBit() {
  __asm__(
    "cbi %0, %1 \n\t"  // Set clock low
    "sbi %0, %2 \n\t"  // Set data high
    "sbi %0, %1 \n\t"  // Set clock high
    "nop \n\t"         // Hold data
    ::
    // Inputs
    "I" (_SFR_IO_ADDR(PORTD)),  // %0
    "I" (PORTD4),  // %1 - clock
    "I" (PORTD0)  // %2 - data
  );
}

inline void sendLowBit() {
  __asm__(
    "cbi %0, %1 \n\t"  // Set clock low
    "cbi %0, %2 \n\t"  // Set data low
    "sbi %0, %1 \n\t"  // Set clock high
    "nop \n\t"         // Hold data
    ::
    // Inputs
    "I" (_SFR_IO_ADDR(PORTD)),  // %0
    "I" (PORTD4),  // %1 - clock
    "I" (PORTD0)  // %2 - data
  );
}
