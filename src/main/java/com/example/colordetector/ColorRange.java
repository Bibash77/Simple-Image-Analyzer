package com.example.colordetector;

import org.opencv.core.Scalar;

import java.util.Arrays;
import java.util.List;

public class ColorRange {
    public String name;
    public Scalar lower;
    public Scalar upper;
    public Scalar boxColor;

    public ColorRange(String name, Scalar lower, Scalar upper, Scalar boxColor) {
        this.name = name;
        this.lower = lower;
        this.upper = upper;
        this.boxColor = boxColor;
    }

    public static List<ColorRange> getPredefined() {
        return Arrays.asList(
                new ColorRange("Red", new Scalar(0, 120, 70), new Scalar(10, 255, 255), new Scalar(0, 0, 255)),
                new ColorRange("Red", new Scalar(170, 120, 70), new Scalar(180, 255, 255), new Scalar(0, 0, 255)),
                new ColorRange("Orange", new Scalar(11, 100, 100), new Scalar(25, 255, 255), new Scalar(0, 165, 255)),
                new ColorRange("Yellow", new Scalar(26, 100, 100), new Scalar(35, 255, 255), new Scalar(0, 255, 255)),
                new ColorRange("Green", new Scalar(36, 50, 70), new Scalar(89, 255, 255), new Scalar(0, 255, 0)),
                new ColorRange("Cyan", new Scalar(80, 100, 100), new Scalar(95, 255, 255), new Scalar(255, 255, 0)),
                new ColorRange("Blue", new Scalar(96, 50, 70), new Scalar(128, 255, 255), new Scalar(255, 0, 0)),
                new ColorRange("Purple", new Scalar(129, 50, 70), new Scalar(158, 255, 255), new Scalar(255, 0, 255)),
                new ColorRange("Pink", new Scalar(159, 50, 70), new Scalar(169, 255, 255), new Scalar(203, 192, 255)),
                new ColorRange("Brown", new Scalar(10, 100, 20), new Scalar(20, 255, 200), new Scalar(42, 42, 165)),
                new ColorRange("White", new Scalar(0, 0, 200), new Scalar(180, 30, 255), new Scalar(255, 255, 255)),
                new ColorRange("Black", new Scalar(0, 0, 0), new Scalar(180, 255, 50), new Scalar(0, 0, 0))
        );
    }
}
