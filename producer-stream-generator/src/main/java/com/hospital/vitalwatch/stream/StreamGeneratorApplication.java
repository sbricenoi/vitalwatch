package com.hospital.vitalwatch.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StreamGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamGeneratorApplication.class, args);
        System.out.println("✅ VitalWatch Stream Generator Producer iniciado");
        System.out.println("📊 Generando signos vitales cada 1 segundo...");
    }
}
