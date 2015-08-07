This android-firmata software project allows you to control an Arduino board via Bluetooth from Android without writing code for Arduino. Instead, you upload the Firmata firmware (program) onto an Arduino microcontroller board to communicate with it through the Firmata protocol. This project implements the Firmata protocol for Android.

### Functions ###
So far only Firmata version 1 is supported, which lets you set pins as INPUT/OUTPUT and read or write data to it. I tested digital read and write and analog read.
Analog write using PWM pins works also well.

Available functions to control Arduino:
  * `int digitalRead(int pin);`
  * `int analogRead(int pin);`
  * `void pinMode(int pin, int mode);`
  * `void digitalWrite(int pin, int value);`
  * `void analogWrite(int pin, int value);`

**Only Android < 2.0 is supported yet**

### Instructions ###
  1. upload embedded Firmata sketch (Standard\_Firmata) to your Arduino
  1. configure your Bluetooth shield baud rate to 57600
  1. upload the Android app to your phone
  1. scan for your Arudino Bluetooth device by hitting 'menu' and select 'Scan for devices'
  1. pair and connect to your Arduino Bluetooth

Now you should see the on-board led (pin 13) blink, all other pins are configured to be input pins. The actual state of each input pin is shown on your phone screen.
Note: Unless you attach 5V or Ground to a digial pin you won't get a proper reading, but random. No pull up resistor is set!

### How to use it ###
You might have a look at the Android sample application to see how it works.
To write your own application you might basically just modify the **Firmata\_SampleApp** class to your need.
Check out [Usage](http://code.google.com/p/android-firmata/wiki/Usage) for more information.

### Known issues ###
for some reason, setting the pull-up resistor does not work.