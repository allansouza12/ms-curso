# ms-curso

Microsserviço de **Cursos com turnos** do sistema de disponibilidade docente (TED 2ª unidade: do monolítico ao microsserviço).

| | |
|---|---|
| **Nome no Eureka** (`spring.application.name`) | `ms-curso` |
| **Porta** | `8081` |
| **Banco** | MySQL, `ms_curso` (criado automaticamente) |
| **Tabelas** | `curso`, `curso_turno` (1:N) |
| **Stack** | Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3, Spring Data JPA, Eureka Client |

> Este serviço **não chama outros microsserviços**. Ele só responde às requisições do **orquestrador** (API Gateway).
> O curso guarda apenas o `centroId` (referência lógica, sem chave estrangeira): o Centro Acadêmico pertence a outro serviço.

---

## Como rodar

**Pré-requisitos:** Java 17, Maven, MySQL ligado em `localhost:3306`.

1. Clone o repositório e abra no VS Code / IntelliJ.
2. Em `src/main/resources`, copie `secrets.yml.example` para **`secrets.yml`** (este arquivo é ignorado pelo Git) e coloque a senha do seu MySQL:
   ```yaml
   spring:
     datasource:
       password: SUA_SENHA_DO_MYSQL
   ```
3. Aponte para o Eureka do grupo. Por padrão é `http://localhost:8761/eureka`. Para outro endereço, adicione no `secrets.yml`:
   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: http://IP-DO-DISCOVERY:8761/eureka
   ```
   Para testar **sem** Eureka, use:
   ```yaml
   eureka:
     client:
       enabled: false
   ```
4. Rode a classe `SisDispoDocenteApplication` (ou `mvn spring-boot:run`).
5. Confira: `http://localhost:8081/api/cursos` responde e, no painel do Eureka (`http://localhost:8761`), aparece **MS-CURSO** como UP.

Se rodar em máquina diferente da do Eureka/Gateway, libere a porta 8081 no firewall (o serviço registra o IP, `prefer-ip-address: true`).

---

## Endpoints

Base: `http://localhost:8081` (ou, via Eureka, `lb://ms-curso`)

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/api/cursos` | Lista (ordenada por sigla). Filtro opcional `?centroId=1` | 200 |
| GET | `/api/cursos/{id}` | Busca um curso | 200 / 404 |
| POST | `/api/cursos` | Cria | 200 |
| PUT | `/api/cursos/{id}` | Edita | 200 / 404 |
| PATCH | `/api/cursos/{id}/inativar` | Muda o status para `INATIVO` | 200 / 404 |
| DELETE | `/api/cursos/{id}` | Exclui (**não** verifica disciplinas, ver abaixo) | 204 / 404 |

### Corpo do POST / PUT

```json
{
  "sigla": "ADS",
  "descricao": "Analise e Desenvolvimento de Sistemas",
  "centroId": 1,
  "turnos": ["MATUTINO", "NOTURNO"],
  "coordenador": "Maria"
}
```

| Campo | Regra |
|---|---|
| `sigla` | obrigatória, até 20 caracteres (é salva em maiúsculas) |
| `descricao` | obrigatória |
| `centroId` | obrigatório (id do Centro Acadêmico, de outro serviço) |
| `turnos` | lista **não vazia**; valores: `MATUTINO`, `VESPERTINO`, `NOTURNO` |
| `coordenador` | obrigatório |

### Resposta

```json
{
  "id": 1,
  "sigla": "ADS",
  "descricao": "Analise e Desenvolvimento de Sistemas",
  "centroId": 1,
  "turnos": ["MATUTINO", "NOTURNO"],
  "coordenador": "Maria",
  "dataCadastro": "2026-11-15",
  "status": "ATIVO"
}
```

### Erros

Sempre no formato `{ "mensagem": "..." }`:

| Status | Quando |
|---|---|
| 400 | corpo inválido (campo faltando, `turnos` vazio...) |
| 404 | curso não encontrado |
| 409 | registro duplicado / vinculado a outros dados |

---

## O que o orquestrador precisa fazer

As regras que envolvem **mais de um serviço** ficam no orquestrador (API Gateway), não aqui:

1. **Excluir curso** (`DELETE /api/cursos/{id}` no gateway)
   1. `GET lb://ms-disciplina/api/disciplinas/existe?cursoId={id}`
   2. se `true` → responder **409** "O curso possui disciplinas vinculadas. Ele não pode ser excluído, apenas inativado."
   3. senão → `DELETE lb://ms-curso/api/cursos/{id}`
2. **Criar/editar disciplina**: antes de chamar o `ms-disciplina`, consultar `GET lb://ms-curso/api/cursos/{cursoId}`; se `404` → 404 "Curso não encontrado."; se `status != ATIVO` → 409 "Não é possível vincular uma disciplina a um curso inativo."
3. **Criar/editar curso**: validar no serviço de Centro Acadêmico se o `centroId` existe e está `ATIVO`, e só então chamar `POST/PUT` aqui.
4. **Listagens com sigla do curso** (ex.: disciplinas com `cursoSigla`): juntar as respostas de `ms-disciplina` e `ms-curso` no gateway.

Rota simples no gateway (o `lb://` resolve o nome pelo Eureka):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: cursos
          uri: lb://ms-curso
          predicates: [ Path=/api/cursos/** ]
```

> O Spring Cloud Gateway é reativo (WebFlux): para chamar os serviços por código, use `WebClient` com `@LoadBalanced`.

---

## Observações

- Autenticação/token **não** faz parte deste serviço (fica no gateway ou no microsserviço de segurança do grupo).
- Versões que **todos** os projetos do grupo devem usar: Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3.