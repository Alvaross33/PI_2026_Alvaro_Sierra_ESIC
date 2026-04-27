package org.example.tfgbackend.controller; // Paquete de controladores

import org.example.tfgbackend.dto.NotaCreateDTO; // DTO para creación
import org.example.tfgbackend.dto.NotaDTO; // DTO de salida
import org.example.tfgbackend.service.IncidenciaService; // Servicio de incidencias
import org.example.tfgbackend.service.NotaService; // Servicio de notas
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.http.ResponseEntity; // Respuesta HTTP genérica
import org.springframework.web.bind.annotation.*; // Anotaciones REST
import java.util.List; // Manejo de listas
import java.util.stream.Collectors; // Streams para transformación

@RestController // Controlador REST
@RequestMapping("/api/notas") // Ruta base de la API de notas
public class NotaController { // Gestión de notas (comentarios asíncronos)

    @Autowired // Inyectar servicio de notas
    private NotaService notaService;

    @Autowired // Inyectar servicio de incidencias
    private IncidenciaService incidenciaService;

    @GetMapping("/incidencia/{incidenciaId}") // Endpoint GET para listar notas de una incidencia
    public List<NotaDTO> getByIncidencia(@PathVariable Long incidenciaId) {
        return notaService.findByIncidenciaId(incidenciaId).stream() // Busca notas
                .map(NotaDTO::fromEntity) // Convierte cada una a DTO
                .collect(Collectors.toList()); // Retorna la lista
    }

    @PostMapping // Endpoint POST para añadir una nueva nota
    public ResponseEntity<?> create(@RequestBody NotaCreateDTO dto) {
        return dto.executeCreate(incidenciaService, notaService);
    }
}