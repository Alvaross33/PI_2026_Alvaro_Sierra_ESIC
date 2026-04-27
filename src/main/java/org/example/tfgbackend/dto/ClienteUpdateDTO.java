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
 * DTO (Data Transfer Object) utilizado para la actualización de un Cliente.
 * Encapsula los datos que pueden ser modificados por un usuario o administrador.
 */
public class ClienteUpdateDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private LocalDateTime fechaRegistro;
    private Rol rol;
    private Boolean activo;

    public void updateEntity(Cliente cliente) {
        cliente.setNombre(this.nombre);
        cliente.setApellido(this.apellido);
        cliente.setCorreo(this.correo);
        cliente.setContrasena(this.contrasena);
        cliente.setFechaRegistro(this.fechaRegistro);
        cliente.setActivo(this.activo);
        
        if (this.rol == Rol.ADMINISTRADOR) {
            cliente.setRol(Rol.ADMINISTRADOR);
        } else if (this.correo != null && this.correo.toLowerCase().endsWith("@incidencias.com")) {
            cliente.setRol(Rol.EMPLEADO);
        } else {
            cliente.setRol(Rol.CLIENTE);
        }
    }

    public ResponseEntity<?> executeUpdate(Long id, ClienteService clienteService) {
        return clienteService.findById(id)
                .map(cliente -> {
                    if (this.correo != null && !cliente.getCorreo().equalsIgnoreCase(this.correo)) {
                        if (clienteService.findByCorreo(this.correo).isPresent()) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo electrónico ya está registrado por otro usuario.");
                        }
                    }
                    this.updateEntity(cliente);
                    Cliente updatedCliente = clienteService.save(cliente);
                    return ResponseEntity.ok(ClienteDTO.fromEntity(updatedCliente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
