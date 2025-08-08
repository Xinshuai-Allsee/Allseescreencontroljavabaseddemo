package com.example.allseescreencontroljavabaseddemo;

import android.app.Activity;
import android.graphics.Typeface;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "AllseeCodecReport";

    private TextView status;
    private TextView jsonView;
    private Button generateBtn;
    private ProgressBar progress;
    private String lastJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ==== Programmatic UI (no XML) ====
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Allsee Codec Reporter");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);

        status = new TextView(this);
        status.setText("Tap ‘Generate’ to query system codecs.");
        status.setTextSize(14);

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);

        generateBtn = new Button(this);
        generateBtn.setText("Generate report");

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.START);
        btnRow.addView(generateBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        jsonView = new TextView(this);
        jsonView.setTextSize(12);
        jsonView.setTypeface(Typeface.MONOSPACE);
        jsonView.setTextIsSelectable(true);

        root.addView(title);
        root.addView(status);
        root.addView(progress);
        root.addView(btnRow);
        root.addView(jsonView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(scroll);

        // ==== Actions ====
        generateBtn.setOnClickListener(v -> generateReport());

        // Optionally auto-generate on first launch
        generateReport();
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        generateBtn.setEnabled(!busy);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void generateReport() {
        setBusy(true);
        status.setText("Querying MediaCodec…");
        new Thread(() -> {
            try {
                JSONObject report = generateCodecReport();
                String pretty = report.toString(2);
                lastJson = pretty;
                String summary = buildSummary(report);
                runOnUiThread(() -> {
                    jsonView.setText(pretty);
                    status.setText(summary);
                    setBusy(false);
                });
            } catch (Throwable t) {
                Log.e(TAG, "Failed to generate", t);
                runOnUiThread(() -> {
                    status.setText("Failed: " + t.getMessage());
                    setBusy(false);
                });
            }
        }).start();
    }

    private String buildSummary(JSONObject report) {
        try {
            JSONObject device = report.getJSONObject("device");
            JSONArray decs = report.getJSONArray("decoders");
            boolean avc4k = has4k(decs, "video/avc");
            boolean hevc4k = has4k(decs, "video/hevc");
            boolean vp94k = has4k(decs, "video/vp9");
            boolean av14k = has4k(decs, "video/av01");
            List<String> lines = new ArrayList<>();
            lines.add(device.optString("brand") + " " + device.optString("model") + " (Android " + device.optString("android") + ")");
            lines.add("H.265/HEVC 4K: " + (hevc4k ? "YES" : "NO"));
            lines.add("H.264/AVC 4K: " + (avc4k ? "YES" : "NO"));
            lines.add("VP9 4K:       " + (vp94k ? "YES" : "NO"));
            lines.add("AV1 4K:       " + (av14k ? "YES" : "NO"));
            return join("\n", lines);
        } catch (Exception e) {
            return "Summary unavailable: " + e.getMessage();
        }
    }

    private boolean has4k(JSONArray decs, String mime) {
        for (int i = 0; i < decs.length(); i++) {
            JSONObject o = decs.optJSONObject(i);
            if (o == null) continue;
            if (mime.equals(o.optString("mime")) && o.optBoolean("supports4kSize", false)) return true;
        }
        return false;
    }

    private String join(String sep, List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    // ===== Codec inspection logic (optimized/expanded) =====

    private String profileName(String mime, int p) {
        switch (mime) {
            case "video/avc":
                if (p == MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline) return "AVC Baseline";
                if (p == MediaCodecInfo.CodecProfileLevel.AVCProfileMain) return "AVC Main";
                if (p == MediaCodecInfo.CodecProfileLevel.AVCProfileHigh) return "AVC High";
                return "AVC p=" + p;
            case "video/hevc":
                if (p == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain) return "HEVC Main";
                if (p == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10) return "HEVC Main10";
                return "HEVC p=" + p;
            case "video/vp9":
                if (p == MediaCodecInfo.CodecProfileLevel.VP9Profile0) return "VP9 Profile0";
                if (p == MediaCodecInfo.CodecProfileLevel.VP9Profile2) return "VP9 Profile2 (10-bit)";
                return "VP9 p=" + p;
            case "video/av01":
                return "AV1 p=" + p;
            default:
                return mime + " p=" + p;
        }
    }

    private String avcLevelName(int level) {
        // Map the common AVC level ints to human-readable labels
        try {
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel1) return "Level 1.0";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel1b) return "Level 1b";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel11) return "Level 1.1";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel12) return "Level 1.2";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel13) return "Level 1.3";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel2)  return "Level 2.0";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel21) return "Level 2.1";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel22) return "Level 2.2";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel3)  return "Level 3.0";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel31) return "Level 3.1";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel32) return "Level 3.2";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel4)  return "Level 4.0";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel41) return "Level 4.1";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel42) return "Level 4.2";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel5)  return "Level 5.0";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel51) return "Level 5.1";
            if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel52) return "Level 5.2";
            if (Build.VERSION.SDK_INT >= 23) {
                if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel6)  return "Level 6.0";
                if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel61) return "Level 6.1";
                if (level == MediaCodecInfo.CodecProfileLevel.AVCLevel62) return "Level 6.2";
            }
        } catch (Throwable ignored) {}
        return "Level " + level;
    }

    private JSONObject generateCodecReport() throws Exception {
        final List<String> wanted = Arrays.asList(
                "video/avc", "video/hevc", "video/vp9", "video/av01", "video/x-vnd.on2.vp8"
        );

        final MediaCodecInfo[] infos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
        final JSONArray out = new JSONArray();

        for (MediaCodecInfo ci : infos) {
            if (ci.isEncoder()) continue;

            String[] types;
            try {
                types = ci.getSupportedTypes();
            } catch (Throwable t) {
                continue;
            }

            for (String type : types) {
                if (!wanted.contains(type)) continue;

                MediaCodecInfo.CodecCapabilities cap;
                try {
                    cap = ci.getCapabilitiesForType(type);
                } catch (Throwable t) {
                    continue;
                }

                final MediaCodecInfo.VideoCapabilities v = cap.getVideoCapabilities();
                if (v == null) continue;

                // ===== 4K checks (robust order) =====
                final int W4K = 3840, H4K = 2160;
                boolean supports4kSize = false;
                boolean supports4k30 = false;
                boolean supports4k60 = false;

                // 1) Exact capability queries first
                try {
                    supports4kSize = v.isSizeSupported(W4K, H4K);
                } catch (Throwable ignored) {
                    // fallback to range containment if vendor throws
                    try {
                        Range<Integer> widths = v.getSupportedWidths();
                        Range<Integer> heights = v.getSupportedHeights();
                        supports4kSize = (widths != null && heights != null
                                && widths.contains(W4K) && heights.contains(H4K));
                    } catch (Throwable ignored2) {}
                }

                try {
                    if (supports4kSize) {
                        // areSizeAndRateSupported is precise when implemented
                        supports4k30 = v.areSizeAndRateSupported(W4K, H4K, 30.0);
                        supports4k60 = v.areSizeAndRateSupported(W4K, H4K, 60.0);
                    }
                } catch (Throwable ignored) {}

                // 2) Performance points (API 29+) as hint/confirmation
                if (Build.VERSION.SDK_INT >= 29) {
                    try {
                        List<MediaCodecInfo.VideoCapabilities.PerformancePoint> pps = v.getSupportedPerformancePoints();
                        if (pps != null && !pps.isEmpty()) {
                            final MediaFormat fmt30 = MediaFormat.createVideoFormat(type, W4K, H4K);
                            fmt30.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                            final MediaFormat fmt60 = MediaFormat.createVideoFormat(type, W4K, H4K);
                            fmt60.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
                            for (MediaCodecInfo.VideoCapabilities.PerformancePoint pp : pps) {
                                if (!supports4k30 && pp.covers(fmt30)) supports4k30 = true;
                                if (!supports4k60 && pp.covers(fmt60)) supports4k60 = true;
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                // 3) Fallback: frame-rate range for 4K size
                if (supports4kSize && (!supports4k30 || !supports4k60)) {
                    try {
                        Range<Double> fr4k = v.getSupportedFrameRatesFor(W4K, H4K);
                        if (fr4k != null) {
                            double up = fr4k.getUpper();
                            if (up >= 30.0) supports4k30 = true;
                            if (up >= 60.0) supports4k60 = true;
                        }
                    } catch (Throwable ignored) {}
                }

                // ===== Basic properties =====
                Range<Integer> widths = null, heights = null;
                String supportedWidths = "", supportedHeights = "", bitrateRange = "";
                try {
                    widths = v.getSupportedWidths();
                    heights = v.getSupportedHeights();
                    if (widths != null) supportedWidths = widths.getLower() + "-" + widths.getUpper();
                    if (heights != null) supportedHeights = heights.getLower() + "-" + heights.getUpper();
                    bitrateRange = v.getBitrateRange().getLower() + "-" + v.getBitrateRange().getUpper();
                } catch (Throwable ignored) {}

                int wAlign = 0, hAlign = 0;
                try {
                    wAlign = v.getWidthAlignment();
                    hAlign = v.getHeightAlignment();
                } catch (Throwable ignored) {}

                // Profiles/levels
                JSONArray profiles = new JSONArray();
                boolean hintsHdr10bit = false;
                if (cap.profileLevels != null) {
                    for (MediaCodecInfo.CodecProfileLevel pl : cap.profileLevels) {
                        JSONObject p = new JSONObject();
                        p.put("profile", profileName(type, pl.profile));
                        // Add friendlier AVC level name if we can
                        if ("video/avc".equals(type)) {
                            p.put("levelName", avcLevelName(pl.level));
                        }
                        p.put("level", pl.level);
                        profiles.put(p);

                        // crude HDR-ish hint
                        if ("video/hevc".equals(type) &&
                                pl.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10) {
                            hintsHdr10bit = true;
                        }
                        if ("video/vp9".equals(type) &&
                                pl.profile == MediaCodecInfo.CodecProfileLevel.VP9Profile2) {
                            hintsHdr10bit = true;
                        }
                    }
                }

                boolean isHw = false, isSw = false, isVendor = false;
                if (Build.VERSION.SDK_INT >= 29) {
                    try { isHw = ci.isHardwareAccelerated(); } catch (Throwable ignored) {}
                    try { isSw = ci.isSoftwareOnly(); } catch (Throwable ignored) {}
                    try { isVendor = ci.isVendor(); } catch (Throwable ignored) {}
                }

                boolean tunneled = false, secure = false;
                try { tunneled = cap.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback); } catch (Throwable ignored) {}
                try { secure = cap.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback); } catch (Throwable ignored) {}

                JSONObject obj = new JSONObject();
                obj.put("codecName", ci.getName());
                obj.put("mime", type);
                obj.put("isHardwareAccelerated", isHw);
                obj.put("isSoftwareOnly", isSw);
                obj.put("isVendor", isVendor);
                obj.put("supportedWidths", supportedWidths);
                obj.put("supportedHeights", supportedHeights);
                obj.put("widthAlignment", wAlign);
                obj.put("heightAlignment", hAlign);
                obj.put("bitrateRange", bitrateRange);
                obj.put("profiles", profiles);
                obj.put("supports4kSize", supports4kSize);
                obj.put("supports4k_30fps", supports4k30);
                obj.put("supports4k_60fps", supports4k60);
                obj.put("supportsTunneled", tunneled);
                obj.put("supportsSecure", secure);
                obj.put("hintsHdr10bit", hintsHdr10bit);

                out.put(obj);
            }
        }

        JSONObject device = new JSONObject();
        device.put("brand", Build.BRAND);
        device.put("model", Build.MODEL);
        device.put("device", Build.DEVICE);
        device.put("android", Build.VERSION.RELEASE);
        device.put("sdk", Build.VERSION.SDK_INT);

        JSONObject root = new JSONObject();
        root.put("device", device);
        root.put("decoders", out);
        return root;
    }
}
