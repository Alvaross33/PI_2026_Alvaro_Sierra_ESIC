package org.example.tfgbackend.service; // Capa de lógica de negocio

import org.example.tfgbackend.model.Nota; // Entidad Nota
import org.example.tfgbackend.repository.NotaRepository; // Repositorio de notas
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.stereotype.Service; // Anotación de servicio de Spring

import java.util.List; // Uso de listas

@Service // Indica que esta clase es un servicio gestionado por Spring
public class NotaService { // Clase que gestiona las notas de las incidencias

    @Autowired // Inyección automática del repositorio
    private NotaRepository notaRepository;

    public List<Nota> findByIncidenciaId(Long incidenciaId) { // Recupera notas de una incidencia específica
        return notaRepository.findByIncidenciaIncidenciaIdOrderByFechaCreacionAsc(incidenciaId);
    }

    public Nota save(Nota nota) { // Guarda una nueva nota en la base de datos
        return notaRepository.save(nota);
    }
}