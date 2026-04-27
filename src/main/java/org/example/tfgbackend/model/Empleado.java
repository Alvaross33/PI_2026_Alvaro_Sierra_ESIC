package org.example.tfgbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {
    @Id
    private Long empleadoId;

    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String responsabilidad;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToMany(mappedBy = "empleado")
    private List<Incidencia> incidencias;
}
