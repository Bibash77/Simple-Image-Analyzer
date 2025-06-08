package com.example.colordetector.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SharpObjectDetector {
    private static final String[] CLASS_LABELS = {
            "person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train",
            "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter",
            "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear",
            "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase",
            "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat",
            "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle",
            "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut",
            "cake", "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor",
            "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven",
            "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors",
            "teddy bear", "hair drier", "toothbrush"
    };

    private final List<String> sharpLabels = List.of("knife", "scissors");
    public  byte[] detectSharpObjectsAndReturnImage(Mat img) throws IOException {
        // Load YOLOv5 ONNX model
        InputStream modelStream = getClass().getClassLoader().getResourceAsStream("models/yolov5m.onnx");
        File tempFile = File.createTempFile("yolov5s", ".onnx");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(modelStream, out);
        }
        Net net = Dnn.readNetFromONNX(tempFile.getAbsolutePath());

        // Preprocess image
        Size inputSize = new Size(640, 640);
        Mat blob = Dnn.blobFromImage(img, 1.0 / 255.0, inputSize, new Scalar(0, 0, 0), true, false);
        net.setInput(blob);

        // Inference
        List<Mat> outputs = new ArrayList<>();
        net.forward(outputs, net.getUnconnectedOutLayersNames());

        float confThreshold = 0.2f;
        List<String> sharpObjects = List.of("knife", "scissors");

        int rows = img.rows();
        int cols = img.cols();

        for (Mat output : outputs) {
            for (int i = 0; i < output.rows(); i++) {
                Mat row = output.row(i);
                float confidence = (float) row.get(0, 4)[0];

                if (confidence >= confThreshold) {
                    Mat scores = row.colRange(5, row.cols());
                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                    int classId = (int) mm.maxLoc.x;
                    float classScore = (float) mm.maxVal;

                    if (classScore > confThreshold) {
                        String label = CLASS_LABELS[classId];
                            float cx = (float) row.get(0, 0)[0] * cols;
                            float cy = (float) row.get(0, 1)[0] * rows;
                            float w = (float) row.get(0, 2)[0] * cols;
                            float h = (float) row.get(0, 3)[0] * rows;

                            int left = (int) (cx - w / 2);
                            int top = (int) (cy - h / 2);

                            // Draw box and label
                            Imgproc.rectangle(img, new Point(left, top), new Point(left + w, top + h), new Scalar(0, 255, 0), 2);
                            Imgproc.putText(img, label, new Point(left, top - 5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 255, 0), 2);
                    }
                }
            }
        }

        // Convert Mat to BufferedImage and return
        return encodePng(img);
    }

    private byte[] encodePng(Mat img) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", img, buffer);
        return buffer.toArray();
    }

}
