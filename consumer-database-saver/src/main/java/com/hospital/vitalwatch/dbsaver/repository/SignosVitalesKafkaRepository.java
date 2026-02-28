package com.hospital.vitalwatch.dbsaver.repository;

import com.hospital.vitalwatch.dbsaver.model.SignosVitalesKafka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SignosVitalesKafkaRepository extends JpaRepository<SignosVitalesKafka, Long> {
    
    List<SignosVitalesKafka> findByPacienteIdOrderByTimestampMedicionDesc(String pacienteId);
    
    @Query("SELECT COUNT(sv) FROM SignosVitalesKafka sv WHERE sv.timestampMedicion >= :from")
    Long countByTimestampAfter(LocalDateTime from);
    
    @Query("SELECT DISTINCT sv.pacienteId FROM SignosVitalesKafka sv WHERE sv.timestampMedicion >= :from")
    List<String> findDistinctPacientesAfter(LocalDateTime from);
}
