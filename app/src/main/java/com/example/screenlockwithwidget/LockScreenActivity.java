package com.example.screenlockwithwidget;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class LockScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, MyDeviceAdminReceiver.class);

        if (dpm.isAdminActive(admin)) {
            dpm.lockNow();
        }else{
            Toast.makeText(this, "Grant device admin permission to use this feature", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}

