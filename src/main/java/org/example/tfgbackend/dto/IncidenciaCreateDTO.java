package org.example.tfgbackend.dto;

import lombok.*;
import org.example.tfgbackend.model.Incidencia;
import org.example.tfgbackend.service.ClienteService;
import java.time.LocalDateTime;

import org.example.tfgbackend.service.IncidenciaService;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaCreateDTO {
    private Long clienteId;
    private String titulo;
    private String descripcion;
    private String prioridad;

    public Incidencia toEntity(ClienteService clienteService) {
        Incidencia incidencia = Incidencia.builder()
                .titulo(this.titulo)
                .descripcion(this.descripcion)
                .prioridad(this.prioridad)
                .estado("ABIERTA")
                .fechaCreacion(LocalDateTime.now())
                .build();

        if (this.clienteId != null) {
            clienteService.findById(this.clienteId).ifPresent(incidencia::setCliente);
        }
        return incidencia;
    }

    public ResponseEntity<?> executeCreate(IncidenciaService incidenciaService, ClienteService clienteService) {
        Incidencia saved = incidenciaService.save(this.toEntity(clienteService));
        return ResponseEntity.ok(IncidenciaDTO.fromEntity(saved));
    }
}
