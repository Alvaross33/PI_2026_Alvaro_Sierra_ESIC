package org.example.tfgbackend.dto; // Paquete de DTOs

import lombok.*; // Lombok para simplificar el código

import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.service.ClienteService;
import org.example.tfgbackend.service.EmpleadoService;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String correo;
    private String contrasena;

    public ResponseEntity<?> executeLogin(ClienteService clienteService, EmpleadoService empleadoService) {
        // Primero buscar en clientes
        Optional<Cliente> clienteOpt = clienteService.findByCorreoAndContrasena(this.correo, this.contrasena);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            if (Boolean.FALSE.equals(cliente.getActivo())) {
                return ResponseEntity.status(403).body("Usuario desactivado. Contacte con el administrador.");
            }
            return ResponseEntity.ok(ClienteDTO.fromEntity(cliente));
        }

        // Si no está en clientes, buscar en empleados
        return empleadoService.findByCorreoIgnoreCase(this.correo)
                .filter(e -> e.getContrasena().equals(this.contrasena))
                .map(empleado -> {

                    Rol rol = empleado.getRol();
                    

                    if (rol == null) {
                        rol = empleado.getCorreo().toLowerCase().endsWith("@incidencias.com") ? Rol.EMPLEADO : Rol.ADMINISTRADOR;
                        // Especial para admin por defecto
                        if ("admin@example.com".equalsIgnoreCase(empleado.getCorreo())) {
                            rol = Rol.ADMINISTRADOR;
                        }
                    }
                    
                    return ResponseEntity.ok(ClienteDTO.fromEmpleado(empleado, rol));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
