package org.example.tfgbackend.controller;

import org.example.tfgbackend.dto.EmpleadoCreateDTO;
import org.example.tfgbackend.dto.EmpleadoDTO;
import org.example.tfgbackend.dto.EmpleadoUpdateDTO;
import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.service.ClienteService;
import org.example.tfgbackend.service.EmpleadoService;
import org.example.tfgbackend.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    @Autowired // Inyectar servicio
    private EmpleadoService empleadoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private IncidenciaService incidenciaService;

    @GetMapping // Obtener todos
    public List<EmpleadoDTO> getAll() {
        return empleadoService.findAll().stream()
                .map(EmpleadoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}") // Obtener por ID
    public ResponseEntity<EmpleadoDTO> getById(@PathVariable Long id) {
        return empleadoService.findById(id)
                .map(EmpleadoDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping // Crear empleado
    public ResponseEntity<?> create(@RequestBody EmpleadoCreateDTO empleadoDTO) {
        if (empleadoDTO.getCorreo() == null) {
            return ResponseEntity.badRequest().body("El correo es obligatorio.");
        }
        
        // Verificar si el correo ya existe en clientes
        if (clienteService.findByCorreo(empleadoDTO.getCorreo()).isPresent()) {
            return ResponseEntity.status(409).body("El correo ya existe como cliente. Actualícelo desde el panel de clientes.");
        }

        // Permitir creación si es dominio corporativo O si es un administrador
        boolean isDomainValid = empleadoDTO.getCorreo().toLowerCase().endsWith("@incidencias.com");
        boolean isAdmin = "admin@example.com".equalsIgnoreCase(empleadoDTO.getCorreo());
        
        if (!isDomainValid && !isAdmin) {
             return ResponseEntity.badRequest().body("Solo se pueden crear empleados con el dominio @incidencias.com");
        }

        return empleadoDTO.executeCreate(empleadoService);
    }

    @PutMapping("/{id}") // Actualizar empleado
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EmpleadoUpdateDTO empleadoDetailsDTO) {
        if (empleadoDetailsDTO.getCorreo() != null) {
            boolean isDomainValid = empleadoDetailsDTO.getCorreo().toLowerCase().endsWith("@incidencias.com");
            boolean isAdmin = "admin@example.com".equalsIgnoreCase(empleadoDetailsDTO.getCorreo());
            
            if (!isDomainValid && !isAdmin) {
                return ResponseEntity.badRequest().body("Un empleado no puede tener un correo que no sea @incidencias.com");
            }
        }
        
        ResponseEntity<?> response = empleadoDetailsDTO.executeUpdate(id, empleadoService);
        if (response.getStatusCode().is2xxSuccessful()) {
            empleadoService.findById(id).ifPresent(e -> {
                // Si el correo ya no es corporativo y no es admin, mover a Clientes
                boolean isDomainValid = e.getCorreo().toLowerCase().endsWith("@incidencias.com");
                boolean isAdmin = "admin@example.com".equalsIgnoreCase(e.getCorreo());
                
                if (!isDomainValid && !isAdmin) {
                    // Mover a tabla Clientes
                    Cliente c = Cliente.builder()
                            .nombre(e.getNombre())
                            .apellido(e.getApellido())
                            .correo(e.getCorreo())
                            .contrasena(e.getContrasena())
                            .rol(Rol.CLIENTE)
                            .fechaRegistro(java.time.LocalDateTime.now())
                            .activo(true)
                            .build();
                    clienteService.save(c);
                    
                    // Desvincular incidencias
                    incidenciaService.findByEmpleado(e).forEach(inc -> {
                        inc.setEmpleado(null);
                        incidenciaService.save(inc);
                    });
                    
                    // Borrar de Empleados
                    empleadoService.deleteById(e.getEmpleadoId());
                }
            });
        }
        return response;
    }

    @DeleteMapping("/{id}") // Borrar empleado
    public ResponseEntity<?> delete(@PathVariable Long id) {
        empleadoService.findById(id).ifPresent(e -> {
            // Desvincular incidencias
            incidenciaService.findByEmpleado(e).forEach(inc -> {
                inc.setEmpleado(null);
                incidenciaService.save(inc);
            });
            // Mover a la tabla clientes como rol CLIENTE
            Cliente c = Cliente.builder()
                    .nombre(e.getNombre())
                    .apellido(e.getApellido())
                    .correo(e.getCorreo())
                    .contrasena(e.getContrasena())
                    .rol(Rol.CLIENTE)
                    .fechaRegistro(java.time.LocalDateTime.now())
                    .activo(true)
                    .build();
            clienteService.save(c);
        });
        return EmpleadoDTO.executeDelete(id, empleadoService);
    }
}
