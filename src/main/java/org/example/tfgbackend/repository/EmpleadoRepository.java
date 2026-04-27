package org.example.tfgbackend.repository; // Paquete de acceso a datos

import org.example.tfgbackend.model.Empleado; // Importación del modelo de datos Empleado
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz genérica de Spring para repositorios JPA
import org.springframework.stereotype.Repository; // Indica que es un componente de Spring de tipo repositorio

import java.util.Optional;

@Repository // Permite que Spring gestione esta interfaz como un bean
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> { // Proporciona métodos CRUD para la entidad Empleado
    Optional<Empleado> findByCorreoIgnoreCase(String correo);
}
