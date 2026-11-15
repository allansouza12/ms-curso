package br.com.sis_dispo_docente.dto;

import br.com.sis_dispo_docente.domain.Curso;
import br.com.sis_dispo_docente.domain.Status;
import br.com.sis_dispo_docente.domain.Turno;

import java.time.LocalDate;
import java.util.Set;

/** Resposta da API (nunca devolvemos a entidade direto). */
public record CursoDto(Long id, String sigla, String descricao, Long centroId,
                       Set<Turno> turnos, String coordenador, LocalDate dataCadastro, Status status) {

    public static CursoDto de(Curso c) {
        return new CursoDto(c.getId(), c.getSigla(), c.getDescricao(), c.getCentroId(),
                Set.copyOf(c.getTurnos()), c.getCoordenador(), c.getDataCadastro(), c.getStatus());
    }
}
