package br.com.sis_dispo_docente.dto;

import br.com.sis_dispo_docente.domain.Turno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Corpo (JSON) recebido para criar/editar curso. */
public record CursoReq(
        @NotBlank @Size(max = 20) String sigla,
        @NotBlank String descricao,
        @NotNull Long centroId,
        @NotEmpty Set<Turno> turnos,
        @NotBlank String coordenador) {
}
