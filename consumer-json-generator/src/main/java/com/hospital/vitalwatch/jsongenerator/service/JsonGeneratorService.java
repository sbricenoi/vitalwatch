package com.hospital.vitalwatch.jsongenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.vitalwatch.jsongenerator.dto.AlertMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio consumidor que recibe alertas de RabbitMQ
 * y genera archivos JSON
 */
@Service
@Slf4j
public class JsonGeneratorService {

    @Value("${json.output.path:/app/data/alerts}")
    private String outputPath;

    private final ObjectMapper objectMapper;
    private long filesGenerated = 0;
    private long filesFailed = 0;

    public JsonGeneratorService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(outputPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("📁 Directorio de salida creado: {}", outputPath);
            } else {
                log.info("📁 Directorio de salida encontrado: {}", outputPath);
            }
        } catch (IOException e) {
            log.error("❌ Error creando directorio de salida: {}", outputPath, e);
        }
    }

    /**
     * Listener que consume mensajes de la cola vital-signs-alerts
     */
    @RabbitListener(queues = "${rabbitmq.queue.alerts}")
    public void consumeAlert(AlertMessage alertMessage) {
        try {
            log.info("📥 Alerta recibida desde RabbitMQ: Paciente {} - Severidad: {} - {} anomalías",
                    alertMessage.getPacienteId(),
                    alertMessage.getSeverity(),
                    alertMessage.getAnomalies().size());

            // Generar archivo JSON
            String fileName = generateFileName(alertMessage);
            File outputFile = new File(outputPath, fileName);

            objectMapper.writeValue(outputFile, alertMessage);
            filesGenerated++;

            log.info("✅ Archivo JSON generado: {} - Total generados: {}",
                    fileName, filesGenerated);

        } catch (Exception e) {
            filesFailed++;
            log.error("❌ Error generando archivo JSON: {} - Total fallidos: {}",
                    e.getMessage(), filesFailed, e);
        }
    }

    /**
     * Genera nombre de archivo único basado en timestamp y paciente
     * Formato: alert_YYYYMMDD_HHmmss_SSS_pacienteID_severity.json
     */
    private String generateFileName(AlertMessage alert) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
        String timestamp = now.format(formatter);
        
        String severity = alert.getSeverity().toLowerCase();
        Long pacienteId = alert.getPacienteId();
        
        return String.format("alert_%s_P%d_%s.json", 
                timestamp, pacienteId, severity);
    }

    /**
     * Obtiene estadísticas del generador
     */
    public GeneratorStats getStats() {
        File dir = new File(outputPath);
        long totalFilesInDirectory = 0;
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            totalFilesInDirectory = (files != null) ? files.length : 0;
        }
        
        return new GeneratorStats(filesGenerated, filesFailed, totalFilesInDirectory, outputPath);
    }

    public static class GeneratorStats {
        private long filesGenerated;
        private long filesFailed;
        private long totalFilesInDirectory;
        private String outputPath;

        public GeneratorStats(long filesGenerated, long filesFailed, long totalFilesInDirectory, String outputPath) {
            this.filesGenerated = filesGenerated;
            this.filesFailed = filesFailed;
            this.totalFilesInDirectory = totalFilesInDirectory;
            this.outputPath = outputPath;
        }

        public long getFilesGenerated() {
            return filesGenerated;
        }

        public long getFilesFailed() {
            return filesFailed;
        }

        public long getTotalFilesInDirectory() {
            return totalFilesInDirectory;
        }

        public String getOutputPath() {
            return outputPath;
        }
    }
}
