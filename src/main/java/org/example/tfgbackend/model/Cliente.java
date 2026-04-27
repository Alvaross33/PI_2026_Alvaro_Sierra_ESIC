package org.example.tfgbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

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