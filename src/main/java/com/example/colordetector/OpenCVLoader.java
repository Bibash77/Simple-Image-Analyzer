package com.example.colordetector;

import nu.pattern.OpenCV;

public class OpenCVLoader {
    static {
        try {
            // Load the OpenCV native library using nu.pattern.OpenCV
            OpenCV.loadShared();
            System.out.println("✅ OpenCV library loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV native library: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void init() {
        // This method is used to trigger the static block
    }
}