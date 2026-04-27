package org.example.tfgbackend.repository;

import org.example.tfgbackend.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    // Busca todas las notas asociadas a una incidencia, ordenadas cronológicamente
    List<Nota> findByIncidenciaIncidenciaIdOrderByFechaCreacionAsc(Long incidenciaId);
}