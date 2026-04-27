package org.example.tfgbackend.service;

import org.example.tfgbackend.model.Cliente;
import org.example.tfgbackend.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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