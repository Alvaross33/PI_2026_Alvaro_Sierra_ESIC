package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.service.ClienteService;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * DTO utilizado para la creación de un nuevo Cliente.
 * Contiene la información requerida durante el proceso de registro en el sistema.
 */
public class ClienteCreateDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;

    private Rol rol;
    private Boolean activo;

    public Cliente toEntity() {
        Rol assignedRol;
        if (this.rol == Rol.ADMINISTRADOR) {
            assignedRol = Rol.ADMINISTRADOR;
        } else {
            assignedRol = (this.correo != null && this.correo.toLowerCase().endsWith("@incidencias.com"))
                    ? Rol.EMPLEADO
                    : Rol.CLIENTE;
        }

        return Cliente.builder()
                .nombre(this.nombre)
                .apellido(this.apellido)
                .correo(this.correo)
                .contrasena(this.contrasena)
                .fechaRegistro(LocalDateTime.now())
                .activo(this.activo != null ? this.activo : true)
                .rol(assignedRol)
                .build();
    }

    public ResponseEntity<?> executeCreate(ClienteService clienteService) {
        if (clienteService.findByCorreo(this.correo).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo electrónico ya está registrado.");
        }
        Cliente savedCliente = clienteService.save(this.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.fromEntity(savedCliente));
    }
}
