# TASKS — Plan de tareas atómicas

> **Fuente:** `SPECS.md` — leído el 2026-11-13, 10 requisitos EARS (REQ-001 … REQ-010) + RF-12
> **Autor:** tasks-development
> **Estado general:** 6 pendientes · 0 completadas · 0 obsoletas · 0 bloqueadas
> **Última actualización:** 2026-11-13

## 1. Resumen

Implementar la regla de consistencia de rama (RF-12) sobre `AptoDivisionEspecial`: excepción de negocio (`ReglaRamaException`), altas y actualizaciones transaccionales con validación en `CrudService`, cableado de `btnCrear` / `btnActualizar` en `MainView` y verificación manual del guion de aceptación (`SPECS.md` §9).

## 2. Convenciones

- IDs de tarea: `TASK-NNN` (TASK-001 … TASK-006 en esta versión; correlativos, nunca reutilizados).
- Estados: `[ ]` pendiente · `[x]` completada · `[!]` obsoleta · `[~]` bloqueada.
- Tamaños: XS (1 archivo) · S (1–2) · M (3–5). Sin tareas L/XL en esta versión.
- Cada tarea es autónoma: un agente puede tomarla sin leer las demás.
- Criterios de aceptación: condiciones verificables (pass/fail). Verificación: comando de build Maven cacheado o comprobación manual.

## 3. Dependencias y orden

```text
TASK-001 → TASK-002
TASK-001 → TASK-003
TASK-002 → TASK-004
TASK-003 → TASK-005
TASK-005 → TASK-006
```

- `TASK-001` es la base: define la excepción compartida por alta y actualización.
- `TASK-002` y `TASK-003` se construyen sobre `TASK-001`; son independientes entre sí.
- `TASK-004` depende de `TASK-002`; `TASK-005` depende de `TASK-003`.
- `TASK-006` depende de `TASK-005`.

## 4. Fases y tareas

### Fase 1 — Excepción y validación transaccional en el servicio

#### TASK-001 — Añadir `ReglaRamaException` en el servicio

- **Estado:** [ ]
- **Prioridad:** P0
- **Trazabilidad:** RN-06, REQ-004, RF-12
- **Descripción:** Añadir la excepción no comprobada `ReglaRamaException extends RuntimeException` en el paquete `com.example.service` que transporta el mensaje de rechazo por inconsistencia de rama o por entidad inexistente.
- **Contexto de entrada:** `SPECS.md` §2.3 (contrato técnico, puntos 3 y 5), §4 (RN-06, RN-10).
- **Criterios de aceptación:**
  - [ ] Existe la clase `ReglaRamaException` en `com.example.service` como `RuntimeException` sin lógica de negocio propia.
  - [ ] El mensaje de rechazo se delega al emisor (servicio) y no se captura dentro del método transaccional.
- **Verificación:**
  - [ ] Compila sin errores (`mvn compile` con Maven cacheado).
- **Depende de:** ninguna
- **Archivos a tocar:**
  - `src/main/java/com/example/service/ReglaRamaException.java` (nuevo)
- **Tamaño:** XS

#### TASK-002 — Añadir `CrudService.crearApto` (validar + persist) transaccional

- **Estado:** [ ]
- **Prioridad:** P0
- **Trazabilidad:** REQ-001, REQ-003, REQ-005, REQ-006, RN-01, RN-02, RN-05, RN-07, RF-12
- **Descripción:** Añadir el alta transaccional de `AptoDivisionEspecial` (`CrudService.crearApto`) que recarga recluta y división especial por ID, valida la rama y persiste; lanza `ReglaRamaException` antes de persistir si no coinciden o si falta alguna entidad.
- **Contexto de entrada:** `SPECS.md` §2.2, §2.3 (contrato técnico completo), §4.1 (REQ-001, REQ-003, REQ-006, REQ-009, REQ-010), §5 (RN-01…RN-10), §6.
- **Criterios de aceptación:**
  - [ ] `CrudService.crearApto(Integer idRecluta, Integer idDivisionEspecial, Boolean aptoServicio, LocalDate fechaEvaluacion, Integer puntaje)` es `@Transactional` (REQUIRED).
  - [ ] Recarga `Recluta` y `DivisionEspecial` con `EntityManager.find` dentro de la transacción.
  - [ ] Si la rama no coincide (`Objects.equals(ramaRecluta.getIdRama(), ramaDivision.getIdRama())`), lanza `ReglaRamaException` con el texto exacto de RN-06 antes de persistir; si falta alguna entidad responde «No encontrado» (RN-10).
- **Verificación:**
  - [ ] Compila sin errores (`mvn compile` con Maven cacheado).
  - [ ] Revisión de diff: los genéricos `guardar`/`actualizar` del servicio no se modifican.
- **Depende de:** TASK-001
- **Archivos a tocar:**
  - `src/main/java/com/example/service/CrudService.java`
  - `src/main/java/com/example/service/ReglaRamaException.java`
- **Tamaño:** S

#### TASK-003 — Añadir `CrudService.actualizarApto` (validar + merge) transaccional

- **Estado:** [ ]
- **Prioridad:** P0
- **Trazabilidad:** REQ-002, REQ-003, REQ-005, REQ-006, RN-01, RN-02, RN-05, RN-07, RF-12
- **Descripción:** Añadir el update transaccional de `AptoDivisionEspecial` (`CrudService.actualizarApto`) que recarga el registro y sus relaciones, revalida el par recluta/división especial y hace merge; lanza `ReglaRamaException` antes de merge si no coinciden o si falta alguna entidad.
- **Contexto de entrada:** `SPECS.md` §2.3 (punto 5), §4.1 (REQ-002, REQ-003, REQ-006, REQ-009, REQ-010), §6.
- **Criterios de aceptación:**
  - [ ] `CrudService.actualizarApto(Integer idApto, Integer idRecluta, Integer idDivisionEspecial, Boolean aptoServicio, LocalDate fechaEvaluacion, Integer puntaje)` es `@Transactional` (REQUIRED).
  - [ ] Recarga `AptoDivisionEspecial`, `Recluta` y `DivisionEspecial` con `EntityManager.find` dentro de la transacción y valida la rama antes de `merge`.
  - [ ] Si la rama no coincide o falta alguna entidad, lanza `ReglaRamaException` antes de `merge`; no captura la excepción dentro del método transaccional.
- **Verificación:**
  - [ ] Compila sin errores (`mvn compile` con Maven cacheado).
  - [ ] Revisión de diff: los genéricos `guardar`/`actualizar` del servicio no se modifican.
- **Depende de:** TASK-001
- **Archivos a tocar:**
  - `src/main/java/com/example/service/CrudService.java`
  - `src/main/java/com/example/service/ReglaRamaException.java`
- **Tamaño:** M

### Checkpoint — tras TASK-001…TASK-003 (capa de servicio)

- [ ] `mvn compile` sin errores
- [ ] `crearApto` / `actualizarApto` transaccionales con validación correcta
- [ ] Genéricos `guardar` / `actualizar` intactos

### Fase 2 — Cableado de la vista «Aptos División Especial»

#### TASK-004 — Cablear `btnCrear` de MainView (pestaña Aptos) para llamar a `crearApto` y mostrar error si falla

- **Estado:** [ ]
- **Prioridad:** P0
- **Trazabilidad:** REQ-004, RN-06, RF-11
- **Descripción:** Reemplazar el handler `btnCrear` de la pestaña «Aptos División Especial» por la vía `CrudService.crearApto` con captura de `ReglaRamaException`.
- **Contexto de entrada:** `SPECS.md` §2.3 (punto 5), §4.1 (REQ-004, REQ-008), §6.
- **Criterios de aceptación:**
  - [ ] El handler conserva las validaciones de campos obligatorios existentes antes de llamar al servicio.
  - [ ] Al crear llama a `crudService.crearApto(...)` con los IDs de los combos; ante `ReglaRamaException` muestra `Notification.show(e.getMessage())` y retorna sin refrescar grilla/ID ni mostrar éxito; en persistencia correcta refresca y confirma.
- **Verificación:**
  - [ ] Compila sin errores (`mvn compile` con Maven cacheado).
  - [ ] Revisión manual del handler `btnCrear` de `crearSeccionAptosDivisionEspecial`.
- **Depende de:** TASK-002
- **Archivos a tocar:**
  - `src/main/java/com/example/MainView.java`
- **Tamaño:** M

#### TASK-005 — Cablear `btnActualizar` de MainView para llamar a `actualizarApto` y mostrar error si falla

- **Estado:** [ ]
- **Prioridad:** P0
- **Trazabilidad:** REQ-004, RN-06, RF-11
- **Descripción:** Reemplazar el handler `btnActualizar` de la pestaña «Aptos División Especial» por la vía `CrudService.actualizarApto` con captura de `ReglaRamaException`.
- **Contexto de entrada:** `SPECS.md` §2.3 (punto 5), §4.1 (REQ-002, REQ-004, REQ-008), §6.
- **Criterios de aceptación:**
  - [ ] Al actualizar llama a `crudService.actualizarApto(idApto, ...)` con los IDs de los combos; ante `ReglaRamaException` muestra `Notification.show(e.getMessage())` y retorna sin refrescar ni mostrar éxito.
  - [ ] En persistencia correcta actualiza el registro y muestra notificación de éxito.
  - [ ] Las validaciones de campos obligatorios existen antes de llamar al servicio.
- **Verificación:**
  - [ ] Compila sin errores (`mvn compile` con Maven cacheado).
  - [ ] Revisión manual del handler `btnActualizar`.
- **Depende de:** TASK-003
- **Archivos a tocar:**
  - `src/main/java/com/example/MainView.java`
- **Tamaño:** M

### Checkpoint — tras TASK-004…TASK-005 (flujo completo cableado)

- [ ] `mvn compile` sin errores
- [ ] Alta coherente persiste; alta incoherente rechaza con notificación exacta
- [ ] Actualización coherente persiste; incoherente rechaza sin cambios

### Fase 3 — Verificación manual del guion de aceptación

#### TASK-006 — Verificación manual del guion de aceptación

- **Estado:** [ ]
- **Prioridad:** P0
- **Trazabilidad:** RF-12, REQ-001 … REQ-010, RN-01 … RN-10, `SPECS.md` §9
- **Descripción:** Ejecutar el guion de aceptación manual (alta misma rama, alta distinta rama, actualización coherente/incoherente, campos obligatorios, no migración de registros) contra la aplicación en ejecución.
- **Contexto de entrada:** `SPECS.md` §9, `AGENTS.md` (comandos de build y arranque).
- **Criterios de aceptación:**
  - [ ] Alta misma rama → persiste y aparece en grilla; alta distinta rama → no crea fila y muestra el mensaje exacto de RN-06.
  - [ ] Actualización coherente → persiste; actualización que introduce incoherencia → fila conserva valores y muestra RN-06.
  - [ ] Faltan recluta o división especial → notificación de campo obligatorio (RN-04) precede al mensaje de rama.
- **Verificación:**
  - [ ] Ejecución manual con `mvn spring-boot:run` contra Neon y checklist de la tarea ejecutada.
- **Depende de:** TASK-005
- **Archivos a tocar:**
  - `SPECS.md` (§9) — guion de verificación.
- **Tamaño:** S

## 5. Matriz de trazabilidad

| Requisito / RF / RN | Tareas que lo cubren |
|---------------------|----------------------|
| RF-12 | TASK-002, TASK-003, TASK-004, TASK-005, TASK-006 |
| REQ-001 | TASK-002, TASK-004, TASK-006 |
| REQ-002 | TASK-003, TASK-005, TASK-006 |
| REQ-003 | TASK-002, TASK-003, TASK-006 |
| REQ-004 | TASK-004, TASK-005, TASK-006 |
| REQ-005 | TASK-002, TASK-003, TASK-006 |
| REQ-006 | TASK-002, TASK-003 |
| REQ-007 | TASK-006 |
| REQ-008 | TASK-004, TASK-005, TASK-006 |
| REQ-009 | TASK-002, TASK-003, TASK-006 |
| REQ-010 | TASK-002, TASK-003, TASK-006 |
| RN-01 | TASK-002, TASK-003, TASK-006 |
| RN-02 | TASK-002, TASK-003, TASK-005, TASK-006 |
| RN-03 | TASK-006 |
| RN-04 | TASK-004, TASK-005, TASK-006 |
| RN-05 | TASK-002, TASK-003, TASK-006 |
| RN-06 | TASK-001, TASK-002, TASK-003, TASK-004, TASK-005, TASK-006 |
| RN-07 | TASK-002, TASK-003 |
| RN-08 | TASK-002, TASK-003, TASK-004, TASK-005 |
| RN-09 | TASK-002, TASK-003, TASK-006 |
| RN-10 | TASK-002, TASK-003, TASK-006 |

## 6. Cambios detectados en la última comparación

| Fecha | Requisito | Cambio | Tareas afectadas |
|-------|-----------|--------|------------------|
| 2026-11-13 | RF-12 (+ REQ-001…REQ-010, RN-01…RN-10) | Creación del plan a partir del SPECS vigente (`dbe909f`) y de las tareas sugeridas | TASK-001 … TASK-006 (nuevas) |

## 7. Supuestos

- La verificación es manual según PA-06 (`SPECS.md` §8); no se añaden pruebas automatizadas ni dependencias.
- No se modifican entidades, esquema, relaciones JPA, `pom.xml`, `application.properties`, `src/main/frontend/generated/` ni triggers/restricciones en BD.
- La regla no es retroactiva (RN-03, REQ-007).
- El mensaje de rechazo es el exacto de RN-06 y, si falta la entidad, «No encontrado» (RN-10).

## 8. Preguntas abiertas

- Ninguna. El breakdown quedó completo: RF-12 queda cubierto por TASK-001 … TASK-006 sin preguntas bloqueantes.

## 9. Historial de cambios

| Fecha | Cambio |
|-------|--------|
| 2026-11-13 | Creación del plan RF-12 desde `SPECS.md` (`dbe909f`): 6 tareas TASK-001 … TASK-006 generadas desde las tareas sugeridas; todas en estado `[ ]` (pendiente). |
