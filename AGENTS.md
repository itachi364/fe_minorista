# AGENTS.md - SDD Core Rules

Reglas globales para cualquier agente de desarrollo. Objetivo: Spec Driven Development con bajo consumo de tokens, uso obligatorio de Context7 para documentación técnica vigente y cero cambios sin aprobación.

## 1. Prioridad

1. Instrucción explícita del usuario en la conversación actual.
2. Seguridad, privacidad y políticas del entorno.
3. `AGENTS.md` local del proyecto.
4. Este `AGENTS.md` global.
5. `/specs` local del proyecto.
6. Plantillas/reglas globales.
7. Convenciones existentes del repositorio.
8. Buenas prácticas del stack verificadas.

## 2. Reglas innegociables

- **MUT-001 No mutar sin confirmación.** Antes de crear, modificar, borrar, mover, formatear, instalar, commitear, pushear, desplegar o cambiar sistemas externos, listar cambios propuestos y preguntar: `Do you confirm that I should proceed?`.
- **SPEC-001 Specs first.** Antes de implementar cualquier feature, fix, refactor, config o infraestructura, leer `AGENTS.md` local y `/specs`. Si `/specs` no existe, proponer crearla y detenerse.
- **CTX7-001 Investigación obligatoria.** Antes de diseñar o implementar, usar Context7 MCP para consultar documentación vigente de frameworks, librerías, APIs, patrones o herramientas relevantes. No inventar detalles técnicos.
- **AC-001 Criterios de aceptación.** No implementar sin criterios explícitos, verificables y trazables.
- **REQ-001 No inventar requisitos.** Ambigüedades se reportan como preguntas, riesgos o supuestos pendientes de aprobación.
- **SPEC-002 Todo cambio de comportamiento actualiza specs.** La especificación es la fuente de verdad.

## 3. Flujo SDD obligatorio

### Fase 1 - Discovery

1. Leer `AGENTS.md` local si existe.
2. Leer `/specs` si existe.
3. Identificar stack, arquitectura, reglas, dependencias, pruebas, seguridad, persistencia, infraestructura y documentación.
4. Cargar solo reglas condicionales aplicables.
5. Ejecutar investigación Context7 para tecnologías relevantes.
6. Reportar riesgos, supuestos y preguntas bloqueantes.

### Fase 2 - Specification

Asegurar o proponer:

```text
/specs/requirements.md
/specs/design.md
/specs/tasks.md
README.md
```

Para cambios medianos/grandes, proponer si aplica:

```text
/specs/acceptance-criteria.md
/specs/api-contract.md
/specs/database-design.md
/specs/infrastructure.md
/specs/adr/
/specs/diagrams/
```

Contenido mínimo:

- `requirements.md`: contexto, problema, objetivo, alcance, requisitos, reglas, restricciones, dependencias, criterios de aceptación.
- `design.md`: arquitectura, componentes, flujos, contratos, modelo, persistencia, errores, seguridad, observabilidad, pruebas y sección `Context7 evidence`.
- `tasks.md`: tareas con ID, archivos, dependencias, criterios, pruebas y estado.

### Fase 3 - Approval

Antes de implementar, presentar: criterios, arquitectura, evidencia Context7, archivos a cambiar, tareas, pruebas, cobertura, Sonar/quality, riesgos y confirmación MUT-001.

### Fase 4 - Implementation

Solo después de confirmación:

- Implementar únicamente lo aprobado.
- Mantener trazabilidad tarea -> criterio -> prueba.
- Crear/actualizar pruebas para todo comportamiento nuevo o modificado.
- Apuntar a 100% branch coverage en lógica de negocio nueva/modificada.
- Aplicar SOLID, Clean Code, arquitectura definida, seguridad y convenciones del repo.
- No agregar dependencias sin justificación y aprobación.

### Fase 5 - Validation and Report

Ejecutar validaciones aprobadas. Si no se puede, reportar motivo y comando exacto. Actualizar README antes de commit/push. Proponer commit Gitmoji y pedir confirmación separada para commit/push.

## 4. Context7 Research Gate

Antes de diseñar o implementar, el agente debe:

1. Identificar tecnologías/librerías relevantes del cambio.
2. Consultar Context7 MCP usando, cuando esté disponible:
   - `resolve-library-id` para obtener el ID correcto.
   - `get-library-docs` para documentación específica por tema.
3. Priorizar documentación oficial y actual de:
   - frameworks y runtimes;
   - librerías/APIs;
   - patrones recomendados;
   - seguridad, testing, performance, observabilidad y despliegue cuando aplique.
4. Resumir solo decisiones útiles. No pegar documentación extensa.
5. Registrar en `design.md` o ADR:

```text
## Context7 evidence
- Library/tool:
- Topic consulted:
- Relevant finding:
- Decision impact:
```

Si Context7 no tiene información suficiente, decirlo explícitamente y no inventar. Usar conocimiento general solo como propuesta, no como hecho verificado.

Token budget Context7:

- Máximo 3 a 5 librerías principales por iteración.
- Máximo 1 a 2 temas por librería.
- Consultas adicionales solo si desbloquean una decisión.

## 5. Carga condicional de reglas

Cargar archivos de `C:\Users\NarutoRgal\Documents\spec-driven-workspace\agent-rules\` solo si aplican:

| Trigger | Archivo |
|---|---|
| Context7, librerías, documentación técnica, anti-alucinación | `context7.md` |
| Git/GitHub/commit/push/PR | `github.md` |
| Docker/Compose/Kubernetes/ECS/EKS | `docker.md` |
| Terraform/IaC/cloud provisioning | `terraform.md` |
| AWS Lambda/API Gateway/DynamoDB/serverless | `aws-serverless.md` |
| Base de datos/persistencia/migraciones | `database.md` |
| Pruebas/cobertura | `testing.md` |
| SonarQube/SonarCloud/quality gate | `sonarqube.md` |
| Seguridad/secrets/auth/validación | `security.md` |
| Logs/métricas/trazas/healthchecks | `observability.md` |
| README/documentación | `readme.md` |

## 6. Calidad de código

- Controladores sin lógica de negocio.
- Dominio/aplicación sin dependencias directas a DB, HTTP, filesystem, cloud SDKs o frameworks, salvo arquitectura aprobada.
- Dependencia hacia abstracciones cuando aporte testabilidad.
- Funciones pequeñas, nombres claros, errores explícitos.
- Evitar duplicación de reglas, validaciones, queries y mappings.
- No exponer secretos ni detalles internos en errores públicos.

## 7. Testing y calidad

- Pruebas obligatorias para comportamiento generado/modificado.
- Cada criterio de aceptación debe tener prueba o validación cuando sea posible.
- 100% branch coverage objetivo en lógica de negocio nueva/modificada.
- Mockear sistemas externos; no mockear el comportamiento bajo prueba.
- Cargar `testing.md` y `sonarqube.md` cuando aplique.

## 8. Seguridad y observabilidad

- Nunca hardcodear secretos, tokens, llaves privadas, credenciales o `.env` reales.
- Validar entradas externas.
- Logs sin información sensible.
- Proponer healthchecks, métricas, correlación y trazas cuando el sistema lo amerite.

## 9. Git

- No commit/push sin confirmación explícita separada.
- Antes de commit: `git status`, `git diff --stat`, revisión de secretos y resumen de pruebas.
- Mensaje propuesto con Gitmoji: `<gitmoji> <type>(<scope>): <description>`.
