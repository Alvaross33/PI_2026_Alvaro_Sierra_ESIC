package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Incidencia;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.service.ClienteService;
import org.example.tfgbackend.service.EmpleadoService;
import java.time.LocalDateTime;

import org.example.tfgbackend.service.IncidenciaService;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaUpdateDTO {
    private Long clienteId;
    private Long empleadoId;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado;
    private LocalDateTime fechaCreacion;

    public void updateEntity(Incidencia incidencia, ClienteService clienteService, EmpleadoService empleadoService) {
        if (this.clienteId != null) {
            clienteService.findById(this.clienteId).ifPresent(incidencia::setCliente);
        }
        if (this.empleadoId != null) {
            empleadoService.findById(this.empleadoId).ifPresent(incidencia::setEmpleado);
        }
        incidencia.setTitulo(this.titulo);
        incidencia.setDescripcion(this.descripcion);
        incidencia.setPrioridad(this.prioridad);
        incidencia.setEstado(this.estado);
        incidencia.setFechaCreacion(this.fechaCreacion);
    }

    public ResponseEntity<?> executeUpdate(Long id, IncidenciaService incidenciaService, ClienteService clienteService, EmpleadoService empleadoService) {
        return incidenciaService.findById(id)
                .map(incidencia -> {
                    this.updateEntity(incidencia, clienteService, empleadoService);
                    Incidencia updated = incidenciaService.save(incidencia);
                    return ResponseEntity.ok(IncidenciaDTO.fromEntity(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
