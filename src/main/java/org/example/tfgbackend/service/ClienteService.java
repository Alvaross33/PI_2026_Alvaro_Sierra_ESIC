package org.example.tfgbackend.service; // Paquete de lógica de negocio

import org.example.tfgbackend.model.Cliente; // Importación del modelo Cliente
import org.example.tfgbackend.repository.ClienteRepository; // Repositorio de Clientes
import org.springframework.beans.factory.annotation.Autowired; // Para inyección de dependencias
import org.springframework.stereotype.Service; // Define la clase como un Servicio de Spring

import java.util.List; // Uso de colecciones de Java
import java.util.Optional; // Manejo de valores que pueden ser nulos

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> findByCorreoAndContrasena(String correo, String contrasena) {
        return clienteRepository.findByCorreoIgnoreCaseAndContrasena(correo, contrasena);
    }

    public Optional<Cliente> findByCorreo(String correo) {
        return clienteRepository.findByCorreoIgnoreCase(correo);
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }
}