package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Incidencia;
import org.example.tfgbackend.service.ClienteService;
import org.example.tfgbackend.service.EmpleadoService;

import java.time.LocalDateTime;

import org.example.tfgbackend.service.IncidenciaService;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaDTO {
    private Long incidenciaId;
    private Long clienteId;
    private Long empleadoId;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado;
    private String nombreCliente;
    private LocalDateTime fechaCreacion;

    public static IncidenciaDTO fromEntity(Incidencia incidencia) {
        if (incidencia == null) return null;
        return IncidenciaDTO.builder()
                .incidenciaId(incidencia.getIncidenciaId())
                .clienteId(incidencia.getCliente() != null ? incidencia.getCliente().getClienteId() : null)
                .nombreCliente(incidencia.getCliente() != null ? incidencia.getCliente().getNombre() + " " + incidencia.getCliente().getApellido() : "Desconocido")
                .empleadoId(incidencia.getEmpleado() != null ? incidencia.getEmpleado().getEmpleadoId() : null)
                .titulo(incidencia.getTitulo())
                .descripcion(incidencia.getDescripcion())
                .prioridad(incidencia.getPrioridad())
                .estado(incidencia.getEstado())
                .fechaCreacion(incidencia.getFechaCreacion())
                .build();
    }

    public Incidencia toEntity(ClienteService clienteService, EmpleadoService empleadoService) {
        Incidencia incidencia = Incidencia.builder()
                .incidenciaId(this.incidenciaId)
                .titulo(this.titulo)
                .descripcion(this.descripcion)
                .prioridad(this.prioridad)
                .estado(this.estado)
                .fechaCreacion(this.fechaCreacion)
                .build();

        if (this.clienteId != null) {
            clienteService.findById(this.clienteId).ifPresent(incidencia::setCliente);
        }
        if (this.empleadoId != null) {
            empleadoService.findById(this.empleadoId).ifPresent(incidencia::setEmpleado);
        }

        return incidencia;
    }

    public static ResponseEntity<?> executeGetByCliente(Long clienteId, ClienteService clienteService, IncidenciaService incidenciaService) {
        return clienteService.findById(clienteId)
                .map(cliente -> incidenciaService.findByCliente(cliente).stream()
                        .map(IncidenciaDTO::fromEntity)
                        .collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static ResponseEntity<?> executeGetById(Long id, IncidenciaService incidenciaService) {
        return incidenciaService.findById(id)
                .map(IncidenciaDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static ResponseEntity<?> executeDelete(Long id, IncidenciaService incidenciaService) {
        return incidenciaService.findById(id)
                .map(incidencia -> {
                    incidenciaService.deleteById(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
