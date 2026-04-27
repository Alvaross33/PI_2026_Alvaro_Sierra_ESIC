package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Nota;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaDTO {
    private Long notaId;
    private Long incidenciaId;
    private String autor;
    private String nombreAutor;
    private String contenido;
    private LocalDateTime fechaCreacion;

    public static NotaDTO fromEntity(Nota nota) {
        if (nota == null) return null;
        return NotaDTO.builder()
                .notaId(nota.getNotaId())
                .incidenciaId(nota.getIncidencia().getIncidenciaId())
                .autor(nota.getAutor())
                .nombreAutor(nota.getNombreAutor())
                .contenido(nota.getContenido())
                .fechaCreacion(nota.getFechaCreacion())
                .build();
    }

    public Nota toEntity() {
        return Nota.builder()
                .notaId(this.notaId)
                .autor(this.autor)
                .nombreAutor(this.nombreAutor)
                .contenido(this.contenido)
                .fechaCreacion(this.fechaCreacion)
                .build();
    }
}