package com.example.allseescreencontroljavabaseddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Allsee Screen Control – Minimal, developer-friendly demo.
 *
 * - Tap buttons to send the documented broadcasts.
 * - The lower text box shows a ready-to-copy code sample and prints any values
 *   returned via notify broadcasts.
 *
 * NOTE: Some broadcasts require system permissions/privileges on specific
 * Allsee firmware. On a regular device they may no-op.
 */
public class MainActivity extends AppCompatActivity {

    // ---------------------------------------------------------------------
    //  Constants
    // ---------------------------------------------------------------------
    private static final class C {
        // ===== Set/Do actions =====
        static final String SET_USB_POWER        = "com.assist.set.usbpower";
        static final String SET_POWER_STATE      = "com.assist.sleep.timeonoff";
        static final String REBOOT               = "com.assist.reboot.action";
        static final String SET_BRIGHTNESS       = "com.assist.set.light";
        static final String SET_PORT             = "com.assist.set.port";
        static final String KILL_APP             = "com.assist.kill.app.action";
        static final String SET_TIME             = "com.assist.settime.action";
        static final String SET_NAV_BAR          = "com.assist.switch.navigation.action";
        static final String SCREENSHOT           = "com.assist.screencap.action";
        static final String SET_VOLUME           = "com.assist.set.volume";
        static final String INSTALL_APP          = "com.assist.install.app.action";
        static final String SET_ORIENTATION      = "com.assist.set.system.orientation";
        static final String SET_IR_REMOTE        = "com.assist.set.remote";
        static final String SET_IR_HOME          = "com.assist.sethome.action";
        static final String SET_TOUCH            = "com.assist.set.touch";
        static final String SET_AUDIO_CHANNEL    = "com.assist.set.audio.channel";
        static final String SET_LOGO_STYLE       = "com.assist.set.logostyle";   // NEW

        // ===== Get requests =====
        static final String GET_BRIGHTNESS       = "com.assist.get.light";
        static final String GET_VOLUME           = "com.assist.get.volume";
        static final String GET_POWER_STATUS     = "com.assist.get.power.status";
        static final String GET_PORT             = "com.assist.get.port";
        static final String GET_ORIENTATION      = "com.assist.get.system.orientation";
        static final String GET_IR_REMOTE        = "com.assist.get.remote";
        static final String GET_IR_HOME          = "com.assist.gethome.action";
        static final String GET_TOUCH            = "com.assist.get.touch";
        static final String GET_AUDIO_CHANNEL    = "com.assist.get.audio.channel";
        static final String GET_USB_POWER        = "com.assist.get.usbpower";    // NEW
        static final String GET_LOGO_STYLE       = "com.assist.get.logostyle";   // NEW

        // ===== Notify (responses) =====
        static final String NOTIFY_BRIGHTNESS    = "com.assist.notify.light";
        static final String NOTIFY_VOLUME        = "com.assist.notify.volume";
        static final String NOTIFY_POWER_STATUS  = "com.assist.notify.power.status";
        static final String NOTIFY_PORT          = "com.assist.notify.port";
        static final String NOTIFY_ORIENTATION   = "com.assist.notify.orientation";
        static final String NOTIFY_IR_REMOTE     = "com.assist.notify.remote";
        static final String NOTIFY_IR_HOME       = "com.assist.notify.homekey.disable";
        static final String NOTIFY_TOUCH         = "com.assist.notify.touch";
        static final String NOTIFY_AUDIO_CHANNEL = "com.assist.notify.audio.channel";
        static final String NOTIFY_USB_POWER     = "com.assist.notify.usbpower";   // NEW
        static final String NOTIFY_LOGO_STYLE    = "com.assist.notify.logostyle";  // NEW

        // ===== Extras =====
        static final String EXTRA_MODE           = "mode";
        static final String EXTRA_USB_POWER      = "usbPower";
        static final String EXTRA_USB_NUMBER     = "usbNumber";       // NEW
        static final String EXTRA_SLEEP_TYPE     = "sleepType";
        static final String EXTRA_ON_TIME        = "onTime";
        static final String EXTRA_LIGHT          = "light";
        static final String EXTRA_PORT           = "port";
        static final String EXTRA_PACKAGE_NAME   = "packageName";
        static final String EXTRA_APP_FILE_PATH  = "appFilePath";
        static final String EXTRA_MSEC           = "msec";
        static final String EXTRA_STATE          = "state";
        static final String EXTRA_SCREENSHOT     = "screen_hdmi_path";
        static final String EXTRA_VOLUME         = "volume";
        static final String EXTRA_MUTE           = "mute";
        static final String EXTRA_ORIENTATION    = "orientation";
        static final String EXTRA_LOGO_STYLE     = "logoStyle";       // NEW
    }

    // ---------------------------------------------------------------------
    //  Tiny command descriptor used to build the UI
    // ---------------------------------------------------------------------
    private static class ApiCommand {
        final String title;
        final String action;
        final String code;
        final @Nullable BroadcastModifier modifier;
        ApiCommand(String title, String action, String code, @Nullable BroadcastModifier modifier) {
            this.title = title;
            this.action = action;
            this.code = code;
            this.modifier = modifier;
        }
    }

    /** Functional interface to tweak an Intent before sending. */
    interface BroadcastModifier { void modify(Intent intent); }

    // ---------------------------------------------------------------------
    //  State
    // ---------------------------------------------------------------------
    private TextView codePane;
    private final List<BroadcastReceiver> trackedReceivers = new ArrayList<>();
    private int lastVolume = -1;  // NEW: Track last known volume (-1 = unknown)
    private int lastMute = -1;    // NEW: Track last known mute (-1 = unknown)

    // ---------------------------------------------------------------------
    //  Lifecycle
    // ---------------------------------------------------------------------
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Allsee Screen Control – Demo");
        setupUi();
        registerAllReceivers();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        // Always unregister to avoid leaks.
        for (BroadcastReceiver r : trackedReceivers) unregisterReceiver(r);
        trackedReceivers.clear();
    }

    // ---------------------------------------------------------------------
    //  UI
    // ---------------------------------------------------------------------
    private void setupUi() {
        // Root: vertical column (header stays fixed)
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);

        // ----- Fixed header (output pane) -----
        codePane = new TextView(this);
        codePane.setText("Code for the last action or notify values will appear here…");
        codePane.setTextSize(14);
        codePane.setTextIsSelectable(true);   // allow copy/select
        codePane.setFocusable(false);
        codePane.setClickable(false);
        // Optional cosmetics:
        // codePane.setTypeface(Typeface.MONOSPACE);
        // codePane.setElevation(4f);

        // Header sits at top, wraps content height
        root.addView(codePane,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        // ----- Scrollable area (buttons) -----
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); // makes child stretch if few buttons

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        // Add buttons
        for (ApiCommand cmd : buildCommands()) {
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(cmd.title);
            b.setOnClickListener(v -> {
                // NEW: Reset volume/mute trackers before sending GET_VOLUME
                if (cmd.action.equals(C.GET_VOLUME)) {
                    lastVolume = -1;
                    lastMute = -1;
                }
                Intent i = new Intent(cmd.action);
                if (cmd.modifier != null) cmd.modifier.modify(i);
                sendBroadcast(i);
                codePane.setText(cmd.code);
            });
            col.addView(b, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        scroll.addView(col, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        // ScrollView fills remaining space
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
    }

    // ---------------------------------------------------------------------
    //  Receivers
    // ---------------------------------------------------------------------
    private void registerAllReceivers() {
        // Simple “single-int extra” receivers
        registerAndTrack(makeValueReceiver("Brightness", C.EXTRA_LIGHT), C.NOTIFY_BRIGHTNESS);
        registerAndTrack(makeValueReceiver("Power Status (0:on, 1:off)", "status"), C.NOTIFY_POWER_STATUS);
        registerAndTrack(makeValueReceiver("Port (int mapping vendor-defined)", C.EXTRA_PORT), C.NOTIFY_PORT);
        registerAndTrack(makeValueReceiver("Orientation", C.EXTRA_ORIENTATION), C.NOTIFY_ORIENTATION);
        registerAndTrack(makeValueReceiver("IR State", C.EXTRA_STATE), C.NOTIFY_IR_REMOTE);
        registerAndTrack(makeValueReceiver("IR HOME State", C.EXTRA_STATE), C.NOTIFY_IR_HOME);
        registerAndTrack(makeValueReceiver("Touch State", C.EXTRA_STATE), C.NOTIFY_TOUCH);

        // Multi-field receivers
        registerAndTrack(volumeReceiver, C.NOTIFY_VOLUME);

        // Decoded/pretty receivers
        registerAndTrack(audioChannelReceiver, C.NOTIFY_AUDIO_CHANNEL);
        registerAndTrack(usbPowerReceiver, C.NOTIFY_USB_POWER);           // NEW
        registerAndTrack(logoStyleReceiver, C.NOTIFY_LOGO_STYLE);         // NEW
    }

    /** Helper to register + remember receivers (handles API 33+ flag). */
    private void registerAndTrack(BroadcastReceiver r, String action) {
        IntentFilter f = new IntentFilter(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(r, f, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(r, f);
        }
        trackedReceivers.add(r);
    }

    /** Generic receiver: prints an extra that might be int, long, boolean, or string. */
    private BroadcastReceiver makeValueReceiver(@NonNull String label, @NonNull String extraKey) {
        return new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                if (!i.hasExtra(extraKey)) {
                    String msg = "Missing extra \"" + extraKey + "\" in " + i.getAction();
                    codePane.setText(msg);
                    Log.w("Notify", msg);
                    return;
                }

                Object raw = null;
                try {
                    Bundle b = i.getExtras();
                    if (b != null) raw = b.get(extraKey);
                } catch (Throwable t) {
                    // Defensive: some OEMs throw on bad parcelables
                    Log.w("Notify", "Failed to read extras for key=" + extraKey, t);
                }

                String valueText;
                if (raw == null) {
                    valueText = "null";
                } else if (raw instanceof Number || raw instanceof Boolean || raw instanceof CharSequence) {
                    // Normalize numeric strings like "100" → 100 for display, otherwise show as-is.
                    if (raw instanceof CharSequence) {
                        String s = raw.toString();
                        try { valueText = String.valueOf(Integer.parseInt(s.trim())); }
                        catch (NumberFormatException e1) {
                            try { valueText = String.valueOf(Long.parseLong(s.trim())); }
                            catch (NumberFormatException e2) { valueText = s; }
                        }
                    } else {
                        valueText = String.valueOf(raw);
                    }
                } else {
                    // Fallback: toString() for any other scalar-ish value
                    valueText = String.valueOf(raw);
                }

                String msg = label + ": " + valueText;
                codePane.setText(msg);
                Log.d("Notify", msg);
            }
        };
    }

    // Pretty receivers ------------------------------------------------------

    private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            boolean updated = false;
            if (i.hasExtra(C.EXTRA_VOLUME)) {
                lastVolume = i.getIntExtra(C.EXTRA_VOLUME, -1);
                updated = true;
            }
            if (i.hasExtra(C.EXTRA_MUTE)) {
                lastMute = i.getIntExtra(C.EXTRA_MUTE, -1);
                updated = true;
            }
            if (updated) {
                StringBuilder sb = new StringBuilder();
                if (lastVolume != -1) sb.append("Volume: ").append(lastVolume).append('\n');
                if (lastMute != -1) sb.append("Mute: ").append(lastMute);  // 0:unmute 1:mute
                String out = sb.toString().trim();
                codePane.setText(out.isEmpty() ? "No volume info in notify." : out);
            }
        }
    };

    private final BroadcastReceiver audioChannelReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            int mode = i.getIntExtra(C.EXTRA_MODE, -1);
            String msg = "Audio Channel: " + decodeAudioMode(mode) + " [" + mode + "]";
            codePane.setText(msg);
        }
    };

    // NEW: show On/Off and which USB if present
    private final BroadcastReceiver usbPowerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            int power = i.getIntExtra(C.EXTRA_USB_POWER, -1); // 0:off 1:on
            int idx   = i.getIntExtra(C.EXTRA_USB_NUMBER, -1);
            String prefix = (idx >= 0) ? ("USB[" + idx + "] ") : "USB ";
            String msg = prefix + "Power: " + (power == 1 ? "ON" : power == 0 ? "OFF" : "UNKNOWN ("+power+")");
            codePane.setText(msg);
        }
    };

    // NEW: pretty print logo style (0/1/2)
    private final BroadcastReceiver logoStyleReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            int style = i.getIntExtra(C.EXTRA_LOGO_STYLE, -1);
            String msg = "Scaler Logo Style: " + decodeLogoStyle(style) + " [" + style + "]";
            codePane.setText(msg);
        }
    };

    // ---------------------------------------------------------------------
    //  Commands shown as buttons
    // ---------------------------------------------------------------------
    private List<ApiCommand> buildCommands() {
        List<ApiCommand> list = new ArrayList<>();

        // USB Power ---------------------------------------------------------
        list.add(new ApiCommand(
                "USB[0] Power ON",
                C.SET_USB_POWER,
                "Intent i = new Intent(\"com.assist.set.usbpower\");\n" +
                        "i.putExtra(\"usbNumber\", 0);\n" +
                        "i.putExtra(\"usbPower\", 1);\n" +
                        "sendBroadcast(i);",
                i -> { i.putExtra(C.EXTRA_USB_NUMBER, 0); i.putExtra(C.EXTRA_USB_POWER, 1); }
        ));
        list.add(new ApiCommand(
                "USB[0] Power OFF",
                C.SET_USB_POWER,
                "Intent i = new Intent(\"com.assist.set.usbpower\");\n" +
                        "i.putExtra(\"usbNumber\", 0);\n" +
                        "i.putExtra(\"usbPower\", 0);\n" +
                        "sendBroadcast(i);",
                i -> { i.putExtra(C.EXTRA_USB_NUMBER, 0); i.putExtra(C.EXTRA_USB_POWER, 0); }
        ));
        list.add(new ApiCommand( // NEW
                "Get USB[0] Power",
                C.GET_USB_POWER,
                "Intent i = new Intent(\"com.assist.get.usbpower\");\n" +
                        "i.putExtra(\"usbNumber\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_USB_NUMBER, 0)
        ));
        list.add(new ApiCommand( // NEW
                "Get USB[1] Power",
                C.GET_USB_POWER,
                "Intent i = new Intent(\"com.assist.get.usbpower\");\n" +
                        "i.putExtra(\"usbNumber\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_USB_NUMBER, 1)
        ));

        // Power / Reboot ----------------------------------------------------
        list.add(new ApiCommand(
                "Sleep",
                C.SET_POWER_STATE,
                "Intent i = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "i.putExtra(\"sleepType\", 2);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_SLEEP_TYPE, 2)
        ));
        list.add(new ApiCommand(
                "Wake Screen",
                C.SET_POWER_STATE,
                "Intent i = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "i.putExtra(\"sleepType\", 3);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_SLEEP_TYPE, 3)
        ));
        list.add(new ApiCommand(
                "Shutdown",
                C.SET_POWER_STATE,
                "Intent i = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "i.putExtra(\"sleepType\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_SLEEP_TYPE, 1)
        ));
        list.add(new ApiCommand(
                "Shutdown & Wake in 120s",
                C.SET_POWER_STATE,
                "Intent i = new Intent(\"com.assist.sleep.timeonoff\");\n" +
                        "i.putExtra(\"sleepType\", 1);\n" +
                        "i.putExtra(\"onTime\", 120);\n" +
                        "sendBroadcast(i);",
                i -> { i.putExtra(C.EXTRA_SLEEP_TYPE, 1); i.putExtra(C.EXTRA_ON_TIME, 120); }
        ));
        list.add(new ApiCommand(
                "Get Power Status",
                C.GET_POWER_STATUS,
                "sendBroadcast(new Intent(\"com.assist.get.power.status\"));",
                null
        ));
        list.add(new ApiCommand(
                "Reboot",
                C.REBOOT,
                "sendBroadcast(new Intent(\"com.assist.reboot.action\"));",
                null
        ));

        // Brightness --------------------------------------------------------
        list.add(new ApiCommand(
                "Brightness 10",
                C.SET_BRIGHTNESS,
                "Intent i = new Intent(\"com.assist.set.light\");\n" +
                        "i.putExtra(\"light\", 10);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_LIGHT, 10)
        ));
        list.add(new ApiCommand(
                "Brightness 100",
                C.SET_BRIGHTNESS,
                "Intent i = new Intent(\"com.assist.set.light\");\n" +
                        "i.putExtra(\"light\", 100);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_LIGHT, 100)
        ));
        list.add(new ApiCommand(
                "Get Brightness",
                C.GET_BRIGHTNESS,
                "sendBroadcast(new Intent(\"com.assist.get.light\"));",
                null
        ));

        // Volume ------------------------------------------------------------
        list.add(new ApiCommand(
                "Volume 0",
                C.SET_VOLUME,
                "Intent i = new Intent(\"com.assist.set.volume\");\n" +
                        "i.putExtra(\"volume\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_VOLUME, 0)
        ));
        list.add(new ApiCommand(
                "Volume 50",
                C.SET_VOLUME,
                "Intent i = new Intent(\"com.assist.set.volume\");\n" +
                        "i.putExtra(\"volume\", 50);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_VOLUME, 50)
        ));
        list.add(new ApiCommand(
                "Mute",
                C.SET_VOLUME,
                "Intent i = new Intent(\"com.assist.set.volume\");\n" +
                        "i.putExtra(\"mute\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MUTE, 1)
        ));
        list.add(new ApiCommand(
                "Unmute",
                C.SET_VOLUME,
                "Intent i = new Intent(\"com.assist.set.volume\");\n" +
                        "i.putExtra(\"mute\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MUTE, 0)
        ));
        list.add(new ApiCommand(
                "Get Volume",
                C.GET_VOLUME,
                "Intent i = new Intent(\"com.assist.get.volume\");\n" +
                        "i.putExtra(\"volume\", true);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_VOLUME, true)
        ));
        list.add(new ApiCommand(
                "Get Mute",
                C.GET_VOLUME,
                "Intent i = new Intent(\"com.assist.get.volume\");\n" +
                        "i.putExtra(\"mute\", true);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MUTE, true)
        ));
        list.add(new ApiCommand(  // Your new combined command
                "Get Mute and Volume",
                C.GET_VOLUME,
                "Intent i = new Intent(\"com.assist.get.volume\");\n" +
                        "i.putExtra(\"mute\", true);\n" +
                        "i.putExtra(\"volume\", true);\n" +
                        "sendBroadcast(i);",
                i -> {
                    i.putExtra(C.EXTRA_MUTE, true);
                    i.putExtra(C.EXTRA_VOLUME, true);
                }
        ));

        // Input / Output Source --------------------------------------------
        list.add(new ApiCommand(
                "Input: HDMI",
                C.SET_PORT,
                "Intent i = new Intent(\"com.assist.set.port\");\n" +
                        "i.putExtra(\"port\", \"HDMI\");\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_PORT, "HDMI")
        ));
        list.add(new ApiCommand(
                "Input: VGA",
                C.SET_PORT,
                "Intent i = new Intent(\"com.assist.set.port\");\n" +
                        "i.putExtra(\"port\", \"VGA\");\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_PORT, "VGA")
        ));
        list.add(new ApiCommand(
                "Output: Android (Digital)",
                C.SET_PORT,
                "Intent i = new Intent(\"com.assist.set.port\");\n" +
                        "i.putExtra(\"port\", \"Digital\");\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_PORT, "Digital")
        ));
        list.add(new ApiCommand(
                "Get Port",
                C.GET_PORT,
                "sendBroadcast(new Intent(\"com.assist.get.port\"));",
                null
        ));

        // Orientation -------------------------------------------------------
        list.add(new ApiCommand(
                "Orientation 90°",
                C.SET_ORIENTATION,
                "Intent i = new Intent(\"com.assist.set.system.orientation\");\n" +
                        "i.putExtra(\"orientation\", 90);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_ORIENTATION, 90)
        ));
        list.add(new ApiCommand(
                "Orientation 180°",
                C.SET_ORIENTATION,
                "Intent i = new Intent(\"com.assist.set.system.orientation\");\n" +
                        "i.putExtra(\"orientation\", 180);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_ORIENTATION, 180)
        ));
        list.add(new ApiCommand(
                "Get Orientation",
                C.GET_ORIENTATION,
                "sendBroadcast(new Intent(\"com.assist.get.system.orientation\"));",
                null
        ));

        // IR Remote ---------------------------------------------------------
        list.add(new ApiCommand(
                "IR: Enable",
                C.SET_IR_REMOTE,
                "Intent i = new Intent(\"com.assist.set.remote\");\n" +
                        "i.putExtra(\"state\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 0)
        ));
        list.add(new ApiCommand(
                "IR: Disable (Power can wake)",
                C.SET_IR_REMOTE,
                "Intent i = new Intent(\"com.assist.set.remote\");\n" +
                        "i.putExtra(\"state\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 1)
        ));
        list.add(new ApiCommand(
                "Get IR State",
                C.GET_IR_REMOTE,
                "sendBroadcast(new Intent(\"com.assist.get.remote\"));",
                null
        ));

        // IR HOME key -------------------------------------------------------
        list.add(new ApiCommand(
                "IR HOME: Enable",
                C.SET_IR_HOME,
                "Intent i = new Intent(\"com.assist.sethome.action\");\n" +
                        "i.putExtra(\"state\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 0)
        ));
        list.add(new ApiCommand(
                "IR HOME: Disable",
                C.SET_IR_HOME,
                "Intent i = new Intent(\"com.assist.sethome.action\");\n" +
                        "i.putExtra(\"state\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 1)
        ));
        list.add(new ApiCommand(
                "Get IR HOME State",
                C.GET_IR_HOME,
                "sendBroadcast(new Intent(\"com.assist.gethome.action\"));",
                null
        ));

        // Touch -------------------------------------------------------------
        list.add(new ApiCommand(
                "Touch: Enable",
                C.SET_TOUCH,
                "Intent i = new Intent(\"com.assist.set.touch\");\n" +
                        "i.putExtra(\"state\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 0)
        ));
        list.add(new ApiCommand(
                "Touch: Disable",
                C.SET_TOUCH,
                "Intent i = new Intent(\"com.assist.set.touch\");\n" +
                        "i.putExtra(\"state\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 1)
        ));
        list.add(new ApiCommand(
                "Get Touch State",
                C.GET_TOUCH,
                "sendBroadcast(new Intent(\"com.assist.get.touch\"));",
                null
        ));

        // Navigation bar ----------------------------------------------------
        list.add(new ApiCommand(
                "Nav Bar: Show",
                C.SET_NAV_BAR,
                "Intent i = new Intent(\"com.assist.switch.navigation.action\");\n" +
                        "i.putExtra(\"state\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 0)
        ));
        list.add(new ApiCommand(
                "Nav Bar: Hide",
                C.SET_NAV_BAR,
                "Intent i = new Intent(\"com.assist.switch.navigation.action\");\n" +
                        "i.putExtra(\"state\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_STATE, 1)
        ));

        // Audio output channel ---------------------------------------------
        list.add(new ApiCommand(
                "Audio: Auto (BT > Speaker)",
                C.SET_AUDIO_CHANNEL,
                "Intent i = new Intent(\"com.assist.set.audio.channel\");\n" +
                        "i.putExtra(\"mode\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MODE, 0)
        ));
        list.add(new ApiCommand(
                "Audio: Bluetooth",
                C.SET_AUDIO_CHANNEL,
                "Intent i = new Intent(\"com.assist.set.audio.channel\");\n" +
                        "i.putExtra(\"mode\", 1);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MODE, 1)
        ));
        list.add(new ApiCommand(
                "Audio: Internal Speaker",
                C.SET_AUDIO_CHANNEL,
                "Intent i = new Intent(\"com.assist.set.audio.channel\");\n" +
                        "i.putExtra(\"mode\", 2);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MODE, 2)
        ));
        list.add(new ApiCommand(
                "Get Audio Channel",
                C.GET_AUDIO_CHANNEL,
                "sendBroadcast(new Intent(\"com.assist.get.audio.channel\"));",
                null
        ));

        // Screenshot / Kill / Install / Time -------------------------------
        list.add(new ApiCommand(
                "Screenshot → /sdcard/Download/screenshot.png",
                C.SCREENSHOT,
                "Intent i = new Intent(\"com.assist.screencap.action\");\n" +
                        "i.putExtra(\"screen_hdmi_path\", \"/storage/emulated/0/Download/screenshot.png\");\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_SCREENSHOT, "/storage/emulated/0/Download/screenshot.png")
        ));
        list.add(new ApiCommand(
                "Kill This App",
                C.KILL_APP,
                "Intent i = new Intent(\"com.assist.kill.app.action\");\n" +
                        "i.putExtra(\"packageName\", getPackageName());\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_PACKAGE_NAME, getPackageName())
        ));
        list.add(new ApiCommand(
                "Install APK (example)",
                C.INSTALL_APP,
                "Intent i = new Intent(\"com.assist.install.app.action\");\n" +
                        "i.putExtra(\"packageName\", \"com.example.app\");\n" +
                        "i.putExtra(\"appFilePath\", \"/storage/emulated/0/Download/example.apk\");\n" +
                        "sendBroadcast(i);",
                i -> { i.putExtra(C.EXTRA_PACKAGE_NAME, "com.example.app");
                    i.putExtra(C.EXTRA_APP_FILE_PATH, "/storage/emulated/0/Download/example.apk"); }
        ));
        list.add(new ApiCommand(
                "Set System Time (example)",
                C.SET_TIME,
                "Intent i = new Intent(\"com.assist.settime.action\");\n" +
                        "// Example: 2024-12-05 11:38:33.118 UTC\n" +
                        "i.putExtra(\"msec\", 1733398713118L);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_MSEC, 1733398713118L)
        ));

        // Scaler Logo Style ------------------------------------------------- (NEW)
        list.add(new ApiCommand(
                "Logo Style: Android (0)",
                C.SET_LOGO_STYLE,
                "Intent i = new Intent(\"com.assist.set.logostyle\");\n" +
                        "i.putExtra(\"logoStyle\", 0);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_LOGO_STYLE, 0)
        ));
        list.add(new ApiCommand(
                "Logo Style: Videri (2)",
                C.SET_LOGO_STYLE,
                "Intent i = new Intent(\"com.assist.set.logostyle\");\n" +
                        "i.putExtra(\"logoStyle\", 2);\n" +
                        "sendBroadcast(i);",
                i -> i.putExtra(C.EXTRA_LOGO_STYLE, 2)
        ));
        list.add(new ApiCommand(
                "Get Logo Style",
                C.GET_LOGO_STYLE,
                "sendBroadcast(new Intent(\"com.assist.get.logostyle\"));",
                null
        ));

        return list;
    }

    // ---------------------------------------------------------------------
    //  Decoders (human-readable)
    // ---------------------------------------------------------------------
    private static String decodeAudioMode(int mode) {
        switch (mode) {
            case 0: return "Auto (Bluetooth > Speaker)";
            case 1: return "Bluetooth";
            case 2: return "Internal speaker";
            default: return "Unknown";
        }
    }

    private static String decodeLogoStyle(int style) {
        switch (style) {
            case 0: return "Android";
            case 2: return "Videri";
            default: return "Unknown";
        }
    }
}