# Simple-Image-Analyzer

A Spring Boot application for color detection and sharp object detection in images using OpenCV.

## Features

- Color detection in images
- Sharp object detection (knives, scissors, etc.)
- Web interface for uploading and analyzing images

## Prerequisites

- Docker and Docker Compose

## Running with Docker

### Option 1: Using Docker Compose (Recommended)

1. Clone the repository:
   ```
   git clone <repository-url>
   cd colordetector
   ```

2. Build and start the application:
   ```
   docker-compose up -d
   ```

3. Access the application at http://localhost:8080

4. To stop the application:
   ```
   docker-compose down
   ```

### Option 2: Using Docker directly

1. Build the Docker image:
   ```
   docker build -t colordetector .
   ```

2. Run the container:
   ```
   docker run -p 8080:8080 colordetector
   ```

3. Access the application at http://localhost:8080

## Troubleshooting

- If you encounter issues with OpenCV loading, check the container logs:
  ```
  docker-compose logs
  ```

- Make sure port 8080 is not already in use on your host machine.

## Development

To make changes to the application:

1. Modify the source code
2. Rebuild the Docker image:
   ```
   docker-compose build
   ```
3. Restart the container:
   ```
   docker-compose up -d
   ```
