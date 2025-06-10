package com.example.colordetector;

import com.example.colordetector.loader.OpenCVLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ColordetectorApplication {
	static {
		OpenCVLoader.init();
	}
	public static void main(String[] args) {
		SpringApplication.run(ColordetectorApplication.class, args);
	}
	@PostConstruct
	public void init() {
		System.out.println("OpenCV loaded successfully");
	}
}
