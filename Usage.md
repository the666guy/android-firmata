### How to setup your Android app to use Firmata ###

This is an early version of android-firmata. The final result will be a library which you can import to your Android project to use its functions.

However, if you cannot wait and you want to use android-firmata right now, you might go through the sample application to see how I made use of functions provided by **BTActivity**.

### General instructions ###
Basically you extend your Activity with BTActivity.
This gives you two menu items in your Activity:
  * Bluetooth on/off
  * Scan for Devices

But **BTActivity** does much more than that. It handles all the communication and provides you some convenient methods to connect or disconnect.
**BTActivity** is abstract and you have to implement the following method:

```
@Override
public void connectionEstablished() {
  Log.d(TAG, "connected");
 
  /* 1. put your arduino setup code in here */

  /* 2. At the end of this function you might want to start a looper to fetch pin states on a regular basis */
}
```

You might use the `connect()` method of BTActivity to connect to the last successful paired device. When connection was successful BTActivity calls `connectionEstablished()` for you. This is where you put your Arduino setup code in and start a looper thread if you want to check pins periodically.