# Color, Brightness, and Shape Detection Web Application

## Overview

This project is a web-based image processing tool that uses **OpenCV** and **Spring Boot** to detect colors, the brightest area, and simple shapes in images captured from a webcam or uploaded by the user. The backend is responsible for processing images and returning annotated results, while the frontend handles image capture and display.

---

## Features

- **Color Detection:**  
  Detects predefined colors (Red, Green, Blue, Yellow) using HSV color space thresholds. The app highlights detected colored objects with bounding boxes and labels.

- **Brightness Detection:**  
  Finds the brightest point in the image (based on grayscale intensity) and marks it with a circle and label.

- **Shape Detection:**  
  Detects basic geometric shapes such as triangles, squares, rectangles, and circles by contour approximation, draws contours, labels each shape, and counts the number of detected shapes.

- **Multi-mode Processing:**  
  User can select between `"color"`, `"bright"`, or `"shape"` detection modes when uploading the image.

- **Webcam Integration:**  
  Capture images directly from the webcam and send them to the backend for processing.

---

## Technologies Used

- Java 11+  
- Spring Boot  
- OpenCV (Java bindings)  
- HTML5, JavaScript (Canvas and Fetch APIs)

---

## How It Works

### Backend

- **DetectionController.java** exposes a REST endpoint `/process-image` that accepts an image file and a `mode` parameter (`color`, `bright`, or `shape`).
- The uploaded image is decoded into an OpenCV `Mat` object.
- Depending on the selected mode:
  - **Color Detection:** Converts image to HSV color space, creates masks for predefined color ranges, finds contours, and draws bounding boxes with labels.
  - **Brightness Detection:** Converts image to grayscale, locates the brightest pixel, and marks it.
  - **Shape Detection:** Converts image to grayscale, applies thresholding, detects contours, approximates polygonal shapes, labels them, and counts occurrences.

### Frontend

- Uses the webcam API to stream live video.
- Allows the user to capture an image frame and send it to the backend via `fetch` POST request.
- Displays the processed image returned from the backend.

---

## Running the Project

1. **Setup OpenCV:**  
   Make sure OpenCV native libraries are installed and configured properly on your machine.

2. **Build and Run Backend:**  
   Use Maven or Gradle to build and run the Spring Boot application.  
   Example with Maven:  
   ```bash
   ./mvnw spring-boot:run
