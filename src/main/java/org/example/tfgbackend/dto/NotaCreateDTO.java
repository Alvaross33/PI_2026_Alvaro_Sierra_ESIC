package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Nota;
import org.example.tfgbackend.service.IncidenciaService;
import java.time.LocalDateTime;

import org.example.tfgbackend.service.NotaService;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCreateDTO {
    private Long incidenciaId;
    private String autor;
    private String nombreAutor;
    private String contenido;

    public Nota toEntity(IncidenciaService incidenciaService) {
        Nota nota = Nota.builder()
                .autor(this.autor)
                .nombreAutor(this.nombreAutor)
                .contenido(this.contenido)
                .fechaCreacion(LocalDateTime.now())
                .build();
        
        if (this.incidenciaId != null) {
            incidenciaService.findById(this.incidenciaId).ifPresent(nota::setIncidencia);
        }
        
        return nota;
    }

    public ResponseEntity<?> executeCreate(IncidenciaService incidenciaService, NotaService notaService) {
        Nota nota = this.toEntity(incidenciaService);
        if (nota.getIncidencia() == null) {
            return ResponseEntity.notFound().build();
        }
        Nota saved = notaService.save(nota);
        return ResponseEntity.ok(NotaDTO.fromEntity(saved));
    }
}
