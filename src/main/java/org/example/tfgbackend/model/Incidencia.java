package org.example.tfgbackend.model; // Paquete de modelos de datos

import jakarta.persistence.*; // Persistencia de datos JPA
import lombok.*; // Utilidades Lombok
import java.time.LocalDateTime; // Manejo de tiempos locales

@Entity
@Table(name = "incidencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidenciaId;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
