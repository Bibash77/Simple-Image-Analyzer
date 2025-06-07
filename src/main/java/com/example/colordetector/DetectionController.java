package com.example.colordetector;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
public class DetectionController {

    static {
        OpenCVLoader.init();
    }

    /**
     * A simple helper class to define a named color range in HSV color space.
     */
    static class ColorRange {
        String name;
        Scalar lower, upper;  // Lower and upper bounds in HSV
        Scalar boxColor;      // Color to draw on the output image

        ColorRange(String name, Scalar lower, Scalar upper, Scalar boxColor) {
            this.name = name;
            this.lower = lower;
            this.upper = upper;
            this.boxColor = boxColor;
        }
    }

    /**
     * Main endpoint that processes the uploaded image.
     *
     * @param file Image file uploaded from frontend
     * @param mode Type of processing: "color" or "bright"
     * @return Processed image as PNG byte array
     */
    @PostMapping(value = "/process-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] processImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mode") String mode) throws IOException {

        // Read raw bytes from uploaded file
        byte[] bytes = file.getBytes();

        // Decode image bytes into OpenCV Mat object (matrix)
        Mat img = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_COLOR);

        // Route based on the selected mode
        if ("color".equalsIgnoreCase(mode)) {
            return detectColors(img);
        } else if ("bright".equalsIgnoreCase(mode)) {
            return detectBrightestArea(img);
        }
        else if ("shape".equalsIgnoreCase(mode)) {
            return detectShapes(img);
        }
        else {
            // Return unmodified image if mode is unknown
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", img, buffer);
            return buffer.toArray();
        }
    }

    private byte[] detectShapes(Mat img) {
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat thresh = new Mat();

        // Convert to grayscale
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // Blur the image to reduce noise
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Apply binary threshold
        Imgproc.threshold(blurred, thresh, 60, 255, Imgproc.THRESH_BINARY);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Count shapes
        Map<String, Integer> shapeCount = new HashMap<>();

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < 300) continue; // filter small noise

            // Approximate the contour to reduce vertices
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(curve, approx, 0.04 * Imgproc.arcLength(curve, true), true);

            int vertices = (int) approx.total();
            String shape = "Unknown";

            if (vertices == 3) {
                shape = "Triangle";
            } else if (vertices == 4) {
                // Check for square vs rectangle
                Rect rect = Imgproc.boundingRect(contour);
                double ratio = (double) rect.width / rect.height;
                shape = (ratio >= 0.9 && ratio <= 1.1) ? "Square" : "Rectangle";
            } else if (vertices > 4) {
                shape = "Circle";
            }

            // Increment shape count
            shapeCount.put(shape, shapeCount.getOrDefault(shape, 0) + 1);

            // Draw contour and label
            Imgproc.drawContours(img, Collections.singletonList(contour), -1, new Scalar(0, 255, 0), 2);
            Point labelPos = Imgproc.boundingRect(contour).tl();
            Imgproc.putText(img, shape, labelPos, Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(255, 0, 0), 2);
        }

        // Draw shape count summary
        int y = 30;
        for (Map.Entry<String, Integer> entry : shapeCount.entrySet()) {
            Imgproc.putText(img, entry.getKey() + ": " + entry.getValue(), new Point(10, y),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
            y += 30;
        }

        // Encode result as PNG
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", img, buffer);
        return buffer.toArray();
    }

    /**
     * Detects predefined colors in the image and draws bounding boxes.
     */
    private byte[] detectColors(Mat img) {
        // Convert BGR image to HSV color space (better for color filtering)
        Mat hsv = new Mat();
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);

        // Define multiple HSV color ranges
        List<ColorRange> colorRanges = Arrays.asList(
                new ColorRange("Red", new Scalar(0, 120, 70), new Scalar(10, 255, 255), new Scalar(0, 0, 255)),
                new ColorRange("Red", new Scalar(170, 120, 70), new Scalar(180, 255, 255), new Scalar(0, 0, 255)),
                new ColorRange("Green", new Scalar(36, 50, 70), new Scalar(89, 255, 255), new Scalar(0, 255, 0)),
                new ColorRange("Blue", new Scalar(90, 50, 70), new Scalar(128, 255, 255), new Scalar(255, 0, 0)),
                new ColorRange("Yellow", new Scalar(20, 100, 100), new Scalar(35, 255, 255), new Scalar(0, 255, 255))
        );

        // Loop through each color range
        for (ColorRange color : colorRanges) {
            // Create binary mask where color is in range
            Mat mask = new Mat();
            Core.inRange(hsv, color.lower, color.upper, mask);

            // Clean noise in mask using morphological operations
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_DILATE, kernel);

            // Find contours (shapes) in the mask
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Filter and annotate significant contours
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > 500) {
                    Rect rect = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(img, rect, color.boxColor, 3);
                    Imgproc.putText(img, color.name + " Object", new Point(rect.x, rect.y - 10),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, color.boxColor, 2);
                }
            }
        }

        // Encode annotated image back to PNG format
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", img, buffer);
        return buffer.toArray();
    }

    /**
     * Detects the brightest point in the image and marks it with a circle.
     */
    private byte[] detectBrightestArea(Mat img) {
        // Convert to grayscale to focus on brightness only
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // Get the location of the maximum intensity pixel
        Core.MinMaxLocResult mmr = Core.minMaxLoc(gray);
        Point maxLoc = mmr.maxLoc;

        // Draw a circle and label at the brightest point
        Imgproc.circle(img, maxLoc, 20, new Scalar(0, 255, 255), 3);
        Imgproc.putText(img, "Brightest", new Point(maxLoc.x + 10, maxLoc.y - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 255, 255), 2);

        // Encode to PNG format
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", img, buffer);
        return buffer.toArray();
    }
}
