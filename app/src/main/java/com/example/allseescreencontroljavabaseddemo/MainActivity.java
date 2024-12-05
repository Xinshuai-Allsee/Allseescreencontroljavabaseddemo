package com.example.allseescreencontroljavabaseddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText codeDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Allsee Screen Control Demo");

        // Create a vertical layout
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);

        // Create an EditText to display the code
        codeDisplay = new EditText(this);
        codeDisplay.setTextSize(14);
        codeDisplay.setSingleLine(false);
        codeDisplay.setEnabled(false);  // Make it read-only
        codeDisplay.setHint("Code will be displayed here...");
        layout.addView(codeDisplay);

        registerReceiver(brightnessReceiver, new IntentFilter("com.assist.notify.light"));
        registerReceiver(volumeReceiver, new IntentFilter("com.assist.notify.volume"));
        registerReceiver(powerStatusReceiver, new IntentFilter("com.assist.notify.power.status"));
        // Add UI components for each functionality
        addButtonWithCode(layout, "Shutdown Screen", "com.assist.sleep.timeonoff",
                "Intent intent = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "intent.putExtra(\"sleepType\", 1);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("sleepType", 1), codeDisplay);

        addButtonWithCode(layout, "Sleep", "com.assist.sleep.timeonoff",
                "Intent intent = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "intent.putExtra(\"sleepType\", 2);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("sleepType", 2), codeDisplay);

        addButtonWithCode(layout, "Shutdown then turn on again", "com.assist.sleep.timeonoff",
                "Intent intent = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "intent.putExtra(\"sleepType\", 1);\n" +
                        "intent.putExtra(\"onTime\", 120);\n" +
                        "context.sendBroadcast(intent);",
                intent -> {
                    intent.putExtra("sleepType", 1);
                    intent.putExtra("onTime", 120);  // Shutdown and turn on in 2 minutes
                }, codeDisplay);

        addButtonWithCode(layout, "Reboot Screen", "com.assist.reboot.action",
                "Intent intent = new Intent(\"com.assist.reboot.action\");\n" +
                        "context.sendBroadcast(intent);",
                null, codeDisplay);

        addButtonWithCode(layout, "Set Screen Brightness to 10", "com.assist.set.light",
                "Intent intent = new Intent(\"com.assist.set.light\");\n" +
                        "intent.putExtra(\"light\", 10);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("light", 10), codeDisplay);

        addButtonWithCode(layout, "Set Screen Brightness to 100", "com.assist.set.light",
                "Intent intent = new Intent(\"com.assist.set.light\");\n" +
                        "intent.putExtra(\"light\", 100);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("light", 100), codeDisplay);

        addButtonWithCode(layout, "Get Screen Brightness", "com.assist.get.light",
                "Intent intent = new Intent(\"com.assist.get.light\");\n" +
                        "context.sendBroadcast(intent);",
                null, codeDisplay);

        addButtonWithCode(layout, "Set Input/Output Source", "com.assist.set.port",
                "Intent intent = new Intent(\"com.assist.set.port\");\n" +
                        "intent.putExtra(\"port\", \"HDMI\");\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("port", "HDMI"), codeDisplay);

        addButtonWithCode(layout, "Kill Application", "com.assist.kill.app.action",
                "Intent intent = new Intent(\"com.assist.kill.app.action\");\n" +
                        "intent.putExtra(\"packageName\", \"com.example.allseescreencontroljavabaseddemo\");\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("packageName", "com.example.allseescreencontroljavabaseddemo"), codeDisplay);

        addButtonWithCode(layout, "Install other Applications", "com.assist.install.app.action",
                "Intent intent = new Intent(\"com.assist.install.app.action\");\n" +
                        "intent.putExtra(\"appFilePath\", \"/mnt/sdcard/Download/hahahaha.apk\");\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("appFilePath", "/mnt/sdcard/Download/hahahaha.apk"), codeDisplay);

        addButtonWithCode(layout, "Set System Time", "com.assist.settime.action",
                "Intent intent = new Intent(\"com.assist.settime.action\");\n" +
                        "intent.putExtra(\"msec\", 1733398713118L);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("msec", 1733398713118L), codeDisplay);

        addButtonWithCode(layout, "Hide Navigation Bar", "com.assist.switch.navigation.action",
                "Intent intent = new Intent(\"com.assist.switch.navigation.action\");\n" +
                        "intent.putExtra(\"state\", 1);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("state", 1), codeDisplay);

        addButtonWithCode(layout, "Show Navigation Bar", "com.assist.switch.navigation.action",
                "Intent intent = new Intent(\"com.assist.switch.navigation.action\");\n" +
                        "intent.putExtra(\"state\", 0);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("state", 0), codeDisplay);

        addButtonWithCode(layout, "Get Screen Screenshot and the image will be saved as /mnt/sdcard/Download/screenshot.png", "com.assist.screencap.action",
                "Intent intent = new Intent(\"com.assist.screencap.action\");\n" +
                        "intent.putExtra(\"screen_hdmi_path\", \"/mnt/sdcard/Download/screenshot.png\");\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("screen_hdmi_path", "/mnt/sdcard/Download/screenshot.png"), codeDisplay);

        addButtonWithCode(layout, "Set Volume to 0", "com.assist.set.volume",
                "Intent intent = new Intent(\"com.assist.set.volume\");\n" +
                        "intent.putExtra(\"volume\", 0);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("volume", 0), codeDisplay);

        addButtonWithCode(layout, "Set Volume to 50", "com.assist.set.volume",
                "Intent intent = new Intent(\"com.assist.set.volume\");\n" +
                        "intent.putExtra(\"volume\", 50);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("volume", 50), codeDisplay);

        addButtonWithCode(layout, "Get Volume", "com.assist.get.volume",
                "Intent intent = new Intent(\"com.assist.get.volume\");\n" +
                        "intent.putExtra(\"volume\", true);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra("volume", true), codeDisplay);

        addButtonWithCode(layout, "Get Power status", "com.assist.get.power.status",
                "Intent intent = new Intent(\"com.assist.get.power.status\");\n" +
                        "context.sendBroadcast(intent);",
                null, codeDisplay);


        setContentView(scrollView);
    }

    private void addButtonWithCode(LinearLayout layout, String buttonText, String action,
                                   String codeText, BroadcastModifier modifier, EditText codeDisplay) {
        Button button = new Button(this);
        button.setText(buttonText);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(action);
            if (modifier != null) modifier.modify(intent);
            sendBroadcast(intent);
            codeDisplay.setText(codeText);
        });
        layout.addView(button);
    }

    interface BroadcastModifier {
        void modify(Intent intent);
    }

    // Register receiver to receive the value from system

    private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the brightness value from the intent extras
            Log.d("brightnessReceiver", "Received broadcast: " + intent.getAction());
            if (intent.getExtras() != null) {
                for (String key : intent.getExtras().keySet()) {
                    Log.d("brightnessReceiver", "Key: " + key + ", Value: " + intent.getExtras().get(key));
                }
            }
            int brightness = intent.getIntExtra("light", -1);
            if (brightness != -1) {
                codeDisplay.setText("Received Brightness: " + brightness);
            } else {
                codeDisplay.setText("Error: Failed to receive brightness.");
            }
        }
    };

    private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the volumeReceiver value from the intent extras
            Log.d("volumeReceiver", "Received broadcast: " + intent.getAction());
            if (intent.getExtras() != null) {
                for (String key : intent.getExtras().keySet()) {
                    Log.d("volumeReceiver", "Key: " + key + ", Value: " + intent.getExtras().get(key));
                }
            }
            // Get the volume value from the intent extras
            int volume = intent.getIntExtra("volume", -1);
            if (volume != -1) {
                codeDisplay.setText("Received volume: " + volume);
            } else {
                codeDisplay.setText("Error: Failed to receive volume.");
            }
        }
    };

    private final BroadcastReceiver powerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the powerStatusReceiver value from the intent extras
            Log.d("powerStatusReceiver", "Received broadcast: " + intent.getAction());
            if (intent.getExtras() != null) {
                for (String key : intent.getExtras().keySet()) {
                    Log.d("powerStatusReceiver", "Key: " + key + ", Value: " + intent.getExtras().get(key));
                }
            }
            // Get the power status value from the intent extras
            int status = intent.getIntExtra("status", -1);
            if (status != -1) {
                codeDisplay.setText("Received status: " + status); //0:Screen can be seen, 1: Screen is black.
            } else {
                codeDisplay.setText("Error: Failed to receive status.");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brightnessReceiver);
        unregisterReceiver(volumeReceiver);
        unregisterReceiver(powerStatusReceiver);
    }

}
