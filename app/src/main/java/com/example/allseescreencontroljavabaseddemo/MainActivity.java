package com.example.allseescreencontroljavabaseddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Centralized constants for all broadcast actions and intent extras.
     * This prevents typos and makes maintenance easier.
     */
    private static final class Constants {
        // Actions for Setting/Toggling
        static final String ACTION_SET_USB_POWER = "com.assist.set.usbpower";
        static final String ACTION_SET_POWER_STATE = "com.assist.sleep.timeonoff";
        static final String ACTION_REBOOT = "com.assist.reboot.action";
        static final String ACTION_SET_BRIGHTNESS = "com.assist.set.light";
        static final String ACTION_SET_PORT = "com.assist.set.port";
        static final String ACTION_KILL_APP = "com.assist.kill.app.action";
        static final String ACTION_SET_TIME = "com.assist.settime.action";
        static final String ACTION_SET_NAV_BAR = "com.assist.switch.navigation.action";
        static final String ACTION_TAKE_SCREENSHOT = "com.assist.screencap.action";
        static final String ACTION_SET_VOLUME = "com.assist.set.volume";
        static final String ACTION_INSTALL_APP = "com.assist.install.app.action";
        static final String ACTION_SET_ORIENTATION = "com.assist.set.system.orientation";
        static final String ACTION_SET_IR_REMOTE = "com.assist.set.remote";
        static final String ACTION_SET_IR_HOME_REMOTE = "com.assist.sethome.action";
        static final String ACTION_SET_TOUCH = "com.assist.set.touch";
        static final String ACTION_SET_AUDIO_CHANNEL = "com.assist.set.audio.channel";
        // Actions for Getting Status
        static final String ACTION_GET_BRIGHTNESS = "com.assist.get.light";
        static final String ACTION_GET_VOLUME = "com.assist.get.volume";
        static final String ACTION_GET_POWER_STATUS = "com.assist.get.power.status";
        static final String ACTION_GET_PORT = "com.assist.get.port";
        static final String ACTION_GET_ORIENTATION = "com.assist.get.system.orientation";
        static final String ACTION_GET_IR_REMOTE = "com.assist.get.remote";
        static final String ACTION_GET_IR_HOME_REMOTE = "com.assist.gethome.action";
        static final String ACTION_GET_TOUCH = "com.assist.get.touch";
        static final String ACTION_GET_AUDIO_CHANNEL = "com.assist.get.audio.channel";

        // Actions for Receiving Status Notifications
        static final String NOTIFY_BRIGHTNESS = "com.assist.notify.light";
        static final String NOTIFY_VOLUME = "com.assist.notify.volume";
        static final String NOTIFY_POWER_STATUS = "com.assist.notify.power.status";
        static final String NOTIFY_PORT = "com.assist.notify.port";
        static final String NOTIFY_ORIENTATION = "com.assist.notify.orientation";
        static final String NOTIFY_IR_REMOTE = "com.assist.notify.remote";
        static final String NOTIFY_TOUCH = "com.assist.notify.touch";
        static final String NOTIFY_IR_HOME = "com.assist.notify.homekey.disable";
        static final String NOTIFY_AUDIO_CHANNEL = "com.assist.notify.audio.channel";

        // Intent Extra Keys
        static final String EXTRA_MODE = "mode";
        static final String EXTRA_USB_POWER = "usbPower";
        static final String EXTRA_SLEEP_TYPE = "sleepType";
        static final String EXTRA_ON_TIME = "onTime";
        static final String EXTRA_LIGHT = "light";
        static final String EXTRA_PORT = "port";
        static final String EXTRA_PACKAGE_NAME = "packageName";
        static final String EXTRA_APP_FILE_PATH = "appFilePath";
        static final String EXTRA_MSEC = "msec";
        static final String EXTRA_STATE = "state";
        static final String EXTRA_SCREENSHOT_PATH = "screen_hdmi_path";
        static final String EXTRA_VOLUME = "volume";
        static final String EXTRA_ORIENTATION = "orientation";
    }

    /**
     * A simple data class to hold all information needed to create a UI button for an API command.
     */
    private static class ApiCommand {
        final String buttonText;
        final String action;
        final String codeSample;
        final BroadcastModifier modifier;

        ApiCommand(String buttonText, String action, String codeSample, @Nullable BroadcastModifier modifier) {
            this.buttonText = buttonText;
            this.action = action;
            this.codeSample = codeSample;
            this.modifier = modifier;
        }
    }

    // A functional interface to modify an Intent before it's broadcast.
    interface BroadcastModifier {
        void modify(Intent intent);
    }

    private EditText codeDisplay;
    private final List<BroadcastReceiver> registeredReceivers = new ArrayList<>();

    // --- Android Lifecycle Methods ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Allsee Screen Control Demo");

        setupUI();
        registerApiReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister all receivers to prevent memory leaks
        for (BroadcastReceiver receiver : registeredReceivers) {
            unregisterReceiver(receiver);
        }
        registeredReceivers.clear();
    }

    // --- Setup Methods ---

    /**
     * Initializes and configures the user interface.
     */
    private void setupUI() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        scrollView.addView(layout);

        codeDisplay = new EditText(this);
        codeDisplay.setTextSize(14);
        codeDisplay.setHint("Code for the action will be displayed here...");
        codeDisplay.setFocusable(false); // Make it read-only
        codeDisplay.setClickable(false);
        layout.addView(codeDisplay);

        // Populate the UI with buttons from our command list
        List<ApiCommand> commands = populateApiCommands();
        for (ApiCommand command : commands) {
            createApiButton(layout, command);
        }

        setContentView(scrollView);
    }

    /**
     * Creates a button from an ApiCommand and adds it to the layout.
     */
    private void createApiButton(LinearLayout layout, ApiCommand command) {
        Button button = new Button(this);
        button.setText(command.buttonText);
        button.setAllCaps(false);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(command.action);
            if (command.modifier != null) {
                command.modifier.modify(intent);
            }
            sendBroadcast(intent);
            codeDisplay.setText(command.codeSample);
        });
        layout.addView(button);
    }

    /**
     * Registers all necessary broadcast receivers for getting status updates from the system.
     */
    private void registerApiReceivers() {
        registerReceiver(brightnessReceiver, Constants.NOTIFY_BRIGHTNESS);
        registerReceiver(volumeReceiver, Constants.NOTIFY_VOLUME);
        registerReceiver(powerStatusReceiver, Constants.NOTIFY_POWER_STATUS);
        registerReceiver(portReceiver, Constants.NOTIFY_PORT);
        registerReceiver(orientationReceiver, Constants.NOTIFY_ORIENTATION);
        registerReceiver(irReceiver, Constants.NOTIFY_IR_REMOTE);
        registerReceiver(touchReceiver, Constants.NOTIFY_TOUCH);
        registerReceiver(irHomeReceiver, Constants.NOTIFY_IR_HOME);
        registerReceiver(audioChannelReceiver, Constants.NOTIFY_AUDIO_CHANNEL);
    }

    /**
     * Helper method to register a receiver and add it to a list for later un-registration.
     * This also handles the API level check for the exported flag.
     */
    private void registerReceiver(BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // The RECEIVER_NOT_EXPORTED flag is required for apps targeting API 33+
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
        registeredReceivers.add(receiver);
    }

    // --- Broadcast Receivers ---

    private final BroadcastReceiver brightnessReceiver = createReceiver("Brightness", Constants.EXTRA_LIGHT);
    private final BroadcastReceiver volumeReceiver = createReceiver("Volume", Constants.EXTRA_VOLUME);
    private final BroadcastReceiver powerStatusReceiver = createReceiver("Power Status", "status");
    private final BroadcastReceiver portReceiver = createReceiver("Port", Constants.EXTRA_PORT);
    private final BroadcastReceiver orientationReceiver = createReceiver("Orientation", Constants.EXTRA_ORIENTATION);
    private final BroadcastReceiver irReceiver = createReceiver("IR Status", Constants.EXTRA_STATE);
    private final BroadcastReceiver touchReceiver = createReceiver("Touch Status", Constants.EXTRA_STATE);
    private final BroadcastReceiver irHomeReceiver = createReceiver("IR Home Status", Constants.EXTRA_STATE);
    private final BroadcastReceiver audioChannelReceiver = createReceiver("Audio Channel", Constants.EXTRA_MODE);
    /**
     * Factory method to create a generic BroadcastReceiver for displaying integer values.
     *
     * @param name      The name to display (e.g., "Brightness", "Volume").
     * @param extraKey  The key for the integer extra in the received Intent.
     * @return A configured BroadcastReceiver instance.
     */
    private BroadcastReceiver createReceiver(@NonNull String name, @NonNull String extraKey) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String displayText;

                int mode = intent.getIntExtra("mode", -1);
                if (mode != -1) {
                    String humanReadable;
                    switch (mode) {
                        case 0: humanReadable = "Auto (Bluetooth > Speaker)"; break;
                        case 1: humanReadable = "Bluetooth"; break;
                        case 2: humanReadable = "Internal speaker"; break;
                        default: humanReadable = "Unknown (" + mode + ")";
                    }
                    displayText = "Current audio channel: " + humanReadable + " [" + mode + "]";
                    codeDisplay.setText(displayText);
                    return;
                }


                String action = intent.getAction() != null ? intent.getAction() : "null";
                Log.d("ApiReceiver", "Received broadcast: " + action);

                // ---------- robust "any-type" read ----------
                Bundle extras = intent.getExtras();


                if (extras != null && extras.containsKey(extraKey)) {

                    Object obj = extras.get(extraKey);

                    if (obj instanceof Integer) {                    // sender used putExtra(key, 123)
                        displayText = "Received " + name + ": " + obj;

                    } else if (obj instanceof String) {              // sender used putExtra(key, "123")
                        String s = (String) obj;
                        displayText = "Received " + name + ": " + s;

                    } else {                                         // some other unexpected type
                        displayText = "Error: " + name +
                                " has unsupported type (" +
                                (obj != null ? obj.getClass().getSimpleName() : "null") + ")";
                        Log.w("ApiReceiver", displayText);
                    }

                } else {                                             // extra missing entirely
                    displayText = "Error: " + name + " not found in Intent.";
                    Log.w("ApiReceiver", displayText + " Intent: " + intent);
                }

                // ---------- update UI + log ----------
                codeDisplay.setText(displayText);
                Log.d("ApiReceiver", displayText);
            }
        };
    }

    /**
     * Defines the list of all API commands that will be displayed as buttons in the UI.
     * To add a new button, simply add a new ApiCommand to this list.
     *
     * @return A list of ApiCommand objects.
     */
    private List<ApiCommand> populateApiCommands() {
        List<ApiCommand> commands = new ArrayList<>();

        // USB Power
        commands.add(
                new ApiCommand(
                        /* title  */ "USB Power ON",
                        /* action */ Constants.ACTION_SET_USB_POWER,          // still "com.assist.set.usbpower"
                        /* code   */
                        "Intent intent = new Intent(\"com.assist.set.usbpower\");\n" +
                                "intent.putExtra(\"usbNumber\", 0);\n" +
                                "intent.putExtra(\"usbPower\", 1);\n" +
                                "context.sendBroadcast(intent);",
                        /* filler */
                        intent -> {
                            intent.putExtra("usbNumber", 0);                   // or Constants.EXTRA_USB_NUMBER
                            intent.putExtra(Constants.EXTRA_USB_POWER, 1);     // keeps your existing constant
                        }
                )
        );
        commands.add(
                new ApiCommand(
                        /* title  */ "USB Power OFF",
                        /* action */ Constants.ACTION_SET_USB_POWER,          // still "com.assist.set.usbpower"
                        /* code   */
                        "Intent intent = new Intent(\"com.assist.set.usbpower\");\n" +
                                "intent.putExtra(\"usbNumber\", 0);\n" +
                                "intent.putExtra(\"usbPower\", 0);\n" +
                                "context.sendBroadcast(intent);",
                        /* filler */
                        intent -> {
                            intent.putExtra("usbNumber", 0);                   // or Constants.EXTRA_USB_NUMBER
                            intent.putExtra(Constants.EXTRA_USB_POWER, 0);     // keeps your existing constant
                        }
                )
        );

        // Power State
        commands.add(new ApiCommand("Sleep", Constants.ACTION_SET_POWER_STATE, "Intent intent = new Intent(\"com.assist.sleep.timeonoff\");\nintent.putExtra(\"sleepType\", 2);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_SLEEP_TYPE, 2)));
        commands.add(new ApiCommand("Shutdown", Constants.ACTION_SET_POWER_STATE, "Intent intent = new Intent(\"com.assist.sleep.timeonoff\");\nintent.putExtra(\"sleepType\", 1);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_SLEEP_TYPE, 1)));
        commands.add(new ApiCommand("Reboot", Constants.ACTION_REBOOT, "Intent intent = new Intent(\"com.assist.reboot.action\");\ncontext.sendBroadcast(intent);", null));
        commands.add(new ApiCommand("Shutdown & Wake in 2 Min", Constants.ACTION_SET_POWER_STATE, "Intent intent = new Intent(\"com.assist.sleep.timeonoff\");\nintent.putExtra(\"sleepType\", 1);\nintent.putExtra(\"onTime\", 120);\ncontext.sendBroadcast(intent);", intent -> {
            intent.putExtra(Constants.EXTRA_SLEEP_TYPE, 1);
            intent.putExtra(Constants.EXTRA_ON_TIME, 120);
        }));
        commands.add(new ApiCommand("Get Power Status", Constants.ACTION_GET_POWER_STATUS, "Intent intent = new Intent(\"com.assist.get.power.status\");\ncontext.sendBroadcast(intent);", null));

        // Brightness
        commands.add(new ApiCommand("Set Brightness to 10", Constants.ACTION_SET_BRIGHTNESS, "Intent intent = new Intent(\"com.assist.set.light\");\nintent.putExtra(\"light\", 10);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_LIGHT, 10)));
        commands.add(new ApiCommand("Set Brightness to 100", Constants.ACTION_SET_BRIGHTNESS, "Intent intent = new Intent(\"com.assist.set.light\");\nintent.putExtra(\"light\", 100);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_LIGHT, 100)));
        commands.add(new ApiCommand("Get Brightness", Constants.ACTION_GET_BRIGHTNESS, "Intent intent = new Intent(\"com.assist.get.light\");\ncontext.sendBroadcast(intent);", null));

        // Volume
        commands.add(new ApiCommand("Set Volume to 0", Constants.ACTION_SET_VOLUME, "Intent intent = new Intent(\"com.assist.set.volume\");\nintent.putExtra(\"volume\", 0);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_VOLUME, 0)));
        commands.add(new ApiCommand("Set Volume to 50", Constants.ACTION_SET_VOLUME, "Intent intent = new Intent(\"com.assist.set.volume\");\nintent.putExtra(\"volume\", 50);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_VOLUME, 50)));
        commands.add(new ApiCommand("Get Volume", Constants.ACTION_GET_VOLUME, "Intent intent = new Intent(\"com.assist.get.volume\");\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_VOLUME, true)));

        // Input/Output Source
        commands.add(new ApiCommand("Set Input to HDMI", Constants.ACTION_SET_PORT, "Intent intent = new Intent(\"com.assist.set.port\");\nintent.putExtra(\"port\", \"HDMI\");\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_PORT, "HDMI")));
        commands.add(new ApiCommand("Get Input Source", Constants.ACTION_GET_PORT, "Intent intent = new Intent(\"com.assist.get.port\");\ncontext.sendBroadcast(intent);", null));

        // Orientation
        commands.add(new ApiCommand("Set Orientation to 90°", Constants.ACTION_SET_ORIENTATION, "Intent intent = new Intent(\"com.assist.set.system.orientation\");\nintent.putExtra(\"orientation\", 90);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_ORIENTATION, 90)));
        commands.add(new ApiCommand("Get Orientation", Constants.ACTION_GET_ORIENTATION, "Intent intent = new Intent(\"com.assist.get.system.orientation\");\ncontext.sendBroadcast(intent);", null));

        // IR Remote
        commands.add(new ApiCommand("Enable IR Remote", Constants.ACTION_SET_IR_REMOTE, "Intent intent = new Intent(\"com.assist.set.remote\");\nintent.putExtra(\"state\", 0);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 0)));
        commands.add(new ApiCommand("Disable IR (Power Wake OK)", Constants.ACTION_SET_IR_REMOTE, "Intent intent = new Intent(\"com.assist.set.remote\");\nintent.putExtra(\"state\", 1);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 1)));
        commands.add(new ApiCommand("Get IR Status", Constants.ACTION_GET_IR_REMOTE, "Intent intent = new Intent(\"com.assist.get.remote\");\ncontext.sendBroadcast(intent);", null));

        // IR HOME Remote
        commands.add(new ApiCommand("Enable IR HOME Remote", Constants.ACTION_SET_IR_HOME_REMOTE, "Intent intent = new Intent(\"com.assist.sethome.action\");\nintent.putExtra(\"state\", 0);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 0)));
        commands.add(new ApiCommand("Disable IR HOME Remote", Constants.ACTION_SET_IR_HOME_REMOTE, "Intent intent = new Intent(\"com.assist.sethome.action\");\nintent.putExtra(\"state\", 1);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 1)));
        commands.add(new ApiCommand("Get IR HOME Status", Constants.ACTION_GET_IR_HOME_REMOTE, "Intent intent = new Intent(\"com.assist.gethome.action\");\ncontext.sendBroadcast(intent);", null));

        // Touch
        commands.add(new ApiCommand("Enable Touch", Constants.ACTION_SET_TOUCH, "Intent intent = new Intent(\"com.assist.set.touch\");\nintent.putExtra(\"state\", 0);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 0)));
        commands.add(new ApiCommand("Disable Touch", Constants.ACTION_SET_TOUCH, "Intent intent = new Intent(\"com.assist.set.touch\");\nintent.putExtra(\"state\", 1);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 1)));
        commands.add(new ApiCommand("Get Touch Status", Constants.ACTION_GET_TOUCH, "Intent intent = new Intent(\"com.assist.get.touch\");\ncontext.sendBroadcast(intent);", null));

        // Navigation Bar
        commands.add(new ApiCommand("Show Nav Bar", Constants.ACTION_SET_NAV_BAR, "Intent intent = new Intent(\"com.assist.switch.navigation.action\");\nintent.putExtra(\"state\", 0);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 0)));
        commands.add(new ApiCommand("Hide Nav Bar", Constants.ACTION_SET_NAV_BAR, "Intent intent = new Intent(\"com.assist.switch.navigation.action\");\nintent.putExtra(\"state\", 1);\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_STATE, 1)));

        // Miscellaneous
        commands.add(new ApiCommand("Take Screenshot", Constants.ACTION_TAKE_SCREENSHOT, "Intent intent = new Intent(\"com.assist.screencap.action\");\nintent.putExtra(\"screen_hdmi_path\", \"/storage/emulated/0/Download/screenshot.png\");\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_SCREENSHOT_PATH, "/storage/emulated/0/Download/screenshot.png")));
        commands.add(new ApiCommand("Kill This App", Constants.ACTION_KILL_APP, "Intent intent = new Intent(\"com.assist.kill.app.action\");\nintent.putExtra(\"packageName\", getPackageName());\ncontext.sendBroadcast(intent);", intent -> intent.putExtra(Constants.EXTRA_PACKAGE_NAME, getPackageName())));


        // In populateApiCommands()

        // Audio Channel
        commands.add(new ApiCommand(
                "Audio: Auto (BT > Speaker)",
                Constants.ACTION_SET_AUDIO_CHANNEL,
                "Intent intent = new Intent(\"com.assist.set.audio.channel\");\n" +
                        "intent.putExtra(\"mode\", 0);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra(Constants.EXTRA_MODE, 0)
        ));

        commands.add(new ApiCommand(
                "Audio: Bluetooth",
                Constants.ACTION_SET_AUDIO_CHANNEL,
                "Intent intent = new Intent(\"com.assist.set.audio.channel\");\n" +
                        "intent.putExtra(\"mode\", 1);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra(Constants.EXTRA_MODE, 1)
        ));

        commands.add(new ApiCommand(
                "Audio: Internal Speaker",
                Constants.ACTION_SET_AUDIO_CHANNEL,
                "Intent intent = new Intent(\"com.assist.set.audio.channel\");\n" +
                        "intent.putExtra(\"mode\", 2);\n" +
                        "context.sendBroadcast(intent);",
                intent -> intent.putExtra(Constants.EXTRA_MODE, 2)
        ));
        commands.add(new ApiCommand("Get Audio Channel", Constants.ACTION_GET_AUDIO_CHANNEL, "Intent intent = new Intent(\"com.assist.get.audio.channel\");\ncontext.sendBroadcast(intent);", null));


        return commands;
    }
}