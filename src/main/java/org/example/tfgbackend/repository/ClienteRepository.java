package org.example.tfgbackend.repository;

import org.example.tfgbackend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCorreoIgnoreCaseAndContrasena(String correo, String contrasena);
    Optional<Cliente> findByCorreoIgnoreCase(String correo);
}