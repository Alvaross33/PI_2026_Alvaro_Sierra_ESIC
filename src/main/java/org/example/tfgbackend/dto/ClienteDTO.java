package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.model.Rol;

import java.time.LocalDateTime;

import org.example.tfgbackend.service.ClienteService;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
    private Long clienteId;
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private LocalDateTime fechaRegistro;
    private Rol rol;
    private Boolean activo;

    public static ClienteDTO fromEntity(Cliente cliente) {
        if (cliente == null) return null;
        return ClienteDTO.builder()
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .correo(cliente.getCorreo())
                .contrasena(cliente.getContrasena())
                .fechaRegistro(cliente.getFechaRegistro())
                .rol(cliente.getRol())
                .activo(cliente.getActivo())
                .build();
    }

    public static ClienteDTO fromEmpleado(Empleado empleado, Rol rol) {
        if (empleado == null) return null;
        return ClienteDTO.builder()
                .clienteId(empleado.getEmpleadoId())
                .nombre(empleado.getNombre())
                .apellido(empleado.getApellido())
                .correo(empleado.getCorreo())
                .contrasena(empleado.getContrasena())
                .rol(rol)
                .activo(true)
                .build();
    }

    public Cliente toEntity() {
        return Cliente.builder()
                .clienteId(this.clienteId)
                .nombre(this.nombre)
                .apellido(this.apellido)
                .correo(this.correo)
                .contrasena(this.contrasena)
                .fechaRegistro(this.fechaRegistro)
                .rol(this.rol)
                .activo(this.activo)
                .build();
    }

    public static ResponseEntity<?> executeDelete(Long id, ClienteService clienteService) {
        return clienteService.findById(id)
                .map(cliente -> {
                    clienteService.deleteById(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
