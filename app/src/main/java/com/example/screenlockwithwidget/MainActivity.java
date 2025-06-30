package com.example.screenlockwithwidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.app.admin.DevicePolicyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ComponentName adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        // Find views
        View cardGrant = findViewById(R.id.cardGrantPermissions);
        View cardRevoke = findViewById(R.id.cardRevokePermissions);
        Button btnRevoke = findViewById(R.id.btnRevokeAdmin);
        Button btnActivate = findViewById(R.id.btnActivateAdmin);
        Button btnShortcut = findViewById(R.id.btnAddShortcut);

        btnActivate.setOnClickListener(v -> activateDeviceAdmin());
        btnRevoke.setOnClickListener(view -> revokeDeviceAdmin(dpm, adminComponent, cardGrant, cardRevoke));
        btnShortcut.setOnClickListener(v -> addShortcut());

        updateViewCard(dpm, adminComponent, cardGrant, cardRevoke);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ComponentName adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        View cardGrant = findViewById(R.id.cardGrantPermissions);
        View cardRevoke = findViewById(R.id.cardRevokePermissions);

        updateViewCard(dpm, adminComponent, cardGrant, cardRevoke);
    }

    private void revokeDeviceAdmin(DevicePolicyManager dpm, ComponentName adminComponent, View cardGrant, View cardRevoke) {
        if (dpm.isAdminActive(adminComponent)) {
            dpm.removeActiveAdmin(adminComponent);

            // Delay to allow system to fully revoke admin
            new android.os.Handler().postDelayed(() -> {
                if (!dpm.isAdminActive(adminComponent)) {
                    updateViewCard(dpm, adminComponent, cardGrant, cardRevoke);
                }
            }, 300);
        }
    }

    private void activateDeviceAdmin() {
        ComponentName adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (dpm.isAdminActive(adminComponent)) {
            Toast.makeText(this, "Device admin is already active.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Needed to lock the screen.");
            startActivity(intent);
        }
    }

    private void addShortcut() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            Intent shortcutIntent = new Intent(this, LockScreenActivity.class);
            shortcutIntent.setAction("com.example.screenlockwithwidget.LOCK_SCREEN_ACTION");  // Custom action
            shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "lock_screen_shortcut")
                    .setShortLabel("Lock")
                    .setLongLabel("Quickly lock the screen")
                    .setIcon(Icon.createWithResource(this, R.drawable.lock_icon))
                    .setIntent(shortcutIntent)
                    .build();

            Intent pinnedShortcutCallbackIntent = new Intent(this, ShortcutAddedReceiver.class);
            PendingIntent successCallback = PendingIntent.getBroadcast(this, 0, pinnedShortcutCallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            shortcutManager.requestPinShortcut(shortcut, successCallback.getIntentSender());
        } else {
            Toast.makeText(this, "Your device doesn't support adding shortcuts.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void updateViewCard(DevicePolicyManager dpm, ComponentName adminComponent, View cardGrant, View cardRevoke) {
        if (dpm.isAdminActive(adminComponent)) {
            cardGrant.setVisibility(View.GONE);
            cardRevoke.setVisibility(View.VISIBLE);
        } else {
            cardGrant.setVisibility(View.VISIBLE);
            cardRevoke.setVisibility(View.GONE);
        }
    }

}
