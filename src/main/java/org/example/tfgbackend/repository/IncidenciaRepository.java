package org.example.tfgbackend.repository; // Paquete para acceso a base de datos

import org.example.tfgbackend.model.Cliente; // Importación del modelo Cliente
import org.example.tfgbackend.model.Incidencia; // Importación del modelo Incidencia
import org.example.tfgbackend.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository; // Repositorio JPA de Spring Data
import org.springframework.stereotype.Repository; // Etiqueta para inyección de dependencias

import java.util.List; // Uso de listas de Java

@Repository // Define la clase como un repositorio de persistencia
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> { // Extensión de JpaRepository
    // Método personalizado para obtener incidencias filtradas por un cliente específico
    List<Incidencia> findByCliente(Cliente cliente);

    List<Incidencia> findByEmpleado(Empleado empleado);
}
