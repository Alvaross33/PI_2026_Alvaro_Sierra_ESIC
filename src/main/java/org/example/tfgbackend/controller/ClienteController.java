package org.example.tfgbackend.controller;

import org.example.tfgbackend.dto.ClienteCreateDTO;
import org.example.tfgbackend.dto.ClienteDTO;
import org.example.tfgbackend.dto.ClienteUpdateDTO;
import org.example.tfgbackend.dto.LoginRequestDTO;
import org.example.tfgbackend.model.Empleado;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private IncidenciaService incidenciaService;

    @GetMapping // Endpoint GET para listar todos los clientes
    public List<ClienteDTO> getAll() {
        return clienteService.findAll().stream()
                .map(ClienteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}") // Endpoint GET para buscar un cliente por ID
    public ResponseEntity<ClienteDTO> getById(@PathVariable Long id) {
        return clienteService.findById(id)
                .map(ClienteDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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