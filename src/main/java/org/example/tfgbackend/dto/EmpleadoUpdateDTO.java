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
public class EmpleadoUpdateDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String responsabilidad;
    private Rol rol;

    public void updateEntity(Empleado empleado) {
        if (this.nombre != null) empleado.setNombre(this.nombre);
        if (this.apellido != null) empleado.setApellido(this.apellido);
        if (this.correo != null) empleado.setCorreo(this.correo);
        if (this.contrasena != null) empleado.setContrasena(this.contrasena);
        if (this.responsabilidad != null) empleado.setResponsabilidad(this.responsabilidad);
        
        if (this.rol != null) {
            empleado.setRol(this.rol);
        } else if (this.correo != null) {
            // Recalcular rol si cambia el correo y no se especifica rol
            Rol assignedRol = this.correo.toLowerCase().endsWith("@incidencias.com") 
                    ? Rol.EMPLEADO : Rol.ADMINISTRADOR;
            empleado.setRol(assignedRol);
        }
    }

    public ResponseEntity<?> executeUpdate(Long id, EmpleadoService empleadoService) {
        return empleadoService.findById(id)
                .map(empleado -> {
                    this.updateEntity(empleado);
                    Empleado updated = empleadoService.save(empleado);
                    return ResponseEntity.ok(EmpleadoDTO.fromEntity(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
