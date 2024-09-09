This contains the Arduino code used for the giraffe tower neck/mouth flame effect in 2024.

Briefly, the setup involved:
- 2x metal giraffe towers to mount the poofer flame effects along the neck and mouth
- 2x Arduino UNO R4 Wifi boards (one per giraffe)
  - We arbitrarily labeled these "left"/"right" for logic differentiation in the INO code
- Per Arduino: Arduino RF 315Mhz transmitter/receiver chip module, connected for garage-clicker-style remotes with 4 buttons
  - These are connected to analogRead channels 0-3
  - https://www.adafruit.com/product/1095
- Per Arduino: Output relay pins connected to 6x neck poofer solenoid valves, plus 1x mouth poofer solenoid (12V)
  - These are connected to digitalWrite OUPUT pins 0-6 to the relay board
- 8-channel relay board to switch on power to the solenoids
  - https://www.amazon.com/gp/product/B07XM5GVWJ/

The initial plan was to use the 4x remote control buttons to trigger different
pre-programmed sequences across both giraffes. Since the RF remote only
transmitted on a single frequency, both Arduinos would receive each signal. By
manually flipping the whichGiraffe left/right constant on the code uploaded to
each Arduino, we were able to branch on that in loopRF() to have a given button
trigger sequences one or both giraffes.

We found that there was pretty long and unpredictable latency between a button
press, and when the RF chip registered the analog input - say up to 1s, which
required long button holds. This was generally okay, but it also prevented us from
reliably doing any synchronized sequences between the left/right giraffe necks
(e.g. left on, right on, etc in sync; or poofing both of the large mouth
poofers at the same time).

On the other hand, we found that the WiFi connections were very reliable, even
under dusty conditions, and there was minimal latency. So, in this v16 code, Ed
does the following. We still use the RF signals to trigger effects, but one Arduino
connects to the other's WiFi AP to listen for control characters to run effects instead of RF.
- "left" Arduino publishes a WiFi access point Uno1, and runs an HTTP server on port 80
- "right" Arduino connects to Uno1 WiFi. (Note it's not possible to publish an AP and connect to a different AP simultaneously).
- Only "right" uses loopRF() to listen for remote button presses. It sends single character data 'A'-'D' via HTTP to "left"
- Only "left" uses receiveData() to react to HTTP inputs from "right"

This allowed pretty tight control over synchronized timing sequences between the two.

The idea behind poofMouthPersistent is that the RF chip doesn't give you a
binary `button on` vs `button off` event during a long press; it just
continuously spams analogRead input while pressed. So, in order to leave the
poofer on as long as the button is held, we register the latest timestamp of a
buttonA press, then we leave the mouth poofer relay pin on HIGH and
continuously compare against the latest button-received timestamp until we timeout
(currentMs - mouthLastSignal > 750).
