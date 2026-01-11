package com.indrive.decision;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private static final int OVERLAY_PERMISSION_CODE = 1001;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btnStart = findViewById(R.id.btnStart);
        Button btnAccessibility = findViewById(R.id.btnAccessibility);
        
        btnStart.setOnClickListener(v -> {
            if (checkOverlayPermission()) {
                startOverlayService();
            }
        });
        
        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Activa 'InDrive Helper' en la lista", Toast.LENGTH_LONG).show();
        });
    }
    
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }
    
    private void startOverlayService() {
        Intent serviceIntent = new Intent(this, OverlayService.class);
        startService(serviceIntent);
        Toast.makeText(this, "Overlay activado", Toast.LENGTH_SHORT).show();
    }
}
