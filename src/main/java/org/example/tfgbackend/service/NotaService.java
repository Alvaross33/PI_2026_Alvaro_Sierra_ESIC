package org.example.tfgbackend.service;

import org.example.tfgbackend.model.Nota;
import org.example.tfgbackend.repository.NotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List; // Uso de listas

@Service
public class NotaService {

    @Autowired
    private NotaRepository notaRepository;

    public List<Nota> findByIncidenciaId(Long incidenciaId) { // Recupera notas de una incidencia específica
        return notaRepository.findByIncidenciaIncidenciaIdOrderByFechaCreacionAsc(incidenciaId);
    }

    public Nota save(Nota nota) { // Guarda una nueva nota en la base de datos
        return notaRepository.save(nota);
    }
}