package org.example.tfgbackend.controller; // Paquete de controladores API

import org.example.tfgbackend.dto.IncidenciaCreateDTO; // DTO para creación
import org.example.tfgbackend.dto.IncidenciaDTO; // DTO de salida
import org.example.tfgbackend.dto.IncidenciaUpdateDTO; // DTO para actualización
import org.example.tfgbackend.service.ClienteService; // Servicio para gestionar clientes
import org.example.tfgbackend.service.EmpleadoService; // Servicio para gestionar empleados
import org.example.tfgbackend.service.IncidenciaService; // Servicio para gestionar incidencias
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.http.ResponseEntity; // Manejo de respuestas HTTP
import org.springframework.web.bind.annotation.*; // Anotaciones REST de Spring

import java.util.List; // Uso de listas de Java
import java.util.stream.Collectors; // Procesamiento de flujos de datos

@RestController // Define la clase como controlador de servicios REST
@RequestMapping("/api/incidencias") // Ruta base de los recursos de incidencias
public class IncidenciaController { // Controlador principal de incidencias

    @Autowired // Spring inyecta automáticamente los servicios requeridos
    private IncidenciaService incidenciaService;

    @Autowired // Inyección del servicio de clientes
    private ClienteService clienteService;

    @Autowired // Inyección del servicio de empleados
    private EmpleadoService empleadoService;

    @GetMapping // Endpoint GET para obtener todas las incidencias (Admin/Empleado)
    public List<IncidenciaDTO> getAll() {
        return incidenciaService.findAll().stream() // Convierte lista de entidades
                .map(IncidenciaDTO::fromEntity) // Transforma cada entidad a su DTO correspondiente
                .collect(Collectors.toList()); // Retorna la lista resultante
    }

    @GetMapping("/cliente/{clienteId}") // Endpoint GET para filtrar incidencias por cliente
    public ResponseEntity<?> getByCliente(@PathVariable Long clienteId) {
        return IncidenciaDTO.executeGetByCliente(clienteId, clienteService, incidenciaService);
    }

    @GetMapping("/{id}") // Endpoint GET para consultar una incidencia específica por ID
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return IncidenciaDTO.executeGetById(id, incidenciaService);
    }

    @PostMapping // Endpoint POST para que un cliente cree una nueva incidencia
    public ResponseEntity<?> create(@RequestBody IncidenciaCreateDTO incidenciaDTO) {
        return incidenciaDTO.executeCreate(incidenciaService, clienteService);
    }

    @PutMapping("/{id}") // Endpoint PUT para actualizar o asignar una incidencia
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody IncidenciaUpdateDTO incidenciaDetailsDTO) {
        return incidenciaDetailsDTO.executeUpdate(id, incidenciaService, clienteService, empleadoService);
    }

    @DeleteMapping("/{id}") // Endpoint DELETE para borrar una incidencia
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return IncidenciaDTO.executeDelete(id, incidenciaService);
    }
}
