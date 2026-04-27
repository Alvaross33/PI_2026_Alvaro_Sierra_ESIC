package org.example.tfgbackend.model; // Paquete de modelos de datos

import jakarta.persistence.*; // Importación de anotaciones JPA para persistencia
import lombok.*; // Importación de Lombok para reducir código repetitivo
import java.time.LocalDateTime; // Importación para manejar fechas y horas
import java.util.List; // Importación para manejar listas de objetos

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;

    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private String correo;
    
    private String contrasena;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Enumerated(EnumType.STRING)
    private Rol rol;
    
    private Boolean activo;

    @OneToMany(mappedBy = "cliente")
    private List<Incidencia> incidencias;
}