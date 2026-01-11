package com.indrive.decision;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {
    
    private WindowManager windowManager;
    private View overlayView;
    private static OverlayService instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;
        
        windowManager.addView(overlayView, params);
        hideOverlay();
    }
    
    public static void updateDecision(boolean accept, int pricePerKm, String reason) {
        if (instance != null && instance.overlayView != null) {
            instance.showDecision(accept, pricePerKm, reason);
        }
    }
    
    private void showDecision(boolean accept, int pricePerKm, String reason) {
        overlayView.setVisibility(View.VISIBLE);
        overlayView.setBackgroundColor(accept ? 0xFF4CAF50 : 0xFFF44336);
        
        TextView tvDecision = overlayView.findViewById(R.id.tvDecision);
        TextView tvPrice = overlayView.findViewById(R.id.tvPrice);
        TextView tvReason = overlayView.findViewById(R.id.tvReason);
        
        tvDecision.setText(accept ? "ACEPTA" : "RECHAZA");
        tvPrice.setText("$" + pricePerKm + "/km");
        tvReason.setText(reason);
        
        overlayView.postDelayed(this::hideOverlay, 4000);
    }
    
    private void hideOverlay() {
        if (overlayView != null) {
            overlayView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
