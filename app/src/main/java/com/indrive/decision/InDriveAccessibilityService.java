package com.indrive.decision;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InDriveAccessibilityService extends AccessibilityService {
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) {
            return;
        }
        
        String packageName = event.getPackageName().toString();
        if (!packageName.contains("inDriver") && !packageName.contains("indrive")) {
            return;
        }
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            analyzeScreen(rootNode);
            rootNode.recycle();
        }
    }
    
    private void analyzeScreen(AccessibilityNodeInfo node) {
        String screenText = getFullText(node);
        
        double recogida = extractDistance(screenText, "recog|pickup|pick");
        double viaje = extractDistance(screenText, "viaje|trip|distancia|distance|dest");
        int precio = extractPrice(screenText);
        
        if (viaje > 0 && precio > 0) {
            evaluateRide(recogida, viaje, precio);
        }
    }
    
    private String getFullText(AccessibilityNodeInfo node) {
        StringBuilder text = new StringBuilder();
        if (node.getText() != null) {
            text.append(node.getText()).append(" ");
        }
        if (node.getContentDescription() != null) {
            text.append(node.getContentDescription()).append(" ");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                text.append(getFullText(child));
                child.recycle();
            }
        }
        return text.toString();
    }
    
    private double extractDistance(String text, String keywords) {
        String pattern = "(?i)(?:" + keywords + ")[^0-9]*(\\d+[.,]?\\d*)\\s*(?:km|k)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                String num = m.group(1).replace(",", ".");
                return Double.parseDouble(num);
            } catch (Exception e) {
                return 0;
            }
        }
        
        pattern = "(\\d+[.,]?\\d*)\\s*(?:km|k)";
        p = Pattern.compile(pattern);
        m = p.matcher(text);
        if (m.find()) {
            try {
                String num = m.group(1).replace(",", ".");
                return Double.parseDouble(num);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
    
    private int extractPrice(String text) {
        Pattern p = Pattern.compile("\\$\\s*(\\d{1,3}(?:[.,]\\d{3})*)|\\b(\\d{4,6})\\b");
        Matcher m = p.matcher(text);
        while (m.find()) {
            try {
                String num = m.group(1) != null ? m.group(1) : m.group(2);
                num = num.replace(".", "").replace(",", "");
                int value = Integer.parseInt(num);
                if (value >= 5000 && value <= 100000) {
                    return value;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return 0;
    }
    
    private void evaluateRide(double recogida, double viaje, int precio) {
        double precioPorKm = precio / viaje;
        boolean accept = recogida <= 2 && viaje <= 6 && precio >= 8000 && precioPorKm >= 3000;
        
        String reason;
        if (recogida > 2) reason = "Recogida lejos";
        else if (viaje > 6) reason = "Viaje largo";
        else if (precio < 8000) reason = "Precio bajo";
        else if (precioPorKm < 3000) reason = "$/km bajo";
        else reason = "✓ OK";
        
        OverlayService.updateDecision(accept, (int) precioPorKm, reason);
    }
    
    @Override
    public void onInterrupt() {}
}
