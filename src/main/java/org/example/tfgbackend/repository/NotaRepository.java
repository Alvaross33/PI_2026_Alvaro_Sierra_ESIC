package org.example.tfgbackend.repository; // Paquete de acceso a datos

import org.example.tfgbackend.model.Nota; // Importación del modelo de datos Nota
import org.springframework.data.jpa.repository.JpaRepository; // Repositorio genérico de Spring Data JPA
import org.springframework.stereotype.Repository; // Etiqueta para marcar como repositorio

import java.util.List; // Manejo de colecciones tipo lista

@Repository // Componente de persistencia gestionado por Spring
public interface NotaRepository extends JpaRepository<Nota, Long> { // Herencia de JpaRepository para manejar notas
    // Busca todas las notas asociadas a una incidencia, ordenadas cronológicamente
    List<Nota> findByIncidenciaIncidenciaIdOrderByFechaCreacionAsc(Long incidenciaId);
}