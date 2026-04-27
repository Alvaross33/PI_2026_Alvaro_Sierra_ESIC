package org.example.tfgbackend.service; // Capa de servicios

import org.example.tfgbackend.model.Incidencia; // Entidad Incidencia
import org.example.tfgbackend.repository.IncidenciaRepository; // Repositorio Incidencia
import org.springframework.beans.factory.annotation.Autowired; // Inyección de dependencias
import org.springframework.stereotype.Service; // Anotación de lógica de negocio

import java.util.List; // Manejo de listas
import java.util.Optional; // Manejo de valores opcionales

@Service // Clase que define las operaciones de negocio para Incidencias
public class IncidenciaService { // Implementación del servicio

    @Autowired // Spring conecta automáticamente el repositorio
    private IncidenciaRepository incidenciaRepository;

    public List<Incidencia> findAll() { // Retorna todas las incidencias registradas
        return incidenciaRepository.findAll();
    }

    public List<Incidencia> findByCliente(org.example.tfgbackend.model.Cliente cliente) { // Filtra por cliente
        return incidenciaRepository.findByCliente(cliente);
    }

    public List<Incidencia> findByEmpleado(org.example.tfgbackend.model.Empleado empleado) {
        return incidenciaRepository.findByEmpleado(empleado);
    }

    public Optional<Incidencia> findById(Long id) { // Busca una incidencia por su identificador
        return incidenciaRepository.findById(id);
    }

    public Incidencia save(Incidencia incidencia) { // Crea o actualiza una incidencia
        return incidenciaRepository.save(incidencia);
    }

    public void deleteById(Long id) { // Elimina físicamente una incidencia por su ID
        incidenciaRepository.deleteById(id);
    }
}
