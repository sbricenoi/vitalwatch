package com.hospital.vitalwatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.vitalwatch.dto.PacienteDTO;
import com.hospital.vitalwatch.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PacienteController
 */
@WebMvcTest(PacienteController.class)
@WithMockUser
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacienteService pacienteService;

    private PacienteDTO pacienteDTO;

    @BeforeEach
    void setUp() {
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
    void obtenerTodos_DeberiaRetornar200ConListaDePacientes() throws Exception {
        // Given
        List<PacienteDTO> pacientes = Arrays.asList(pacienteDTO);
        when(pacienteService.obtenerTodos()).thenReturn(pacientes);

        // When & Then
        mockMvc.perform(get("/api/v1/pacientes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nombre").value("Juan"));
    }

    @Test
    void obtenerPorId_DeberiaRetornar200ConPaciente() throws Exception {
        // Given
        when(pacienteService.obtenerPorId(1L)).thenReturn(pacienteDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/pacientes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.nombre").value("Juan"))
                .andExpect(jsonPath("$.data.apellido").value("Pérez"));
    }

    @Test
    void crear_DeberiaRetornar201ConPacienteCreado() throws Exception {
        // Given
        when(pacienteService.crear(any(PacienteDTO.class))).thenReturn(pacienteDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/pacientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.data.nombre").value("Juan"));
    }

    @Test
    void actualizar_DeberiaRetornar200ConPacienteActualizado() throws Exception {
        // Given
        when(pacienteService.actualizar(anyLong(), any(PacienteDTO.class)))
                .thenReturn(pacienteDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/pacientes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.nombre").value("Juan"));
    }

    @Test
    void buscarPorEstado_DeberiaRetornar200ConPacientesFiltrados() throws Exception {
        // Given
        List<PacienteDTO> pacientes = Arrays.asList(pacienteDTO);
        when(pacienteService.buscarPorEstado("ESTABLE")).thenReturn(pacientes);

        // When & Then
        mockMvc.perform(get("/api/v1/pacientes/buscar/estado/ESTABLE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].estado").value("ESTABLE"));
    }
}
