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
public class EmpleadoCreateDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String responsabilidad;
    private Rol rol;

    public Empleado toEntity() {
        // Por defecto, si el correo termina en @incidencias.com es EMPLEADO, sino ADMINISTRADOR
        Rol assignedRol = this.rol;
        if (assignedRol == null) {
            assignedRol = (this.correo != null && this.correo.toLowerCase().endsWith("@incidencias.com")) 
                    ? Rol.EMPLEADO : Rol.ADMINISTRADOR;
        }

        return Empleado.builder()
                .nombre(this.nombre)
                .apellido(this.apellido)
                .correo(this.correo)
                .contrasena(this.contrasena)
                .responsabilidad(this.responsabilidad)
                .rol(assignedRol)
                .build();
    }

    public ResponseEntity<?> executeCreate(EmpleadoService empleadoService) {
        Empleado saved = empleadoService.save(this.toEntity());
        return ResponseEntity.ok(EmpleadoDTO.fromEntity(saved));
    }
}
