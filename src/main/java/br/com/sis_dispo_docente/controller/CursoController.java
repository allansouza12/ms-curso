package br.com.sis_dispo_docente.controller;

import br.com.sis_dispo_docente.dto.CursoDto;
import br.com.sis_dispo_docente.dto.CursoReq;
import br.com.sis_dispo_docente.service.CursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService service;

    @GetMapping
    public List<CursoDto> listar(@RequestParam(required = false) Long centroId) {
        return service.listar(centroId).stream().map(CursoDto::de).toList();
    }

    /** Consultado pelo orquestrador para validar o curso (existe? está ATIVO?). */
    @GetMapping("/{id}")
    public CursoDto buscar(@PathVariable Long id) {
        return CursoDto.de(service.buscar(id));
    }

    @PostMapping
    public CursoDto criar(@RequestBody @Valid CursoReq req) {
        return CursoDto.de(service.criar(req));
    }

    @PutMapping("/{id}")
    public CursoDto atualizar(@PathVariable Long id, @RequestBody @Valid CursoReq req) {
        return CursoDto.de(service.atualizar(id, req));
    }

    @PatchMapping("/{id}/inativar")
    public CursoDto inativar(@PathVariable Long id) {
        return CursoDto.de(service.inativar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
