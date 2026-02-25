package com.hospital.vitalwatch.stream.scheduler;

import com.hospital.vitalwatch.stream.dto.VitalSignsMessage;
import com.hospital.vitalwatch.stream.producer.VitalSignsStreamProducer;
import com.hospital.vitalwatch.stream.service.VitalSignsGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
public class VitalSignsScheduler {

    private final VitalSignsGeneratorService generatorService;
    private final VitalSignsStreamProducer streamProducer;
    private boolean streamEnabled;

    public VitalSignsScheduler(
            VitalSignsGeneratorService generatorService,
            VitalSignsStreamProducer streamProducer,
            @Value("${stream.generation.enabled:true}") boolean streamEnabled) {
        this.generatorService = generatorService;
        this.streamProducer = streamProducer;
        this.streamEnabled = streamEnabled;
    }

    @PostConstruct
    public void init() {
        if (streamEnabled) {
            log.info("🚀 Stream Generator iniciado - Generando signos vitales cada 1 segundo");
            log.info("📊 Tópico: {}", streamProducer.getTopicName());
        } else {
            log.warn("⚠️ Stream Generator deshabilitado por configuración");
        }
    }

    @Scheduled(fixedRate = 1000)
    public void generateAndPublishVitalSigns() {
        if (!streamEnabled) {
            return;
        }

        try {
            VitalSignsMessage message = generatorService.generateRandomVitalSigns();
            
            streamProducer.publishVitalSigns(message);
            
        } catch (Exception e) {
            log.error("❌ Error en generación programada: {}", e.getMessage(), e);
        }
    }

    public void enableStream() {
        this.streamEnabled = true;
        log.info("✅ Stream habilitado");
    }

    public void disableStream() {
        this.streamEnabled = false;
        log.info("⏸️ Stream pausado");
    }

    public boolean isStreamEnabled() {
        return streamEnabled;
    }
}
