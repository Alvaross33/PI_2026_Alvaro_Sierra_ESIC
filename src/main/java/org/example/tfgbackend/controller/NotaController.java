package org.example.tfgbackend.controller;

import org.example.tfgbackend.dto.NotaCreateDTO;
import org.example.tfgbackend.dto.NotaDTO;
import org.example.tfgbackend.service.IncidenciaService;
import org.example.tfgbackend.service.NotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    @Autowired
    private NotaService notaService;

    @Autowired
    private IncidenciaService incidenciaService;

    @GetMapping("/incidencia/{incidenciaId}") // Endpoint GET para listar notas de una incidencia
    public List<NotaDTO> getByIncidencia(@PathVariable Long incidenciaId) {
        return notaService.findByIncidenciaId(incidenciaId).stream()
                .map(NotaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping 
    public ResponseEntity<?> create(@RequestBody NotaCreateDTO dto) {
        return dto.executeCreate(incidenciaService, notaService);
    }
}