package br.com.sis_dispo_docente.repository;

import br.com.sis_dispo_docente.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findAllByOrderBySiglaAsc();
    List<Curso> findByCentroIdOrderBySiglaAsc(Long centroId);
}
