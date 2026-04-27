package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.model.Rol;

import org.example.tfgbackend.service.EmpleadoService;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoDTO {
    private Long empleadoId;
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String responsabilidad;
    private Rol rol;

    public static EmpleadoDTO fromEntity(Empleado empleado) {
        if (empleado == null) return null;
        return EmpleadoDTO.builder()
                .empleadoId(empleado.getEmpleadoId())
                .nombre(empleado.getNombre())
                .apellido(empleado.getApellido())
                .correo(empleado.getCorreo())
                .contrasena(empleado.getContrasena())
                .responsabilidad(empleado.getResponsabilidad())
                .rol(empleado.getRol())
                .build();
    }

    public Empleado toEntity() {
        return Empleado.builder()
                .empleadoId(this.empleadoId)
                .nombre(this.nombre)
                .apellido(this.apellido)
                .correo(this.correo)
                .contrasena(this.contrasena)
                .responsabilidad(this.responsabilidad)
                .rol(this.rol)
                .build();
    }

    public static ResponseEntity<?> executeDelete(Long id, EmpleadoService empleadoService) {
        return empleadoService.findById(id)
                .map(empleado -> {
                    empleadoService.deleteById(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
