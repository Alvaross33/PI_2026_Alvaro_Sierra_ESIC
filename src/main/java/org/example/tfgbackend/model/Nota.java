package org.example.tfgbackend.model; // Paquete de modelos de datos

import jakarta.persistence.*; // Uso de JPA para persistir objetos
import lombok.*; // Uso de Lombok para simplificar el código
import java.time.LocalDateTime; // Uso de fechas y horas locales

@Entity // Clase mapeada a una tabla de base de datos
@Table(name = "notas") // Nombre físico de la tabla
@Getter // Autogenerar Getters
@Setter // Autogenerar Setters
@NoArgsConstructor // Constructor sin parámetros
@AllArgsConstructor // Constructor completo
@Builder // Soporte para patrón Builder
public class Nota { // Representa un comentario o nota en una incidencia
    @Id // Clave primaria única
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Valor auto-incremental
    private Long notaId; // ID único de la nota

    @ManyToOne // Varias notas pertenecen a una sola incidencia
    @JoinColumn(name = "incidencia_id", nullable = false) // Relación obligatoria
    private Incidencia incidencia; // Referencia a la incidencia padre

    private String autor; // Rol del autor de la nota (CLIENTE o EMPLEADO)
    private String nombreAutor; // Nombre completo de quien escribió la nota
    private String contenido; // El texto del comentario
    private LocalDateTime fechaCreacion; // Fecha y hora del registro de la nota
}