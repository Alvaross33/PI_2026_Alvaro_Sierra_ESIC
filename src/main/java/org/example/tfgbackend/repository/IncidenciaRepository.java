package org.example.tfgbackend.repository;

import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.model.Incidencia;
import org.example.tfgbackend.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByCliente(Cliente cliente);

    List<Incidencia> findByEmpleado(Empleado empleado);
}
