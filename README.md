# Allsee screen control Java Based

## Table of Contents
1. [Shutdown Screen](#shutdown-screen)
2. [Reboot Screen](#reboot-screen)
3. [Set Screen Brightness](#set-screen-brightness)
4. [Get Screen Brightness](#get-screen-brightness)
5. [Set System Input and Output Source](#set-system-input-output-source)
7. [Kill Application](#kill-application)
8. [Set System Time](#set-system-time)
9. [Navigation Bar UI Switch](#navigation-bar-UI-switch)
10. [Get Screen Screenshot](#get-screen-screenshot)
11. [Set Volume](#set-volume)
12. [Get Volume](#get-volume)
13. [Get Screen Power Status](#get-screen-power-status)
14. [Install Other Applications](#install-other-applications)
---

## Shutdown Screen

- **Broadcast message**:  
  `com.assist.sleep.timeonoff`

---

### **Parameters**

| **Parameter Name** | **Description**                                                                                                                    | **Notes**                       |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| **onTime**          | Time in seconds (S) before the device turns on again. Effective for real shutdown only. The value must be no less than 60 seconds. | Example: `120` for 2 minutes   |
| **sleepType**       | Type of shutdown mode.                                                                                                             | Default: `2`                   |
|                     | `1`: shutdown mode                                                                                                                 |                                 |
|                     | `2`: sleep mode                                                                                                                    |                                 |

### **Example Usage**

#### **Shutdown**

```java
Intent intent = new Intent("com.assist.sleep.timeonoff");
intent.putExtra("sleepType", 1);   
context.sendBroadcast(intent);   
```

#### **Sleep**

```java
Intent intent = new Intent("com.assist.sleep.timeonoff");
intent.putExtra("sleepType", 2);  
context.sendBroadcast(intent);   
```

#### **Shutdown then turn on again**

```java
Intent intent = new Intent("com.assist.sleep.timeonoff");
intent.putExtra("sleepType", 1);   
intent.putExtra("onTime", 120);  // The screen will be shutdown then turns on after 120 seconds (2 minutes) automatically. 
context.sendBroadcast(intent);   
```

## Reboot Screen

- **Broadcast message**:  
  `com.assist.reboot.action`

---

### **Parameters**

| **Parameter Name** | **Description**                   | **Notes** |
|---------------------|-----------------------------------|-----------|
| **None**            | This interface takes no parameters. |           |


### **Example Usage**

#### **Reboot**

```java
Intent intent = new Intent("com.assist.reboot.action");  
context.sendBroadcast(intent);  
```

## Set Screen Brightness

- **Broadcast message**:  
  `com.assist.set.light`

---

### **Parameters**

| **Parameter Name** | **Description**         | **Notes**           |
|---------------------|-------------------------|---------------------|
| **light**           | Brightness value (0–100) | Example: `50` for medium brightness |

### **Example Usage**

#### **Set Brightness**

```java
Intent intent = new Intent("com.assist.set.light");  
intent.putExtra("light", 50);  // Sets brightness to 50%
context.sendBroadcast(intent);  
```

## Get Screen Brightness

- **Broadcast message**  
  `com.assist.get.light`

---

### **Parameters**

| **Parameter Name** | **Description**                       | **Notes**                                                                                       |
|---------------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **No parameters**   | No parameters are needed for this action | The intent simply requests the current brightness level without needing additional parameters.  |

### **Example Usage**

#### **Get the current brightness**

```java
Intent intent = new Intent("com.assist.get.light");  
context.sendBroadcast(intent);  // Requests the current system brightness  

// Register receiver to receive the value from system
registerReceiver(brightnessReceiver, new IntentFilter("com.assist.notify.light"));
private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the brightness value from the intent extras
        int brightness = intent.getIntExtra("light", -1);
        if (brightness != -1) {
            codeDisplay.append("\n\nReceived Brightness: " + brightness);
        } else {
            codeDisplay.append("\n\nError: Failed to receive brightness.");
        }
    }
};
@Override
protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(brightnessReceiver);
}
```

## Set System Input and Output Source

- **Broadcast message**:  
  `com.assist.set.port`

---

### **Parameters**

| **Parameter Name** | **Description**                     | **Notes**                           |
|---------------------|-------------------------------------|-------------------------------------|
| **port**            | Input/Output port                  | HDMI: hdmi input, VGA: vga input, Digital: Android system output |

### **Example Usage**

#### **Set Input as HDMI**

```java
Intent intent = new Intent("com.assist.set.port");  
intent.putExtra("port", "HDMI");  // Sets the input port to HDMI input  
context.sendBroadcast(intent);  
```

#### **Set Output as Android system**

```java
Intent intent = new Intent("com.assist.set.port");  
intent.putExtra("port", "Digital");  // Sets the output port to Android system
context.sendBroadcast(intent);  
```

## Kill Application

- **Broadcast message**  
  `com.assist.kill.app.action`

---

### **Parameters**

| **Parameter Name** | **Description**                       | **Notes**                                                                                       |
|---------------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **packageName**      | The package name of the third-party app | The name of the app's package to be terminated (e.g., "com.example.app")                          |

### **Example Usage**

#### **Kill a third-party app**

```java
Intent intent = new Intent("com.assist.kill.app.action");  
intent.putExtra("packageName", "xxx");  // Replace "xxx" with the app's package name  
context.sendBroadcast(intent);  
```

## Set System Time

- **Broadcast message**  
  `com.assist.settime.action`

---

### **Parameters**

| **Parameter Name** | **Description**                       | **Notes**                                                                                             |
|---------------------|---------------------------------------|-------------------------------------------------------------------------------------------------------|
| **msec**             | System time to be set                 | The system time in milliseconds (e.g., 1733398713118L represents the desired time in milliseconds - December 5, 2024, at 11:38:33.118 AM (UTC)) |

### **Example Usage**

#### **Set the system time**

```java
Intent intent = new Intent("com.assist.settime.action");  
intent.putExtra("msec", 1733398713118L);  // Sets the system time to 1733398713118L milliseconds  (The given timestamp 1733398713118L milliseconds corresponds to December 5, 2024, at 11:38:33.118 AM (UTC). )
context.sendBroadcast(intent);  
```

## Navigation Bar UI Switch

- **Broadcast message**  
  `com.assist.switch.navigation.action`

---

### **Parameters**

| **Parameter Name** | **Description**                       | **Notes**                                                                                       |
|---------------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **state**            | Navigation bar visibility            | 0: Show the navigation bar, 1: Hide the navigation bar                                          |

### **Example Usage**

#### **Show the navigation bar**

```java
Intent intent = new Intent("com.assist.switch.navigation.action");  
intent.putExtra("state", 0);  // Shows the navigation bar  
context.sendBroadcast(intent);  
```

## Get Screen Screenshot

- **Broadcast message**  
  `com.assist.screencap.action`

---

### **Parameters**

| **Parameter Name**   | **Description**                       | **Notes**                                                                                       |
|----------------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **screen_hdmi_path**  | Screenshot file storage path         | The file path where the screenshot will be stored (e.g., "path/to/screenshot.png")              |

### **Example Usage**

#### **Capture the screen and save to a file**

```java
Intent intent = new Intent("com.assist.screencap.action");  
intent.putExtra("screen_hdmi_path", "xxx");  // Replace "xxx" with the desired file path for the screenshot  
context.sendBroadcast(intent);  
```

## Set Volume

- **Broadcast message**  
  `com.assist.set.volume`

---

### **Parameters**

| **Parameter Name** | **Description**                       | **Notes**                                                                                       |
|---------------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **volume**          | Volume level                          | The volume level to be set (e.g., 50 for 50% volume). The range is typically 0–100.             |

### **Example Usage**

#### **Set the volume to 50**

```java
Intent intent = new Intent("com.assist.set.volume");  
intent.putExtra("volume", 50);  // Sets the volume to 50%  
context.sendBroadcast(intent);  
```

## Get Volume

- **Broadcast message**  
  `com.assist.get.volume`

---

### **Parameters**

| **Parameter Name** | **Description**                       | **Notes**                                                                                       |
|---------------------|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **volume**          | Request to get the current volume     | Set to `true` to request the current system volume level.                                         |

### **Example Usage**

#### **Get the current volume**

```java
Intent intent = new Intent("com.assist.get.volume");  
intent.putExtra("volume", true);  // Requests the current volume level  
context.sendBroadcast(intent);

// Register receiver to receive the value from system
registerReceiver(volumeReceiver, new IntentFilter("com.assist.notify.volume"));
private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    // Get the volume value from the intent extras
    int volume = intent.getIntExtra("volume", -1);
    if (brightness != -1) {
      codeDisplay.append("\n\nReceived volume: " + volume);
    } else {
      codeDisplay.append("\n\nError: Failed to receive volume.");
    }
  }
};
@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(volumeReceiver);
}
```


## Get Screen Power Status

- **Broadcast message**  
  `com.assist.get.power.status`

---

### **Parameters**

### **Example Usage**

#### **Get the current volume**

```java
Intent intent = new Intent("com.assist.get.power.status"); 
context.sendBroadcast(intent);

// Register receiver to receive the value from system
registerReceiver(powerStatusReceiver, new IntentFilter("com.assist.notify.power.status"));
private final BroadcastReceiver powerStatusReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    // Get the power status value from the intent extras
    int status = intent.getIntExtra("status", -1);
    if (status != -1) {
      codeDisplay.append("\n\nReceived status: " + status); //0:Screen can be seen, 1: Screen is black.
    } else {
      codeDisplay.append("\n\nError: Failed to receive status.");
    }
  }
};
@Override
protected void onDestroy() {
  super.onDestroy();
  unregisterReceiver(powerStatusReceiver);
}
```

## Install Other Applications

- **Broadcast message**:  
  `com.assist.install.app.action`

---

### **Parameters**

| **Parameter Name** | **Description**                               | **Notes**                                |
|---------------------|-----------------------------------------------|------------------------------------------|
| **appFilePath**           | File location for the third party application | Example: `/mnt/sdcard/Download/xxxx.apk` |

### **Example Usage**

#### **Install xxx.apk**

```java
Intent intent = new Intent("com.assist.install.app.action");
intent.putExtra("appFilePath", "/mnt/sdcard/Download/xxxx.apk"); 
context.sendBroadcast(intent);
```

