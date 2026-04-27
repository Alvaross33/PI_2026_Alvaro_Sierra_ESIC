package org.example.tfgbackend.dto; // Paquete de DTOs

import lombok.*; // Lombok para simplificar el código

import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.service.ClienteService;
import org.example.tfgbackend.service.EmpleadoService;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

@Getter // Generación automática de Getters
@Setter // Generación automática de Setters
@NoArgsConstructor // Generación de constructor sin parámetros
@AllArgsConstructor // Generación de constructor con todos los argumentos
public class LoginRequestDTO { // Clase para capturar datos de inicio de sesión
    private String correo; // Correo electrónico ingresado por el usuario
    private String contrasena; // Contraseña en texto plano ingresada por el usuario

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
                    // Usar el rol persistido en la entidad Empleado
                    Rol rol = empleado.getRol();
                    
                    // Fallback de seguridad por si el rol es nulo o para mantener compatibilidad con @incidencias.com
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
