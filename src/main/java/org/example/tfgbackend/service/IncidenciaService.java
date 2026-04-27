package org.example.tfgbackend.service;

import org.example.tfgbackend.model.Incidencia;
import org.example.tfgbackend.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidenciaService {

    @Autowired
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
