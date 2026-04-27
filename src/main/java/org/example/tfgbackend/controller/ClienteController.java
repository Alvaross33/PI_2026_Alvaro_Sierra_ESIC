package org.example.tfgbackend.controller; // Paquete de controladores de la API

import org.example.tfgbackend.dto.ClienteCreateDTO; // DTO para creación de clientes
import org.example.tfgbackend.dto.ClienteDTO; // DTO para salida de clientes
import org.example.tfgbackend.dto.ClienteUpdateDTO; // DTO para actualización de clientes
import org.example.tfgbackend.dto.LoginRequestDTO; // DTO para la petición de login
import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.service.ClienteService; // Servicio de Clientes
import org.example.tfgbackend.service.EmpleadoService;
import org.example.tfgbackend.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.http.ResponseEntity; // Respuesta HTTP genérica
import org.springframework.web.bind.annotation.*; // Anotaciones web de Spring

import java.util.List; // Manejo de listas
import java.util.stream.Collectors; // Procesamiento de colecciones

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/clientes") // Ruta base para todos los endpoints de clientes
public class ClienteController { // Controlador para gestionar clientes

    @Autowired // Inyecta el servicio de clientes
    private ClienteService clienteService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private IncidenciaService incidenciaService;

    @GetMapping // Endpoint GET para listar todos los clientes
    public List<ClienteDTO> getAll() {
        return clienteService.findAll().stream() // Obtiene entidades
                .map(ClienteDTO::fromEntity) // Convierte a DTO
                .collect(Collectors.toList()); // Retorna lista
    }

    @GetMapping("/{id}") // Endpoint GET para buscar un cliente por ID
    public ResponseEntity<ClienteDTO> getById(@PathVariable Long id) {
        return clienteService.findById(id) // Busca entidad
                .map(ClienteDTO::fromEntity) // Convierte a DTO si existe
                .map(ResponseEntity::ok) // Retorna 200 OK
                .orElse(ResponseEntity.notFound().build()); // Retorna 404 si no existe
    }

    @PostMapping // Endpoint POST para registrar un nuevo cliente
    public ResponseEntity<?> create(@RequestBody ClienteCreateDTO clienteDTO) {
        // Verificar si ya existe en empleados
        if (empleadoService.findByCorreoIgnoreCase(clienteDTO.getCorreo()).isPresent()) {
            return ResponseEntity.status(409).body("El correo electrónico ya está registrado como empleado.");
        }

        ResponseEntity<?> response = clienteDTO.executeCreate(clienteService);
        if (response.getStatusCode().is2xxSuccessful()) {
            ClienteDTO created = (ClienteDTO) response.getBody();
            if (created != null && (created.getRol() == Rol.EMPLEADO || created.getRol() == Rol.ADMINISTRADOR)) {
                // Si el DTO determinó que es empleado/admin (por dominio o manual), lo movemos a tabla empleados
                clienteService.findById(created.getClienteId()).ifPresent(c -> {
                    Empleado e = Empleado.builder()
                            .empleadoId(c.getClienteId())
                            .nombre(c.getNombre())
                            .apellido(c.getApellido())
                            .correo(c.getCorreo())
                            .contrasena(c.getContrasena())
                            .responsabilidad("Sincronizado desde registro")
                            .rol(c.getRol())
                            .build();
                    empleadoService.save(e);
                    
                    // IMPORTANTE: Borrar de la tabla clientes para cumplir la regla de exclusividad
                    clienteService.deleteById(c.getClienteId());
                });
                
                // Retornar el DTO con los datos, pero ya no estará en la tabla clientes
                return ResponseEntity.status(201).body(created);
            }
        }
        return response;
    }

    @PostMapping("/login") // Endpoint POST para autenticar usuarios
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        return request.executeLogin(clienteService, empleadoService);
    }

    @PutMapping("/{id}") // Endpoint PUT para actualizar datos de un cliente
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ClienteUpdateDTO clienteDetailsDTO) {
        // Primero verificar si el correo nuevo ya existe en empleados (si ha cambiado)
        if (clienteDetailsDTO.getCorreo() != null) {
            empleadoService.findByCorreoIgnoreCase(clienteDetailsDTO.getCorreo()).ifPresent(e -> {
                if (!e.getEmpleadoId().equals(id)) {
                    // Si existe un empleado con ese correo y no es el mismo ID
                    // Pero en este sistema el ID es compartido o se intenta compartir
                }
            });
        }

        ResponseEntity<?> response = clienteDetailsDTO.executeUpdate(id, clienteService);
        if (response.getStatusCode().is2xxSuccessful()) {
            clienteService.findById(id).ifPresent(c -> {
                if (c.getRol() == Rol.EMPLEADO || c.getRol() == Rol.ADMINISTRADOR) {
                    // Si el rol es empleado o admin, mover a tabla Empleados
                    Empleado e = Empleado.builder()
                            .empleadoId(c.getClienteId())
                            .nombre(c.getNombre())
                            .apellido(c.getApellido())
                            .correo(c.getCorreo())
                            .contrasena(c.getContrasena())
                            .responsabilidad("Sincronizado desde Cliente")
                            .rol(c.getRol())
                            .build();
                    empleadoService.save(e);
                    
                    // Borrar de clientes
                    clienteService.deleteById(c.getClienteId());
                }
            });
        }
        return response;
    }

    @DeleteMapping("/{id}") // Endpoint DELETE para borrar un cliente
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ClienteDTO.executeDelete(id, clienteService);
    }
}