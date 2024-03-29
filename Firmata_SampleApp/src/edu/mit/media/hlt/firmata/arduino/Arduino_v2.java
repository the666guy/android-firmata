/*
  Copyright (c) 2009 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  
  Code depends on David A. Mellis's implementation for Processing
*/
package edu.mit.media.hlt.firmata.arduino;

import edu.mit.media.hlt.firmata.serial.Serial;

/**
 * Together with the Firmata 2 firmware (an Arduino sketch uploaded to the
 * Arduino board), this class allows you to control the Arduino board from
 * Processing: reading from and writing to the digital pins and reading the
 * analog inputs.
 */
public class Arduino_v2 extends Arduino {

	public static final String TAG = "Arduino_v2";

	static final int MAX_DATA_BYTES = 32;

	static final int DIGITAL_MESSAGE        = 0x90; // send data for a digital port
	static final int ANALOG_MESSAGE         = 0xE0; // send data for an analog pin (or PWM)
	static final int REPORT_ANALOG          = 0xC0; // enable analog input by pin #
	static final int REPORT_DIGITAL         = 0xD0; // enable digital input by port
	static final int SET_PIN_MODE           = 0xF4; // set a pin to INPUT/OUTPUT/PWM/etc
	static final int REPORT_VERSION         = 0xF9; // report firmware version
	static final int SYSTEM_RESET           = 0xFF; // reset from MIDI
	static final int START_SYSEX            = 0xF0; // start a MIDI SysEx message
	static final int END_SYSEX              = 0xF7; // end a MIDI SysEx message

	int inputData;
	int command;

	int waitForData = 0;
	int executeMultiByteCommand = 0;
	int multiByteChannel = 0;
	int[] storedInputData = new int[MAX_DATA_BYTES];
	boolean parsingSysex = false;
	int sysexBytesRead;

	int[] digitalOutputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	int[] digitalInputData  = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	int[] analogInputData   = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	

	public Arduino_v2(Serial serial) {
		this.serial = serial;
		serial.registerArduino(this);
		reportState();
	}

	@Override
	public void reportState(){
		//		for (int i = 0; i < 6; i++) {
		//			serial.write(REPORT_ANALOG | i);
		//			serial.write(1);
		//		}
		//
		//		for (int i = 0; i < 2; i++) {
		//			serial.write(REPORT_DIGITAL | i);
		//			serial.write(1);
		//		}
		new Thread() {
			public void run(){
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {}

				for (int i = 0; i < 6; i++) {
					serial.write(REPORT_ANALOG | i);
					serial.write(1);
				}

				for (int i = 0; i < 2; i++) {
					serial.write(REPORT_DIGITAL | i);
					serial.write(1);
				}
			}
		}.start();
	}


	/**
	 * Returns the last known value read from the digital pin: HIGH or LOW.
	 *
	 * @param pin the digital pin whose value should be returned (from 2 to 13,
	 * since pins 0 and 1 are used for serial communication)
	 */
	@Override
	public int digitalRead(int pin) {
		return (digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01;
	}

	/**
	 * Returns the last known value read from the analog pin: 0 (0 volts) to
	 * 1023 (5 volts).
	 *
	 * @param pin the analog pin whose value should be returned (from 0 to 5)
	 */
	@Override
	public int analogRead(int pin) {
		return analogInputData[pin];
	}

	/**
	 * Set a digital pin to input or output mode.
	 *
	 * @param pin the pin whose mode to set (from 2 to 13)
	 * @param mode either Arduino.INPUT or Arduino.OUTPUT
	 */
	@Override
	public void pinMode(int pin, int mode) {
		serial.write(SET_PIN_MODE);
		serial.write(pin);
		serial.write(mode);
	}

	/**
	 * Write to a digital pin (the pin must have been put into output mode with
	 * pinMode()).
	 *
	 * @param pin the pin to write to (from 2 to 13)
	 * @param value the value to write: Arduino.LOW (0 volts) or Arduino.HIGH
	 * (5 volts)
	 */
	@Override
	public void digitalWrite(int pin, int value) {
		int portNumber = (pin >> 3) & 0x0F;

		if (value == 0)
			digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
		else
			digitalOutputData[portNumber] |= (1 << (pin & 0x07));

		serial.write(DIGITAL_MESSAGE | portNumber);
		serial.write(digitalOutputData[portNumber] & 0x7F);
		serial.write(digitalOutputData[portNumber] >> 7);
	}

	/**
	 * Write an analog value (PWM-wave) to a digital pin.
	 *
	 * @param pin the pin to write to (must be 9, 10, or 11, as those are they
	 * only ones which support hardware pwm)
	 * @param the value: 0 being the lowest (always off), and 255 the highest
	 * (always on)
	 */
	@Override
	public void analogWrite(int pin, int value) {
		pinMode(pin, PWM);
		serial.write(ANALOG_MESSAGE | (pin & 0x0F));
		serial.write(value & 0x7F);
		serial.write(value >> 7);
	}

	private void setDigitalInputs(int portNumber, int portData) {
		//System.out.println("digital port " + portNumber + " is " + portData);
		digitalInputData[portNumber] = portData;
	}

	private void setAnalogInput(int pin, int value) {
		//System.out.println("analog pin " + pin + " is " + value);
		analogInputData[pin] = value;
	}

	private void setVersion(int majorVersion, int minorVersion) {
		//System.out.println("version is " + majorVersion + "." + minorVersion);
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}


	protected void processInput() {
		int inputData = serial.read();
		int command;

		if (parsingSysex) {
			if (inputData == END_SYSEX) {
				parsingSysex = false;
				//processSysexMessage();
			} else {
				storedInputData[sysexBytesRead] = inputData;
				sysexBytesRead++;
			}
		} else if (waitForData > 0 && inputData < 128) {
			waitForData--;
			storedInputData[waitForData] = inputData;

			if (executeMultiByteCommand != 0 && waitForData == 0) {
				//we got everything
				switch(executeMultiByteCommand) {
				case DIGITAL_MESSAGE:
					setDigitalInputs(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
					break;
				case ANALOG_MESSAGE:
					setAnalogInput(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
					break;
				case REPORT_VERSION:
					setVersion(storedInputData[1], storedInputData[0]);
					break;
				}
			}
		} else {
			if(inputData < 0xF0) {
				command = inputData & 0xF0;
				multiByteChannel = inputData & 0x0F;
			} else {
				command = inputData;
				// commands in the 0xF* range don't use channel data
			}
			switch (command) {
			case DIGITAL_MESSAGE:
			case ANALOG_MESSAGE:
			case REPORT_VERSION:
				waitForData = 2;
				executeMultiByteCommand = command;
				break;      
			}
		}
	}
}
