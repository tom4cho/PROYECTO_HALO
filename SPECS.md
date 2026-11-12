# SPECS — Especificaciones EARS

> Fuente: `docs/08_REQUERIMIENTOS.md` (RF-05, RF-10, RF-11) + nueva regla de negocio de consistencia de rama.
> Fecha: 2026-11-12
> Autor: specs-development

## 1. Objetivo y alcance

Definir la regla de negocio que exige que, en todo registro de `AptoDivisionEspecial`, el `Recluta` asignado pertenezca a la misma `Rama` que la `Division` a la que pertenece la `DivisionEspecial` seleccionada.

**Dentro del alcance**

- Validación en el alta y en la actualización de `AptoDivisionEspecial` (pestaña «Aptos División Especial»).
- Rechazo de la operación sin persistir cambios cuando la regla no se cumple.
- Notificación en español al usuario con el motivo del rechazo.
- Validación ejecutada en la capa de servicio, dentro de una transacción, para que no dependa solo de la UI.

**Fuera del alcance**

- Migración, corrección o marcado de registros históricos inconsistentes.
- Cambios de esquema, entidades, relaciones JPA o tablas.
- Restricciones o triggers a nivel de base de datos.
- Validación equivalente en las pestañas de Ramas, Divisiones, Divisiones Especiales, Reclutas o Consultas.
- Cambios en `src/main/frontend/generated/` (código generado por Vaadin).

**Cadena de verificación de la regla**

```
AptoDivisionEspecial.idRecluta.idRama.idRama
  == AptoDivisionEspecial.idDivisionEspecial.idDivision.idRama.idRama
```

## 2. Actores y glosario

| Término | Definición |
|---------|------------|
| Sistema UNSC | Aplicación Spring Boot + Vaadin Flow objeto de esta especificación. |
| Usuario / operador | Persona que opera la pestaña «Aptos División Especial». |
| `Rama` | Componente organizativo de la UNSC (`ramas`, PK `idRama` de tipo `Integer`). |
| `Division` | Unidad dependiente de una `Rama` (`divisiones`, FK `id_rama`). |
| `DivisionEspecial` | División especial dependiente de una `Division` (`divisiones_especiales`, FK `id_division`). |
| `Recluta` | Integrante de la UNSC asignado a una `Rama` (`reclutas`, FK `id_rama`). |
| `AptoDivisionEspecial` | Registro que vincula un `Recluta` con una `DivisionEspecial` (`aptos_division_especial`). |
| Rama del recluta | `AptoDivisionEspecial.idRecluta.idRama`. |
| Rama de la división especial | `AptoDivisionEspecial.idDivisionEspecial.idDivision.idRama`. |
| Coincidencia de rama | Igualdad de `idRama` entre la rama del recluta y la rama de la división especial. |

Convención de redacción: palabras clave EARS en inglés (`When`, `While`, `If ... then`, `shall`) y términos de dominio en español, alineados con `docs/08_REQUERIMIENTOS.md`.

## 3. Requisitos EARS

### 3.1 Consistencia de rama en `AptoDivisionEspecial` (RF-12)

| ID | Patrón | Requisito EARS | Verificación | Trazabilidad |
|----|--------|----------------|--------------|--------------|
| REQ-001 | Event-driven | When a user submits a new `AptoDivisionEspecial`, the UNSC system shall persist the record only if the selected `Recluta` belongs to the same `Rama` as the `Division` of the selected `DivisionEspecial`. | Alta con recluta y división especial de la misma rama → fila creada. Alta con ramas distintas → sin fila nueva. | RF-12, RN-01, RN-02 |
| REQ-002 | Event-driven | When a user submits changes to an existing `AptoDivisionEspecial`, the UNSC system shall persist the update only if the selected `Recluta` belongs to the same `Rama` as the `Division` of the selected `DivisionEspecial`. | Actualización que mantiene o cambia a un par coherente → fila actualizada. Actualización hacia un par incoherente → fila sin cambios. | RF-12, RN-01, RN-02 |
| REQ-003 | Unwanted-behavior | If the selected `Recluta` belongs to a different `Rama` than the `Division` of the selected `DivisionEspecial`, then the UNSC system shall reject the operation without modifying the `aptos_division_especial` table. | Tras el rechazo, la consulta por ID devuelve el registro previo intacto; en alta no existe fila nueva. | RF-12, RN-01, RN-07 |
| REQ-004 | Event-driven | When the UNSC system rejects an `AptoDivisionEspecial` operation because of a `Rama` mismatch, the UNSC system shall display the notification «El recluta debe pertenecer a la misma rama que la división de la división especial seleccionada.» | La UI muestra exactamente ese texto y no muestra la notificación de éxito. | RF-12, RF-11, RN-06 |
| REQ-005 | Ubiquitous | The UNSC system shall compare the `Rama` of the selected `Recluta` with the `Rama` of the `Division` of the selected `DivisionEspecial` by their `idRama` values. | Dos entidades `Rama` con el mismo `idRama` se consideran iguales; distinto `idRama` se considera distinto. | RF-12, RN-05 |
| REQ-006 | Ubiquitous | The UNSC system shall evaluate the `Rama` consistency rule and persist the `AptoDivisionEspecial` within the same database transaction. | Un fallo de validación no deja escritura parcial; la validación y la escritura comparten transacción. | RF-12, RN-07 |
| REQ-007 | Ubiquitous | The UNSC system shall not migrate or modify existing `AptoDivisionEspecial` records when the `Rama` consistency rule is deployed. | Los registros previos conservan sus valores y siguen siendo consultables. | RF-12, RN-03 |
| REQ-008 | State-driven | While the selected `Recluta` or the selected `DivisionEspecial` is missing, the UNSC system shall display the existing required-field notification instead of the `Rama` mismatch notification. | Alta o actualización sin recluta o sin división especial → mensaje de campo obligatorio ya existente; no se evalúa la regla de rama. | RF-12, RF-10, RN-04 |

### 3.2 Requerimiento funcional asignado

**RF-12 — Consistencia de rama en asignaciones a divisiones especiales.** El sistema debe impedir que se cree o actualice un registro de `AptoDivisionEspecial` cuando el recluta seleccionado no pertenece a la misma rama que la división a la que pertenece la división especial seleccionada, y debe informar el motivo mediante una notificación en español. La regla no se aplica retroactivamente a los registros existentes.

Relación con los requisitos vigentes:

- **Extiende RF-10** (integridad y validación básica): añade una validación de integridad referencial de negocio que RF-10 no cubre.
- **Complementa RF-05** (gestión de asignaciones): restringe qué combinaciones recluta/división especial son admisibles.
- **Usa RF-11** (retroalimentación al usuario) para el canal de notificación.

## 4. Reglas de negocio

| ID | Regla |
|----|-------|
| RN-01 | En un `AptoDivisionEspecial`, la rama del recluta debe ser igual a la rama de la división de la división especial: `idRecluta.idRama.idRama == idDivisionEspecial.idDivision.idRama.idRama`. |
| RN-02 | La regla se evalúa tanto en el alta como en la actualización del `AptoDivisionEspecial`. |
| RN-03 | La regla no es retroactiva: no se migran ni corrigen registros existentes; solo se valida lo que se cree o actualice después de su implementación. |
| RN-04 | La validación de campos obligatorios (recluta y división especial seleccionados) tiene precedencia sobre la validación de consistencia de rama. |
| RN-05 | La igualdad de ramas se determina comparando los `idRama` de tipo `Integer`, de forma null-safe; no se compara por referencia ni por `equals` de entidad. |
| RN-06 | El mensaje de rechazo es exactamente: «El recluta debe pertenecer a la misma rama que la división de la división especial seleccionada.». |
| RN-07 | La validación y la persistencia deben ejecutarse en la misma transacción; si la validación falla, no se inserta ni se actualiza ninguna fila. |
| RN-08 | La regla no altera el esquema, las entidades, las relaciones JPA ni el comportamiento de las demás pestañas. |

## 5. Consideraciones de implementación

1. **Dónde validar (servicio + UI).** La UI es el único cliente actual, pero la regla debe aplicarse en la capa de servicio para que cualquier ruta de persistencia quede cubierta. `CrudService.guardar` y `CrudService.actualizar` son genéricos; se recomienda añadir un método específico y transaccional para `AptoDivisionEspecial` (por ejemplo, validar y persistir en la misma llamada) en lugar de modificar el comportamiento genérico de `guardar` para todas las entidades. La UI puede seguir validando campos obligatorios y mostrar la notificación devuelta por el servicio.
2. **Transacción y `LazyInitializationException`.** Las relaciones `@ManyToOne` son `LAZY` y `CrudService` cierra la transacción tras cada llamada, por lo que los objetos de los `ComboBox` llegan a la UI como entidades *detached*; navegar `recluta.getIdRama()` o `divisionEspecial.getIdDivision().getIdRama()` fuera de una transacción puede lanzar `LazyInitializationException`. La validación debe ejecutarse dentro de un método `@Transactional`, recargando las entidades por ID con `EntityManager.find` (o con una consulta `JOIN FETCH`) y navegando las relaciones dentro de esa transacción. No se debe confiar en los proxies de los objetos *detached* que provienen de la UI.
3. **Atomicidad.** El método que valida y persiste debe ser único y transaccional. Si la validación falla, debe lanzar una excepción de negocio o devolver un resultado de rechazo **antes** de llamar a `persist`/`merge`; no debe capturarse la excepción dentro de la transacción dejándola marcada para *rollback* y continuar con la escritura.
4. **IDs `Integer`.** Todos los identificadores son `Integer` con `GenerationType.IDENTITY`. La comparación debe ser null-safe, por ejemplo `Objects.equals(ramaRecluta.getIdRama(), ramaDivision.getIdRama())`. No usar `==` ni comparar entidades completas.
5. **Alta y actualización.** En el alta, la entidad se construye con los valores de los `ComboBox` y se persiste con `CrudService.guardar` (`persist`). En la actualización se recupera el registro y se modifican sus campos antes de `CrudService.actualizar` (`merge`). En ambos casos la validación debe ejecutarse antes de la escritura y revalidarse en cada actualización, aunque solo cambien `aptoServicio`, `fechaEvaluacion` o `puntaje`.
6. **Notificación en español.** Usar `Notification.show(...)` con el texto exacto de RN-06. En caso de rechazo no se debe refrescar la grilla, no se debe actualizar el campo ID ni se debe mostrar la notificación de éxito; conviene conservar los valores del formulario para que el usuario corrija la selección. Sin `Bean Validation`: la validación es manual, como el resto del proyecto.
7. **Registros históricos.** Los registros existentes que no cumplan la regla no se corrigen ni se bloquean al consultarse o eliminarse; solo se rechaza su actualización si el par recluta/división especial resultante sigue siendo incoherente. La pestaña «Consultas» puede seguir mostrándolos.
8. **Sin restricción en base de datos.** Al no existir triggers ni restricciones de comprobación, una escritura directa por SQL podría saltarse la regla. Esto se asume como limitación conocida (coherente con LT-06) y queda fuera del alcance.
9. **Sin cambios de modelo.** No se añaden colecciones `@OneToMany`, no se modifica el mapeo unidireccional existente ni se toca `src/main/frontend/generated/`.

## 6. Supuestos

- `Recluta.idRama` y `DivisionEspecial.idDivision` son obligatorios por modelo (`optional = false`), por lo que ambas ramas están disponibles cuando el registro es válido.
- El mensaje genérico de RN-06 es suficiente; no se requiere listar los nombres de las ramas en conflicto.
- La regla se revalida en toda actualización, incluso si el recluta y la división especial no cambian.
- No se requiere validación a nivel de base de datos ni migración de datos existentes.
- La validación se ejecuta en el servidor; la validación en la UI es solo de campos obligatorios y de presentación del mensaje.

## 7. Preguntas abiertas

- [ ] ¿Debe el mensaje de rechazo incluir los nombres de las ramas en conflicto? Supuesto actual: no, mensaje genérico de RN-06.
- [ ] ¿Debe permitirse actualizar un registro histórico incoherente cuando solo se modifican `aptoServicio`, `fechaEvaluacion` o `puntaje`? Supuesto actual: no, toda actualización revalida el par recluta/división especial.
- [ ] ¿Debe la pestaña «Consultas» ocultar o marcar los registros históricos incoherentes? Supuesto actual: no, se muestran sin cambios.
- [ ] ¿Se requiere una restricción o trigger en PostgreSQL para bloquear escrituras fuera de la aplicación? Supuesto actual: no, validación solo en la aplicación.
- [ ] ¿Debe aplicarse una regla equivalente al cambiar la rama de un `Recluta` o la división de una `DivisionEspecial` ya asignados? Supuesto actual: fuera de alcance de RF-12.

## 8. Criterios de éxito

- [ ] Alta de `AptoDivisionEspecial` con recluta y división especial de la misma rama: el registro se persiste y aparece en la grilla.
- [ ] Alta con recluta y división especial de ramas distintas: no se crea fila y se muestra el mensaje de RN-06.
- [ ] Actualización que mantiene la coherencia de rama: los cambios se persisten.
- [ ] Actualización que introduce una incoherencia de rama: la fila conserva sus valores previos y se muestra el mensaje de RN-06.
- [ ] La validación se ejecuta en el servicio dentro de una transacción, sin `LazyInitializationException`.
- [ ] Los registros existentes incoherentes no se modifican ni se bloquean al consultarse.
- [ ] La comparación de ramas usa `idRama` de tipo `Integer` de forma null-safe.
- [ ] No se modifican entidades, esquema, relaciones JPA ni código generado por Vaadin.

## 9. Historial de cambios

| Fecha | Cambio |
|-------|--------|
| 2026-11-12 | Creación de `SPECS.md` con RF-12, ocho requisitos EARS y reglas de negocio para la consistencia de rama en `AptoDivisionEspecial`. |
