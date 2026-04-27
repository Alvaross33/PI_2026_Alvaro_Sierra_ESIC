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
            clienteService.findById(this.empleadoId)
                    .filter(c -> c.getRol() == Rol.EMPLEADO || c.getRol() == Rol.ADMINISTRADOR)
                    .ifPresent(c -> {
                        Empleado e = empleadoService.findById(c.getClienteId())
                                .orElseGet(() -> {
                                    Empleado nuevo = Empleado.builder()
                                            .empleadoId(c.getClienteId())
                                            .nombre(c.getNombre())
                                            .apellido(c.getApellido())
                                            .correo(c.getCorreo())
                                            .contrasena(c.getContrasena())
                                            .responsabilidad("Sincronizado desde Cliente")
                                            .build();
                                    try {
                                        return empleadoService.save(nuevo);
                                    } catch (Exception ex) {
                                        return empleadoService.findById(c.getClienteId()).orElse(null);
                                    }
                                });
                        if (e != null) {
                            incidencia.setEmpleado(e);
                        }
                    });
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
