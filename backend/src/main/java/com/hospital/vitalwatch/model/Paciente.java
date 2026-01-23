package com.hospital.vitalwatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Paciente
 * Representa un paciente hospitalizado en el sistema VitalWatch
 */
@Entity
@Table(name = "PACIENTES")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pacientes")
    @SequenceGenerator(name = "seq_pacientes", sequenceName = "SEQ_PACIENTES", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "Formato de RUT inválido (ej: 12345678-9)")
    @Column(name = "RUT", nullable = false, unique = true, length = 12)
    private String rut;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Column(name = "FECHA_NACIMIENTO", nullable = false)
    private LocalDate fechaNacimiento;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
    @Max(value = 120, message = "La edad debe ser menor o igual a 120")
    @Column(name = "EDAD", nullable = false)
    private Integer edad;

    @NotNull(message = "El género es obligatorio")
    @Pattern(regexp = "[MFO]", message = "El género debe ser M, F u O")
    @Column(name = "GENERO", nullable = false, length = 1)
    private String genero;

    @NotBlank(message = "La sala es obligatoria")
    @Size(max = 20, message = "La sala no puede exceder 20 caracteres")
    @Column(name = "SALA", nullable = false, length = 20)
    private String sala;

    @NotBlank(message = "La cama es obligatoria")
    @Size(max = 10, message = "La cama no puede exceder 10 caracteres")
    @Column(name = "CAMA", nullable = false, length = 10)
    private String cama;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ESTABLE|MODERADO|CRÍTICO|RECUPERACIÓN", 
             message = "El estado debe ser: ESTABLE, MODERADO, CRÍTICO o RECUPERACIÓN")
    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado = "ESTABLE";

    @Size(max = 500, message = "El diagnóstico no puede exceder 500 caracteres")
    @Column(name = "DIAGNOSTICO", length = 500)
    private String diagnostico;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Column(name = "FECHA_INGRESO", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "FECHA_ALTA")
    private LocalDateTime fechaAlta;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // Relaciones
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SignosVitales> signosVitales = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alerta> alertas = new ArrayList<>();

    /**
     * Constructor para creación manual sin ID
     */
    public Paciente(String nombre, String apellido, String rut, LocalDate fechaNacimiento,
                    Integer edad, String genero, String sala, String cama, String estado,
                    String diagnostico, LocalDateTime fechaIngreso) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.genero = genero;
        this.sala = sala;
        this.cama = cama;
        this.estado = estado;
        this.diagnostico = diagnostico;
        this.fechaIngreso = fechaIngreso;
    }

    /**
     * Método helper para obtener nombre completo
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Método para verificar si el paciente está en estado crítico
     */
    public boolean esCritico() {
        return "CRÍTICO".equals(this.estado);
    }

    @PrePersist
    protected void onCreate() {
        if (fechaIngreso == null) {
            fechaIngreso = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "ESTABLE";
        }
    }
}
