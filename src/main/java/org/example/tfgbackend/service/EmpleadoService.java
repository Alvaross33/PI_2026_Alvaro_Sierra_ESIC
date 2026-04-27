package org.example.tfgbackend.service; // Paquete de lógica de negocio

import org.example.tfgbackend.model.Empleado; // Importación de la entidad Empleado
import org.example.tfgbackend.repository.EmpleadoRepository; // Repositorio de Empleados
import org.springframework.beans.factory.annotation.Autowired; // Inyección automática
import org.springframework.stereotype.Service; // Anotación de servicio de Spring

import java.util.List; // Importación de colecciones
import java.util.Optional; // Manejo seguro de nulos

@Service // Clase que gestiona la lógica de los empleados
public class EmpleadoService { // Implementación del servicio de Empleados

    @Autowired // Spring inyecta la instancia del repositorio
    private EmpleadoRepository empleadoRepository;

    public List<Empleado> findAll() { // Devuelve todos los empleados de la BD
        return empleadoRepository.findAll();
    }

    public Optional<Empleado> findById(Long id) { // Busca un empleado concreto por ID
        return empleadoRepository.findById(id);
    }

    public Empleado save(Empleado empleado) { // Almacena un empleado en la BD
        return empleadoRepository.save(empleado);
    }

    public Optional<Empleado> findByCorreoIgnoreCase(String correo) {
        return empleadoRepository.findByCorreoIgnoreCase(correo);
    }

    public void deleteById(Long id) { // Borra el registro de un empleado por ID
        empleadoRepository.deleteById(id);
    }
}
