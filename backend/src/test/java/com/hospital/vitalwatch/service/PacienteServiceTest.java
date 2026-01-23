package com.hospital.vitalwatch.service;

import com.hospital.vitalwatch.dto.PacienteDTO;
import com.hospital.vitalwatch.exception.ResourceNotFoundException;
import com.hospital.vitalwatch.model.Paciente;
import com.hospital.vitalwatch.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PacienteService
 */
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteDTO pacienteDTO;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        paciente = Paciente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .fechaNacimiento(LocalDate.of(1980, 1, 1))
                .genero("M")
                .telefono("123456789")
                .email("juan.perez@example.com")
                .estado("ESTABLE")
                .fechaAdmision(LocalDateTime.now())
                .build();

        pacienteDTO = PacienteDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .fechaNacimiento(LocalDate.of(1980, 1, 1))
                .genero("M")
                .telefono("123456789")
                .email("juan.perez@example.com")
                .estado("ESTABLE")
                .build();
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaDePacientes() {
        // Given
        List<Paciente> pacientes = Arrays.asList(paciente);
        when(pacienteRepository.findAll()).thenReturn(pacientes);

        // When
        List<PacienteDTO> resultado = pacienteService.obtenerTodos();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        verify(pacienteRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_CuandoExiste_DeberiaRetornarPaciente() {
        // Given
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        // When
        PacienteDTO resultado = pacienteService.obtenerPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        assertEquals("Pérez", resultado.getApellido());
        verify(pacienteRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_CuandoNoExiste_DeberiaLanzarExcepcion() {
        // Given
        when(pacienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            pacienteService.obtenerPorId(999L);
        });
        verify(pacienteRepository, times(1)).findById(999L);
    }

    @Test
    void crear_DeberiaGuardarYRetornarPaciente() {
        // Given
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        // When
        PacienteDTO resultado = pacienteService.crear(pacienteDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    void actualizar_CuandoExiste_DeberiaActualizarYRetornar() {
        // Given
        PacienteDTO actualizacion = PacienteDTO.builder()
                .nombre("Juan Carlos")
                .apellido("Pérez García")
                .build();
        
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        // When
        PacienteDTO resultado = pacienteService.actualizar(1L, actualizacion);

        // Then
        assertNotNull(resultado);
        verify(pacienteRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    void eliminar_CuandoExiste_DeberiaEliminar() {
        // Given
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        doNothing().when(pacienteRepository).delete(any(Paciente.class));

        // When
        pacienteService.eliminar(1L);

        // Then
        verify(pacienteRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).delete(any(Paciente.class));
    }

    @Test
    void buscarPorEstado_DeberiaRetornarPacientesFiltrados() {
        // Given
        List<Paciente> pacientes = Arrays.asList(paciente);
        when(pacienteRepository.findByEstado("ESTABLE")).thenReturn(pacientes);

        // When
        List<PacienteDTO> resultado = pacienteService.buscarPorEstado("ESTABLE");

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("ESTABLE", resultado.get(0).getEstado());
        verify(pacienteRepository, times(1)).findByEstado("ESTABLE");
    }
}
