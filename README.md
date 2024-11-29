# Allsee Screen Control 

## Table of Contents

* [Toggle USB Power](#toggle-usb-power)
* [Get USB Power State](#get-usb-power-state)
* [Control Power State (Shutdown/Sleep/Wake)](#control-power-state-shutdownsleepwake)
* [Reboot Screen](#reboot-screen)
* [Set Screen Brightness](#set-screen-brightness)
* [Set Input/Output Source](#set-inputoutput-source)
* [Kill Application](#kill-application)
* [Set System Time](#set-system-time)
* [Set Navigation Bar Visibility](#set-navigation-bar-visibility)
* [Get Screen Screenshot](#get-screen-screenshot)
* [Set Volume](#set-volume)
* [Install Application](#install-application)
* [Set Screen Orientation](#set-screen-orientation)
* [Set IR Remote State](#set-ir-remote-state)
* [Set IR Home Key State](#set-ir-home-key-state)
* [Set Touch Screen State](#set-touch-screen-state)
* [Set Audio Output Channel](#set-audio-output-channel)
* [Scaler Logo Style (Set/Get)](#scaler-logo-style-setget)

---

## Toggle USB Power

* **Broadcast message**: `com.assist.set.usbpower`

---

### **Parameters**

| **Parameter Name** | **Description**                  | **Notes** |
| ------------------ |----------------------------------| --------- |
| `usbPower`         | 0: Off, 1: On                    |           |
| `usbNumber`        | 0: First USB, 1: Second USB, etc |           |

### **Example Usage**

#### **Turn USB Power On**

```java
Intent intent = new Intent("com.assist.set.usbpower");
intent.putExtra("usbNumber", 0);
intent.putExtra("usbPower", 1);
context.sendBroadcast(intent);
```

#### **Turn USB Power Off**

```java
Intent intent = new Intent("com.assist.set.usbpower");
intent.putExtra("usbPower", 0);
intent.putExtra("usbPower", 0);
context.sendBroadcast(intent);
```

---

## Get USB Power State

* **Request broadcast**: `com.assist.get.usbpower`
* **Response broadcast**: `com.assist.notify.usbpower`

### **Parameters**

**Request**

| **Parameter Name** | **Description**                                                                                                          |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------ |
| `usbNumber`        | USB index to query. `0`: First USB, `1`: Second USB, … (the order matches **Other Settings → USB switch** on the device) |

**Response**

| **Parameter Name** | **Description**                    |
| ------------------ | ---------------------------------- |
| `usbPower`         | USB power state. `0`: Off, `1`: On |

### **Example Usage**

```java
// Ask for the current power state of USB #1 (index 1)
Intent req = new Intent("com.assist.get.usbpower");
req.putExtra("usbNumber", 1);
context.sendBroadcast(req);

// Listen for the answer
registerReceiver(usbPowerReceiver, new IntentFilter("com.assist.notify.usbpower"));

private final BroadcastReceiver usbPowerReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int usbPower = intent.getIntExtra("usbPower", -1); // 0: Off, 1: On
    if (usbPower != -1) {
      Log.d("UsbPower", "USB power state = " + usbPower);
    } else {
      Log.e("UsbPower", "Failed to read usbPower.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(usbPowerReceiver);
}
```

---

## Control Power State (Shutdown/Sleep/Wake)

* **Broadcast message**: `com.assist.sleep.timeonoff`

---

### **Parameters**

| **Parameter Name** | **Description**                                                                                                                    | **Notes**                                      |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| `onTime`           | Time in seconds (s) before the device turns on again. Effective for real shutdown only. The value must be no less than 60 seconds. | Example: `120` for 2 minutes. Default is `60`. |
| `sleepType`        | `1`: Shutdown mode<br>`2`: Sleep mode<br>`3`: Wake up the screen                                                                   | Default: `2`.                                  |

### **Example Usage**

#### **Enter Sleep Mode**

```java
Intent intent = new Intent("com.assist.sleep.timeonoff");
intent.putExtra("sleepType", 2);  
context.sendBroadcast(intent);   
```

#### **Wake Up Screen**

```java
Intent intent = new Intent("com.assist.sleep.timeonoff");
intent.putExtra("sleepType", 3);  
context.sendBroadcast(intent);   
```

#### **Shutdown with Automatic Power-On**

```java
Intent intent = new Intent("com.assist.sleep.timeonoff");
intent.putExtra("sleepType", 1);   
// The screen will shut down then turn on automatically after 120 seconds (2 minutes). 
intent.putExtra("onTime", 120);  
context.sendBroadcast(intent);   
```

#### **Get Current Power Status**

```java
// Request the current power status
Intent intent = new Intent("com.assist.get.power.status"); 
context.sendBroadcast(intent);

// Register a receiver to get the status from the system
registerReceiver(powerStatusReceiver, new IntentFilter("com.assist.notify.power.status"));

private final BroadcastReceiver powerStatusReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    // 0: Screen on (visible), 1: Screen off (black)
    int status = intent.getIntExtra("status", -1); 
    if (status != -1) {
      Log.d("PowerStatus", "Received status: " + status);
    } else {
      Log.e("PowerStatus", "Error: Failed to receive status.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(powerStatusReceiver);
}
```

---

## Reboot Screen

* **Broadcast message**: `com.assist.reboot.action`

---

### **Parameters**

This broadcast takes no parameters.

### **Example Usage**

#### **Reboot the Device**

```java
Intent intent = new Intent("com.assist.reboot.action");  
context.sendBroadcast(intent);  
```

---

## Set Screen Brightness

* **Broadcast message**: `com.assist.set.light`

---

### **Parameters**

| **Parameter Name** | **Description**          | **Notes**                         |
| ------------------ | ------------------------ | --------------------------------- |
| `light`            | Brightness value (0–100) | Example: `50` for 50% brightness. |

### **Example Usage**

#### **Set Brightness**

```java
Intent intent = new Intent("com.assist.set.light");  
intent.putExtra("light", 50);  // Sets brightness to 50%
context.sendBroadcast(intent);  
```

#### **Get Current Brightness**

```java
// Request the current system brightness
Intent intent = new Intent("com.assist.get.light");  
context.sendBroadcast(intent);  

// Register a receiver to get the value from the system
registerReceiver(brightnessReceiver, new IntentFilter("com.assist.notify.light"));

private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        int brightness = intent.getIntExtra("light", -1);
        if (brightness != -1) {
            Log.d("Brightness", "Received Brightness: " + brightness);
        } else {
            Log.e("Brightness", "Error: Failed to receive brightness.");
        }
    }
};

@Override
protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(brightnessReceiver);
}
```

---

## Set Input/Output Source

* **Broadcast message**: `com.assist.set.port`

---

### **Parameters**

| **Parameter Name** | **Description**                                                                  |
| ------------------ | -------------------------------------------------------------------------------- |
| `port`             | `"HDMI"`: HDMI input<br>`"VGA"`: VGA input<br>`"Digital"`: Android system output |

### **Example Usage**

#### **Set Input to HDMI**

```java
Intent intent = new Intent("com.assist.set.port");  
intent.putExtra("port", "HDMI");  
context.sendBroadcast(intent);  
```

#### **Set Output to Android System**

```java
Intent intent = new Intent("com.assist.set.port");  
intent.putExtra("port", "Digital");
context.sendBroadcast(intent);  
```

#### **Get Current Input/Output Source**

```java
// Request the current Input/Output source
Intent intent = new Intent("com.assist.get.port");
context.sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(portReceiver, new IntentFilter("com.assist.notify.port"), Context.RECEIVER_NOT_EXPORTED);

private final BroadcastReceiver portReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int port = intent.getIntExtra("port", -1);
    if (port != -1) {
      // Note: The integer value needs to be mapped to the corresponding source name.
      Log.d("PortReceiver", "Received port value: " + port);
    } else {
      Log.e("PortReceiver", "Error: Failed to receive port value.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(portReceiver);
}
```

---

## Kill Application

* **Broadcast message**: `com.assist.kill.app.action`

---

### **Parameters**

| **Parameter Name** | **Description**                      |
| ------------------ | ------------------------------------ |
| `packageName`      | The package name of the app to kill. |

### **Example Usage**

#### **Kill a Third-Party App**

```java
Intent intent = new Intent("com.assist.kill.app.action");  
// Replace with the actual package name
intent.putExtra("packageName", "com.example.app");  
context.sendBroadcast(intent);  
```

---

## Set System Time

* **Broadcast message**: `com.assist.settime.action`

---

### **Parameters**

| **Parameter Name** | **Description**                                         |
| ------------------ | ------------------------------------------------------- |
| `msec`             | The system time in milliseconds since the epoch (long). |

### **Example Usage**

#### **Set the System Time**

```java
Intent intent = new Intent("com.assist.settime.action");  
// Example: December 5, 2024, at 11:38:33.118 AM (UTC)
long timestamp = 1733398713118L;
intent.putExtra("msec", timestamp);
context.sendBroadcast(intent);  
```

---

## Set Navigation Bar Visibility

* **Broadcast message**: `com.assist.switch.navigation.action`

---

### **Parameters**

| **Parameter Name** | **Description**                                      |
| ------------------ | ---------------------------------------------------- |
| `state`            | `0`: Show navigation bar<br>`1`: Hide navigation bar |

### **Example Usage**

#### **Show Navigation Bar**

```java
Intent intent = new Intent("com.assist.switch.navigation.action");  
intent.putExtra("state", 0);
context.sendBroadcast(intent);  
```

#### **Hide Navigation Bar**

```java
Intent intent = new Intent("com.assist.switch.navigation.action");  
intent.putExtra("state", 1);
context.sendBroadcast(intent);  
```

---

## Get Screen Screenshot

* **Broadcast message**: `com.assist.screencap.action`

---

### **Parameters**

| **Parameter Name** | **Description**                            |
| ------------------ | ------------------------------------------ |
| `screen_hdmi_path` | Full file path to save the PNG screenshot. |

### **Example Usage**

#### **Capture Screen to a File**

```java
Intent intent = new Intent("com.assist.screencap.action");
// Example path, ensure your app has storage permissions
String filePath = "/storage/emulated/0/Pictures/screenshot.png";
intent.putExtra("screen_hdmi_path", filePath);
context.sendBroadcast(intent);  
```

---

## Set Volume

* **Broadcast message**: `com.assist.set.volume`

---

### **Parameters**

| **Parameter Name** | **Description**              |
| ------------------ | ---------------------------- |
| `volume`           | Volume level to set (0-100). |
| `mute`             | `0`: unmute, `1`: mute       |

### **Example Usage**

#### **Set Volume**

```java
Intent intent = new Intent("com.assist.set.volume");  
intent.putExtra("volume", 50);  // Sets the volume to 50%
context.sendBroadcast(intent);  
```

#### **Mute**

```java
Intent intent = new Intent("com.assist.set.volume");  
intent.putExtra("mute", 1);  
context.sendBroadcast(intent);  
```

#### **Unmute**

```java
Intent intent = new Intent("com.assist.set.volume");  
intent.putExtra("mute", 0);  
context.sendBroadcast(intent);  
```

#### **Get Current Volume**

```java
// Request the current volume level  
Intent intent = new Intent("com.assist.get.volume");
intent.putExtra("volume", true);  // request flag
intent.putExtra("mute", true);    // request flag
sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(volumeReceiver, new IntentFilter("com.assist.notify.volume"));

private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int volume = intent.getIntExtra("volume", -1);
    if (volume != -1) {
      Log.d("Volume", "Received volume: " + volume);
    } else {
      Log.e("Volume", "Error: Failed to receive volume.");
    }
    int mute = intent.getIntExtra("mute", -1);
    if (mute != -1) {
      Log.d("Mute", "Received mute: " + mute); // 0:unmute 1:mute
    } else {
      Log.e("Mute", "Error: Failed to receive mute.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(volumeReceiver);
}
```

---

## Install Application

* **Broadcast message**: `com.assist.install.app.action`

---

### **Parameters**

| **Parameter Name** | **Description**                       |
| ------------------ | ------------------------------------- |
| `appFilePath`      | Full file path of the APK to install. |
| `packageName`      | The package name of the application.  |

### **Example Usage**

#### **Install an Application**

```java
Intent intent = new Intent("com.assist.install.app.action");
// Replace with actual package name and file path
intent.putExtra("packageName","com.example.app");
intent.putExtra("appFilePath","/storage/emulated/0/Download/example.apk");
context.sendBroadcast(intent);
```

---

## Set Screen Orientation

* **Broadcast message**: `com.assist.set.system.orientation`

---

### **Parameters**

| **Parameter Name** | **Description**                                                                                  |
| ------------------ | ------------------------------------------------------------------------------------------------ |
| `orientation`      | `0`: Landscape (0°)<br>`90`: Portrait (90°)<br>`180`: Landscape (180°)<br>`270`: Portrait (270°) |

### **Example Usage**

#### **Rotate to Portrait (90°)**

```java
Intent intent = new Intent("com.assist.set.system.orientation");
intent.putExtra("orientation", 90);
context.sendBroadcast(intent);
```

#### **Rotate to Portrait (180°)**

```java
Intent intent = new Intent("com.assist.set.system.orientation");
intent.putExtra("orientation", 180);
context.sendBroadcast(intent);
```

#### **Get Current Orientation**

```java
// Request the current orientation
Intent intent = new Intent("com.assist.get.system.orientation");
context.sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(orientationReceiver, new IntentFilter("com.assist.notify.orientation"), Context.RECEIVER_NOT_EXPORTED);

private final BroadcastReceiver orientationReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int orientation = intent.getIntExtra("orientation", -1);
    if (orientation != -1) {
      Log.d("Orientation", "Received orientation: " + orientation);
    } else {
      Log.e("Orientation", "Error: Failed to receive orientation.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(orientationReceiver);
}
```

---

## Set IR Remote State

* **Broadcast message**: `com.assist.set.remote`

---

### **Parameters**

| **Parameter Name** | **Description**                                                                                                              |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| `state`            | `0`: Enable IR remote.<br>`1`: Disable IR remote (power button can still wake device).<br>`2`: Disable IR remote completely. |

### **Example Usage**

#### **Enable IR Remote**

```java
Intent intent = new Intent("com.assist.set.remote");
intent.putExtra("state", 0);
context.sendBroadcast(intent);
```

#### **Get Current IR Remote State**

```java
// Request the current IR remote state
Intent intent = new Intent("com.assist.get.remote");
context.sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(irReceiver, new IntentFilter("com.assist.notify.remote"), Context.RECEIVER_NOT_EXPORTED);

private final BroadcastReceiver irReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int state = intent.getIntExtra("state", -1);
    if (state != -1) {
      Log.d("IRRemote", "Received IR remote state: " + state);
    } else {
      Log.e("IRRemote", "Error: Failed to receive IR remote state.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(irReceiver);
}
```

---

## Set IR Home Key State

* **Broadcast message**: `com.assist.sethome.action`

---

### **Parameters**

| **Parameter Name** | **Description**               |
| ------------------ | ----------------------------- |
| `state`            | `0`: Enabled<br>`1`: Disabled |

### **Example Usage**

#### **Enable IR Home Key**

```java
Intent intent = new Intent("com.assist.sethome.action");
intent.putExtra("state", 0);
context.sendBroadcast(intent);
```

#### **Get Current IR Home Key State**

```java
// Request the current IR Home key state
Intent intent = new Intent("com.assist.gethome.action");
context.sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(irHomeReceiver, new IntentFilter("com.assist.notify.homekey.disable"), Context.RECEIVER_NOT_EXPORTED);

private final BroadcastReceiver irHomeReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int state = intent.getIntExtra("state", -1);
    if (state != -1) {
      Log.d("IRHome", "Received IR Home key state: " + state);
    } else {
      Log.e("IRHome", "Error: Failed to receive IR Home key state.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(irHomeReceiver);
}
```

---

## Set Touch Screen State

* **Broadcast message**: `com.assist.set.touch`

---

### **Parameters**

| **Parameter Name** | **Description**                           |
| ------------------ | ----------------------------------------- |
| `state`            | `0`: Touch enabled<br>`1`: Touch disabled |

### **Example Usage**

#### **Disable Touch Screen**

```java
Intent intent = new Intent("com.assist.set.touch");
intent.putExtra("state", 1);
context.sendBroadcast(intent);
```

#### **Get Current Touch Screen State**

```java
// Request the current touch screen state
Intent intent = new Intent("com.assist.get.touch");
context.sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(touchReceiver, new IntentFilter("com.assist.notify.touch"), Context.RECEIVER_NOT_EXPORTED);

private final BroadcastReceiver touchReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int state = intent.getIntExtra("state", -1);
    if (state != -1) {
      Log.d("Touch", "Received touch state: " + state);
    } else {
      Log.e("Touch", "Error: Failed to receive touch state.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(touchReceiver);
}
```

---

## Set Audio Output Channel

* **Broadcast message**: `com.assist.set.audio.channel`

---

### **Parameters**

| **Parameter Name** | **Description**                                                                                   |
| ------------------ | ------------------------------------------------------------------------------------------------- |
| `mode`             | `0`: Auto (priority: **Bluetooth > Internal speaker**)<br>`1`: Bluetooth<br>`2`: Internal speaker |

> **Note:** After setting the channel, the device will **auto-reboot \~2 seconds** later.

### **Example Usage**

#### **Auto (Bluetooth > Speaker)**

```java
Intent intent = new Intent("com.assist.set.audio.channel");
intent.putExtra("mode", 0);
context.sendBroadcast(intent);
```

#### **Force Bluetooth**

```java
Intent intent = new Intent("com.assist.set.audio.channel");
intent.putExtra("mode", 1);
context.sendBroadcast(intent);
```

#### **Force Internal Speaker**

```java
Intent intent = new Intent("com.assist.set.audio.channel");
intent.putExtra("mode", 2);
context.sendBroadcast(intent);
```

#### **Get Current Audio Output Channel**

```java
// Request the current audio output channel
Intent intent = new Intent("com.assist.get.audio.channel");
context.sendBroadcast(intent);

// Register a receiver to get the value from the system
registerReceiver(audioChannelReceiver, new IntentFilter("com.assist.notify.audio.channel"), Context.RECEIVER_NOT_EXPORTED);

private final BroadcastReceiver audioChannelReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
      int mode = intent.getIntExtra("mode", -1);
      if (mode != -1) {
          String humanReadable;
          switch (mode) {
              case 0: humanReadable = "Auto (Bluetooth > Speaker)"; break;
              case 1: humanReadable = "Bluetooth"; break;
              case 2: humanReadable = "Internal speaker"; break;
              default: humanReadable = "Unknown (" + mode + ")";
          }
          Log.d("AudioChannel", "Current audio channel: " + humanReadable + " [" + mode + "]");
      } else {
          Log.e("AudioChannel", "Error: Failed to receive audio channel.");
      }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(audioChannelReceiver);
}
```

---

## Scaler Logo Style (Set/Get)

### Set Scaler Logo Style

* **Broadcast message**: `com.assist.set.logostyle`

#### **Parameters**

| **Parameter Name** | **Description**                   |
| ------------------ |-----------------------------------|
| `logoStyle`        | `0`: Android, `1`: X, `2`: Videri |

#### **Example Usage**

```java
Intent intent = new Intent("com.assist.set.logostyle");
intent.putExtra("logoStyle", 0); // 0: Android, 1: X, 2: Videri
context.sendBroadcast(intent);
```

### Get Scaler Logo Style

* **Request broadcast**: `com.assist.get.logostyle`
* **Response broadcast**: `com.assist.notify.logostyle`

#### **Response Parameter**

| **Parameter Name** | **Description**                   |
| ------------------ |-----------------------------------|
| `logoStyle`        | `0`: Android, `1`: X, `2`: Videri |

#### **Example Usage**

```java
// Ask for the current scaler logo style
Intent req = new Intent("com.assist.get.logostyle");
context.sendBroadcast(req);

// Listen for the answer
registerReceiver(logoStyleReceiver, new IntentFilter("com.assist.notify.logostyle"));

private final BroadcastReceiver logoStyleReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    int style = intent.getIntExtra("logoStyle", -1);
    if (style != -1) {
      String name = (style == 0) ? "Android" : (style == 1) ? "X" : (style == 2) ? "Videri" : ("Unknown(" + style + ")");
      Log.d("LogoStyle", "Current scaler logo style: " + name + " [" + style + "]");
    } else {
      Log.e("LogoStyle", "Failed to receive logoStyle.");
    }
  }
};

@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(logoStyleReceiver);
}
```

---
