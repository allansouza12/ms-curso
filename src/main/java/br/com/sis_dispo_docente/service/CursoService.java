package br.com.sis_dispo_docente.service;

import br.com.sis_dispo_docente.domain.Curso;
import br.com.sis_dispo_docente.domain.Status;
import br.com.sis_dispo_docente.dto.CursoReq;
import br.com.sis_dispo_docente.exception.NaoEncontradoException;
import br.com.sis_dispo_docente.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository repository;

    public List<Curso> listar(Long centroId) {
        return centroId == null ? repository.findAllByOrderBySiglaAsc()
                : repository.findByCentroIdOrderBySiglaAsc(centroId);
    }

    public Curso buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Curso não encontrado."));
    }

    @Transactional
    public Curso criar(CursoReq req) {
        Curso c = new Curso();
        preencher(c, req);
        return repository.save(c);
    }

    @Transactional
    public Curso atualizar(Long id, CursoReq req) {
        Curso c = buscar(id);
        preencher(c, req);
        return c;
    }

    @Transactional
    public Curso inativar(Long id) {
        Curso c = buscar(id);
        c.setStatus(Status.INATIVO);
        return c;
    }

    /**
     * Só exclui. A regra "curso com disciplinas não pode ser excluído" é do ORQUESTRADOR (API Gateway):
     * ele consulta o ms-disciplina antes de pedir este DELETE. Este serviço apenas responde ao orquestrador.
     */
    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    private void preencher(Curso c, CursoReq req) {
        c.setSigla(req.sigla().trim().toUpperCase());
        c.setDescricao(req.descricao().trim());
        c.setCentroId(req.centroId());
        c.getTurnos().clear();
        c.getTurnos().addAll(req.turnos());
        c.setCoordenador(req.coordenador().trim());
    }
}
