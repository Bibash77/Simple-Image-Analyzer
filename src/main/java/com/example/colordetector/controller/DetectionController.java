package com.example.colordetector.controller;

import com.example.colordetector.pojo.ColorRange;
import com.example.colordetector.loader.OpenCVLoader;
import com.example.colordetector.service.SharpObjectDetector;
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

    private static final int SHAPE_AREA_THRESHOLD = 300;
    private static final int COLOR_AREA_THRESHOLD = 500;
    private static final double SQUARE_RATIO_TOLERANCE = 0.1;

    private final SharpObjectDetector sharpObjectDetector;
    static {
        OpenCVLoader.init();
    }

    public DetectionController(SharpObjectDetector sharpObjectDetector) {
        this.sharpObjectDetector = sharpObjectDetector;
    }

    @PostMapping(value = "/process-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] processImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mode") String mode) throws IOException {

        Mat img = Imgcodecs.imdecode(new MatOfByte(file.getBytes()), Imgcodecs.IMREAD_COLOR);
        if (img.empty()) {
            throw new IOException("Invalid image data.");
        }

        return switch (mode.toLowerCase()) {
            case "color" -> detectColors(img);
            case "bright" -> detectBrightestArea(img);
            case "shape" -> detectShapes(img);
            case "sharp" -> sharpObjectDetector.detectSharpObjectsAndReturnImage(img);
            default -> {
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".png", img, buffer);
                yield buffer.toArray();
            }
        };
    }

    private byte[] detectShapes(Mat img) {
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat thresh = new Mat();

        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);
        Imgproc.threshold(blurred, thresh, 60, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Map<String, Integer> shapeCount = new HashMap<>();

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < SHAPE_AREA_THRESHOLD) continue;

            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(curve, approx, 0.04 * Imgproc.arcLength(curve, true), true);

            int vertices = (int) approx.total();
            String shape = switch (vertices) {
                case 3 -> "Triangle";
                case 4 -> {
                    Rect rect = Imgproc.boundingRect(contour);
                    double ratio = (double) rect.width / rect.height;
                    yield (Math.abs(ratio - 1.0) <= SQUARE_RATIO_TOLERANCE) ? "Square" : "Rectangle";
                }
                default -> "Circle";
            };

            shapeCount.merge(shape, 1, Integer::sum);
            Imgproc.drawContours(img, Collections.singletonList(contour), -1, new Scalar(0, 255, 0), 2);
            Imgproc.putText(img, shape, Imgproc.boundingRect(contour).tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(255, 0, 0), 2);
        }

        int y = 30;
        for (Map.Entry<String, Integer> entry : shapeCount.entrySet()) {
            Imgproc.putText(img, entry.getKey() + ": " + entry.getValue(), new Point(10, y),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
            y += 30;
        }

        return encodePng(img);
    }

    private byte[] detectColors(Mat img) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);

        for (ColorRange color : ColorRange.getPredefined()) {
            Mat mask = new Mat();
            Core.inRange(hsv, color.lower, color.upper, mask);

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_DILATE, kernel);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            for (MatOfPoint contour : contours) {
                if (Imgproc.contourArea(contour) > COLOR_AREA_THRESHOLD) {
                    Rect rect = Imgproc.boundingRect(contour);
                    Imgproc.rectangle(img, rect, color.boxColor, 3);
                    Imgproc.putText(img, color.name + " Object", new Point(rect.x, rect.y - 10),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, color.boxColor, 2);
                }
            }
        }

        return encodePng(img);
    }

    private byte[] detectBrightestArea(Mat img) {
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        Core.MinMaxLocResult mmr = Core.minMaxLoc(gray);
        Point maxLoc = mmr.maxLoc;

        Imgproc.circle(img, maxLoc, 20, new Scalar(0, 255, 255), 3);
        Imgproc.putText(img, "Brightest", new Point(maxLoc.x + 10, maxLoc.y - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 255, 255), 2);

        return encodePng(img);
    }

    private byte[] encodePng(Mat img) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", img, buffer);
        return buffer.toArray();
    }
}
