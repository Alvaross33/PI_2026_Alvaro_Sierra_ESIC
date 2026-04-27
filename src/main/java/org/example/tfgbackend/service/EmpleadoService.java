package org.example.tfgbackend.service;

import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
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
