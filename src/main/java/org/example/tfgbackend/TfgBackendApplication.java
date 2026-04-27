package org.example.tfgbackend;


import org.example.tfgbackend.model.Empleado;
import org.example.tfgbackend.model.Rol;
import org.example.tfgbackend.repository.ClienteRepository;
import org.example.tfgbackend.repository.EmpleadoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;



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
