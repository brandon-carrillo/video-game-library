package edu.ranken.brandon_carrillo.game_library.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.R;

public class PlatformChooserDialog {
    private final AlertDialog dialog;
    private final Map<String, Boolean> supportedPlatforms;
    private final Map<String, Boolean> selectedPlatforms;

    public PlatformChooserDialog(
        @NonNull Context context,
        @NonNull Integer title,
        @Nullable Map<String, Boolean> supportedPlatforms,
        @Nullable Map<String, Boolean> selectedPlatforms,
        @NonNull OnChooseListener onChoose
    ) {

        // get inflater
        LayoutInflater inflater = LayoutInflater.from(context);

        //inflate layout
        View contentView = inflater.inflate(R.layout.platform_chooser_dialog, null, false);

        // find views
        ImageButton playstationButton = contentView.findViewById(R.id.playstationButton);
        ImageButton xboxButton = contentView.findViewById(R.id.xboxButton);
        ImageButton windowsButton = contentView.findViewById(R.id.windowsButton);
        ImageButton switchButton = contentView.findViewById(R.id.switchButton);

        // supported
        if (supportedPlatforms == null) {
            this.supportedPlatforms = null;
        } else {
            this.supportedPlatforms = supportedPlatforms;

            boolean playstation = Objects.equals(supportedPlatforms.get("playstation"), Boolean.TRUE);
            playstationButton.setEnabled(playstation);
            if (!playstation) {
                playstationButton.setVisibility(View.GONE);
            }

            boolean xbox = Objects.equals(supportedPlatforms.get("xbox"), Boolean.TRUE);
            xboxButton.setEnabled(xbox);
            if (!xbox) {
                xboxButton.setVisibility(View.GONE);
            }

            boolean windows = Objects.equals(supportedPlatforms.get("windows"), Boolean.TRUE);
            windowsButton.setEnabled(windows);
            if (!windows) {
                windowsButton.setVisibility(View.GONE);
            }

            boolean nSwitch = Objects.equals(supportedPlatforms.get("switch"), Boolean.TRUE);
            switchButton.setEnabled(nSwitch);
            if (!nSwitch) {
                switchButton.setVisibility(View.GONE);
            }
        }

        // selected
        if (selectedPlatforms == null) {
            this.selectedPlatforms = new HashMap<>();
        } else {
            this.selectedPlatforms = selectedPlatforms;

            boolean playstation = Objects.equals(selectedPlatforms.get("playstation"), Boolean.TRUE);

            if (playstation) {
                playstationButton.setImageResource(R.drawable.ic_playstation);
            }

            boolean xbox = Objects.equals(selectedPlatforms.get("xbox"), Boolean.TRUE);
            if (xbox) {
                xboxButton.setImageResource(R.drawable.ic_xbox);
            }

            boolean windows = Objects.equals(selectedPlatforms.get("windows"), Boolean.TRUE);
            if (windows) {
                windowsButton.setImageResource(R.drawable.ic_windows);
            }

            boolean nSwitch = Objects.equals(selectedPlatforms.get("switch"), Boolean.TRUE);
            if (nSwitch) {
                switchButton.setImageResource(R.drawable.ic_switch);
            }
        }


        // register listeners
        playstationButton.setOnClickListener((view) -> {
            boolean checked = !Objects.equals(this.selectedPlatforms.get("playstation"), Boolean.TRUE);
            this.selectedPlatforms.put("playstation", checked);
//            playstationButton.setBackgroundTintList();
//            playstationButton.setImageResource();
            if (checked) {
                playstationButton.setImageResource(R.drawable.ic_playstation);
            } else {
                playstationButton.setImageResource(R.drawable.ic_playstation_outline);
            }

        });
        xboxButton.setOnClickListener((view) -> {
            boolean checked = !Objects.equals(this.selectedPlatforms.get("xbox"), Boolean.TRUE);
            this.selectedPlatforms.put("xbox", checked);
            if (checked) {
                xboxButton.setImageResource(R.drawable.ic_xbox);
            } else {
                xboxButton.setImageResource(R.drawable.ic_xbox_outline);
            }
        });
        windowsButton.setOnClickListener((view) -> {
            boolean checked = !Objects.equals(this.selectedPlatforms.get("windows"), Boolean.TRUE);
            this.selectedPlatforms.put("windows", checked);
            if (checked) {
                windowsButton.setImageResource(R.drawable.ic_windows);
            } else {
                windowsButton.setImageResource(R.drawable.ic_windows_outline);
            }

        });
        switchButton.setOnClickListener((view) -> {
            boolean checked = !Objects.equals(this.selectedPlatforms.get("switch"), Boolean.TRUE);
            this.selectedPlatforms.put("switch", checked);
            if (checked) {
                switchButton.setImageResource(R.drawable.ic_switch);
            } else {
                switchButton.setImageResource(R.drawable.ic_switch_outline);
            }

        });

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(contentView);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            onChoose.onChoose(this.selectedPlatforms);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            // do nothing
        });
        dialog = builder.create();

    }

    public void show() { dialog.show(); }
    public void cancel() { dialog.cancel(); }


    public interface OnChooseListener {
        void onChoose(@NonNull Map<String, Boolean> selectedPlatforms);
    }


}
