#include <SPI.h>
#include <WiFiS3.h>
#include <ArduinoOTA.h>

// INPUTS:
//   switch this before compiling per each left/right arduino.
//   no other const changes should be needed for the subsequent wifi settings before compiling.
String version = "16";
String whichGiraffe = "left";
// String whichGiraffe = "right";

bool isLeft() {
  return whichGiraffe == "left";
}
bool isRight() {
  return whichGiraffe == "right";
}

// *********************************************************************************
// Wifi Settings

int keyIndex; // your network key index number (needed only for WEP)
const char wifipass[] = "12345678"; // your network password (use for WPA, or use as key for WEP)
char* ssid; // your network SSID (name)
char ssid1[] = "Uno1";
char ssid2[] = "Uno2";

int led = LED_BUILTIN;
int status = WL_IDLE_STATUS;
int relay[] = { 1,2,3,4,5,6,7 };
int neckCount = 6;
int mouthIdx = 6;
const byte BUTTON_THRESHOLD = 50; // for RF chip output - analog signal recognition

WiFiServer server(80);
WiFiClient client = server.available();


void setup() {
  // set global consts
  if (isLeft()) {
    ssid = ssid1; keyIndex = 0;
  } else {
    ssid = ssid2; keyIndex = 1;
  }
  // Initialize serial:
  Serial.begin(9600);
  while (!Serial); // wait for serial port to connect. Needed for native USB port only
  delay(1000);
  pinMode(led, OUTPUT); // set the LED pin mode
  Serial.println("");
  Serial.println("---------------------------");

  if (isRight()) {
    Serial.println("Connecting to Giraffe Access Point v" + version + ": " + whichGiraffe);
    connectToAccessPoint(); // not used when we're publishing our own AP
  } else {
    Serial.println("Launching Giraffe Access Point v" + version + ": " + whichGiraffe);
    publishOwnAccessPoint();
  }
  // start the WiFi OTA library with internal (flash) based storage
  // Serial.println("Attempting ArduinoOTA.begin:");
  ArduinoOTA.begin(WiFi.localIP(), "Arduino", "password", InternalStorage);
  // Serial.println("Attempting printWifiStatus:");
  printWifiStatus();
  for (int i = 0; i < neckCount; i++) {
    pinMode(relay[i], OUTPUT);
  }
  pinMode(relay[mouthIdx], OUTPUT);
}

void publishOwnAccessPoint() {
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    while (true);
  }
  String fv = WiFi.firmwareVersion();
  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {
    Serial.println("  Please upgrade the firmware");
  }
  // by default the local IP address will be 192.168.4.1. you can override it with the following:
  if (isLeft()) {
    WiFi.config(IPAddress(192,168,4,1));
  } else {
    WiFi.config(IPAddress(192,168,4,2));
  }
  // print the network name (SSID);
  Serial.println("Creating access point named: " + String(ssid));
  // Create open network. Change this line if you want to create an WEP network:
  status = WiFi.beginAP(ssid, wifipass);
  if (status != WL_AP_LISTENING) {
    Serial.println("Creating access point failed. Hanging...");
    while (true);
  }
  Serial.println("Waiting 2s for wifi connections...");
  delay(2000);
  Serial.println("- Done. Beginning loop()");
  Serial.println("Starting server on port 80");
  server.begin();
}

void connectToAccessPoint() {
  // check for the presence of the shield:
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present. Hanging...");
    while (true); // don't continue
  }
  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.println("Attempting to connect to SSID: Uno1 ");
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin("Uno1", "12345678");
  }
}

void printWifiStatus() {
  Serial.println("  SSID: " + String(WiFi.SSID()));
  IPAddress ip = WiFi.localIP();
  Serial.println("  IP Address: " + ip.toString());
  long rssi = WiFi.RSSI(); // print the received signal strength:
  Serial.println("  Signal strength (RSSI): " + String(rssi) + " dBm");
}

// *********************************************************************************

void loopAccessPoint() {
  if (status != WiFi.status()) { // compare the previous status to the current status.
    status = WiFi.status(); // if changed, update the global
    if (status == WL_AP_CONNECTED) {
      Serial.println("Device connected to AP");
    } else {
      Serial.println("Device disconnected from AP");
    }
  }
}

void loopArduinoOTA() {
  ArduinoOTA.poll(); // check for WiFi OTA updates
}

// *********************************************************************************
// giraffe neck sequences

void poof1(int idx1, int durationOn) {
  digitalWrite(relay[idx1], HIGH);
  delay(durationOn);
  digitalWrite(relay[idx1], LOW);
}

void poof2(int idx1, int idx2, int durationOn) {
  digitalWrite(relay[idx1], HIGH);
  digitalWrite(relay[idx2], HIGH);
  delay(durationOn);
  digitalWrite(relay[idx1], LOW);
  digitalWrite(relay[idx2], LOW);
}

void seqUp(int durationOn, int sleepBetween) {
  for (int i = 0; i < neckCount; i++) {
    poof1(i, durationOn);
    delay(sleepBetween);
  }
}

void seqDown(int durationOn, int sleepBetween) {
  for (int i = 0; i < neckCount; i++) {
    poof1(neckCount - i - 1, durationOn);
    delay(sleepBetween);
  }
}

void poofMouth(int durationOn) {
  poof1(mouthIdx, durationOn);
}

bool mouthState = false;
unsigned long mouthLastSignal = 0;

void poofMouthPersistent() {
  unsigned long currentMs = millis();
  mouthLastSignal = currentMs;
  if (!mouthState) {
    digitalWrite(relay[mouthIdx], HIGH);
  }
  mouthState = true;
  Serial.println("    mouthState: on");
}

void checkMouthLoop() {
  unsigned long currentMs = millis();
  if ((currentMs - mouthLastSignal > 750) && mouthState) {
    digitalWrite(relay[mouthIdx], LOW);
    mouthState = false;
    Serial.println("    mouthState off");
  }
}

void outsideIn(int durationOuter, int durationMiddle, int sleepBetween) {
  poof2(0, 5, durationOuter); delay(sleepBetween);
  poof2(1, 4, durationOuter); delay(sleepBetween);
  poof2(2, 3, durationMiddle);
}

void loopRF() {
  byte buttonA = analogRead(3); // forward right
  byte buttonB = analogRead(2); // forward left
  byte buttonC = analogRead(1); // reverse right
  byte buttonD = analogRead(0); // reverse left
  // outsideIn(100, 500, 250);
  // seqDown(50, 200);

  if (isRight()) {
    if (buttonA > BUTTON_THRESHOLD)  {
      Serial.println("  got buttonA");
      sendData("A");
      poofMouthPersistent();
    } else if (buttonB > BUTTON_THRESHOLD) {
      Serial.println("  got buttonB");
      sendData("B");
      seqUp(50, 200); seqDown(50, 200);
      seqUp(50, 200); seqDown(50, 200);
      delay(125);
      poof1(mouthIdx, 1000);
    } else if (buttonC > BUTTON_THRESHOLD) {
      Serial.println("  got buttonC");
      sendData("C");
      seqUp(50, 100);
      delay(900);
      seqUp(50,100);
      delay(900);
      poof1(mouthIdx, 1000);
    } else if (buttonD > BUTTON_THRESHOLD) {
      Serial.println("  got buttonD");
      sendData("D");
      seqDown(50, 100); seqUp(50, 100);
      seqDown(50, 100); seqUp(50, 100);
      outsideIn(100, 100, 250);
      poof1(mouthIdx, 1000);
    }
  }
}

void receiveData() {
  if (client) {                     // if you get a client,
    Serial.println("new client");   // print a message out the serial port
    String currentLine = "";        // make a String to hold incoming data from the client
    while (client.connected()) {    // loop while the client's connected
      if (client.available()) {     // if there's bytes to read from the client,
        char c = client.read();     // read a byte, then:
        Serial.write(c);
        if (c == 'A') {
          poofMouthPersistent();
        } else if (c == 'B') {
          delay(125);
          seqUp(50, 200); seqDown(50, 200);
          seqUp(50, 200); seqDown(50, 200);
          poof1(mouthIdx, 1000);
        } else if (c == 'C') {
          delay(900);
          seqUp(50, 100);
          delay(900);
          seqUp(50,100);
          poof1(mouthIdx, 1000);
        } else if (c == 'D') {
          seqUp(50, 100); seqDown(50, 100);
          seqUp(50, 100); seqDown(50, 100);
          outsideIn(100, 100, 250);
          poof1(mouthIdx, 1000);
        }
      }
    }
    client.stop(); // close the connection
    Serial.println("client disconnected");
  }
}

void sendData(String data) {
  if (WiFi.status() != WL_CONNECTED) {
    connectToAccessPoint();
  }
  if (client.connect("192.168.4.1", 80)) {
    Serial.println("Connected to server");
    client.println(data);
    client.stop();
    poofMouthPersistent();
  } else {
    Serial.println("connection failed");
  }
}

void loop() {
  checkMouthLoop();
  loopAccessPoint();
  loopArduinoOTA();
  loopRF();
  client = server.available();
  if (client) {
    receiveData();
  }
}
