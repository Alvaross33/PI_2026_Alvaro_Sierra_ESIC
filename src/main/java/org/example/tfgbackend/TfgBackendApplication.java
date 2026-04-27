package org.example.tfgbackend; // Paquete raíz de la aplicación

import org.example.tfgbackend.model.Cliente; // Importación de la entidad Cliente
import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.model.Rol; // Importación de los roles
import org.example.tfgbackend.repository.ClienteRepository; // Repositorio para inicializar datos
import org.example.tfgbackend.repository.EmpleadoRepository;
import org.springframework.boot.CommandLineRunner; // Interfaz para ejecutar código al inicio
import org.springframework.boot.SpringApplication; // Clase principal para lanzar Spring
import org.springframework.boot.autoconfigure.SpringBootApplication; // Anotación de aplicación Spring Boot
import org.springframework.context.annotation.Bean; // Indica que un método devuelve un bean de Spring

import java.time.LocalDateTime; // Manejo de fechas

@SpringBootApplication
public class TfgBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TfgBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository) {
        return args -> {
            if (clienteRepository.findByCorreoIgnoreCase("admin@example.com").isEmpty() && 
                empleadoRepository.findByCorreoIgnoreCase("admin@example.com").isEmpty()) {
                
                Empleado empAdmin = Empleado.builder()
                        .empleadoId(1L) // Id inicial
                        .nombre("Admin")
                        .apellido("Sistema")
                        .correo("admin@example.com")
                        .contrasena("admin123")
                        .responsabilidad("Administrador del Sistema")
                        .rol(Rol.ADMINISTRADOR)
                        .build();
                empleadoRepository.save(empAdmin);
                
                System.out.println("Cuenta de administrador creada solo en Empleado: admin@example.com / admin123");
            }
        };
    }
}
