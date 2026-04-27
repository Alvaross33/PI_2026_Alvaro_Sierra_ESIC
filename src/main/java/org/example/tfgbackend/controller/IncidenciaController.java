package org.example.tfgbackend.controller;

import org.example.tfgbackend.dto.IncidenciaCreateDTO;
import org.example.tfgbackend.dto.IncidenciaDTO;
import org.example.tfgbackend.dto.IncidenciaUpdateDTO;
import org.example.tfgbackend.service.ClienteService;
import org.example.tfgbackend.service.EmpleadoService;
import org.example.tfgbackend.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    @Autowired
    private IncidenciaService incidenciaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping // Endpoint GET para obtener todas las incidencias (Admin/Empleado)
    public List<IncidenciaDTO> getAll() {
        return incidenciaService.findAll().stream()
                .map(IncidenciaDTO::fromEntity)
                .collect(Collectors.toList());
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
