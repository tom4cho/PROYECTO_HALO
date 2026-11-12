# SPECS — Especificaciones EARS

> Fuente: `docs/08_REQUERIMIENTOS.md` (RF-05, RF-10, RF-11) + nueva regla de negocio de consistencia de rama.
> Fecha: 2026-11-12
> Autor: specs-development
> Enriquecido por: specs-enricher (tecnologías, fetch de relaciones y contrato de validación verificados contra el código).

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
- Revalidación o bloqueo de asignaciones existentes cuando cambia la rama de un `Recluta` o la división de una `DivisionEspecial` (PA-05).
- Cambios en `src/main/frontend/generated/` (código generado por Vaadin).
- Añadir dependencias nuevas al `pom.xml`.

**Cadena de verificación de la regla**

```
AptoDivisionEspecial.idRecluta.idRama.idRama
  == AptoDivisionEspecial.idDivisionEspecial.idDivision.idRama.idRama
```

## 2. Tecnologías y decisiones técnicas

### 2.1 Stack implicado (verificado en el repositorio)

| Área | Tecnología y versión | Uso concreto en RF-12 | Evidencia |
| :--- | :--- | :--- | :--- |
| Interfaz de usuario | Vaadin Flow 25.3.0, tema Aura, `@Push` | Pestaña «Aptos División Especial»: `ComboBox`, `FormLayout`, `Grid`, `Button` y `Notification.show(...)`; recarga de combos con `ListDataProvider` | `pom.xml` (`vaadin.version=25.3.0`), `Application`, `MainView` |
| Backend | Java 25 + Spring Boot 4.1.1 | `@Service`, `@Transactional` (propagación `REQUIRED` por defecto), `@PersistenceContext EntityManager`; Tomcat embebido en `${PORT:8080}` | `pom.xml`, `CrudService`, `application.properties` |
| Capa de servicio | `com.example.service.CrudService` genérico sobre `EntityManager` | Único punto de persistencia del proyecto; se añaden métodos específicos transaccionales para `AptoDivisionEspecial` | `CrudService` |
| Persistencia / ORM | Jakarta Persistence + Hibernate ORM (versión gestionada por `spring-boot-starter-data-jpa`; sin versión explícita en `pom.xml`) | `EntityManager.find` y JPQL/HQL con `JOIN FETCH`; sin interfaces de repositorio ni Spring Data | `pom.xml`, `CrudService` |
| Base de datos | PostgreSQL 16 o superior (proveedor Neon) | Tablas `aptos_division_especial`, `reclutas`, `divisiones_especiales`, `divisiones`, `ramas`; sin triggers ni restricciones de negocio | `application.properties`, `docs/08` |
| Pool y driver | HikariCP + `org.postgresql:postgresql` (`runtime`) | Conexión JDBC a Neon | `pom.xml`, `application.properties` |
| Configuración | `application.properties` + variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT` | `ddl-auto=update`, `PostgreSQLDialect`; sin Flyway ni Liquibase (LT-02) | `application.properties` |
| Construcción | Apache Maven 3.9.12 (wrapper) + `spring-boot-maven-plugin` + `vaadin-maven-plugin` (`build-frontend`) | Compilación y arranque; `src/main/frontend/generated/` es generado y no se edita | `pom.xml`, `AGENTS.md` |
| Pruebas | JUnit 5 + Mockito (`spring-boot-starter-test`) y `browserless-test-spring` | Stack disponible; actualmente no existen fuentes de prueba (`src/test` vacío) | `pom.xml` |
| Control de versiones | Git | Entrega en rama principal mediante `git-versioning` | repositorio |

No se introduce ninguna tecnología nueva: la regla se implementa con el stack ya declarado (Vaadin Flow, Spring Boot, JPA/Hibernate, PostgreSQL, Maven).

### 2.2 Fetch de las relaciones implicadas

Ninguna entidad declara el atributo `fetch`, por lo que se aplican los valores por defecto de JPA: `@ManyToOne` y `@OneToOne` son **`EAGER`**.

| Relación | Anotación | Fetch efectivo |
| :--- | :--- | :--- |
| `AptoDivisionEspecial.idRecluta` | `@OneToOne(optional = false)` + `unique = true` | `EAGER` |
| `Recluta.idRama` | `@ManyToOne(optional = false)` | `EAGER` |
| `AptoDivisionEspecial.idDivisionEspecial` | `@ManyToOne(optional = false)` | `EAGER` |
| `DivisionEspecial.idDivision` | `@ManyToOne(optional = false)` | `EAGER` |
| `Division.idRama` | `@ManyToOne(optional = false)` | `EAGER` |
| `Recluta.idApto` | `@OneToOne(mappedBy = "idRecluta", optional = true)` | `EAGER` (lado inverso, solo lectura) |

Consecuencias para esta especificación:

- La navegación `recluta.getIdRama().getIdRama()` y `divisionEspecial.getIdDivision().getIdRama().getIdRama()` funciona hoy sobre entidades *detached* devueltas por `listarTodos`/`buscarPorId`, porque las asociaciones se inicializan al cargarlas. **No es el riesgo principal.**
- El riesgo real es la **obsolescencia**: los objetos de los `ComboBox` se cargaron al abrir o refrescar la pestaña y pueden reflejar una rama distinta de la que hay en base de datos en el momento de guardar.
- Por eso se mantiene la exigencia de revalidar **recargando por ID dentro de la transacción**; además, así la regla no depende del comportamiento por defecto de `EAGER` ni de que alguien añada `fetch = LAZY` en el futuro.

### 2.3 Contrato técnico de la validación (decisión técnica revisable)

Se añaden dos métodos específicos en `CrudService` (sin tocar la firma ni el comportamiento de `guardar`/`actualizar` genéricos):

```java
@Transactional
public AptoDivisionEspecial crearApto(Integer idRecluta, Integer idDivisionEspecial,
        Boolean aptoServicio, LocalDate fechaEvaluacion, Integer puntaje);

@Transactional
public AptoDivisionEspecial actualizarApto(Integer idApto, Integer idRecluta,
        Integer idDivisionEspecial, Boolean aptoServicio, LocalDate fechaEvaluacion, Integer puntaje);
```

Reglas del contrato:

1. Los parámetros de selección se reciben como **`Integer` (IDs)**, no como entidades *detached*: el servicio recarga `Recluta` y `DivisionEspecial` con `EntityManager.find` dentro de la transacción.
2. La validación de rama se ejecuta **antes** de `persist`/`merge`, comparando `Objects.equals(ramaRecluta.getIdRama(), ramaDivision.getIdRama())`.
3. Si la regla no se cumple, se lanza `ReglaRamaException extends RuntimeException` (paquete `com.example.service`) **antes** de escribir. La excepción no se captura dentro del método transaccional; se propaga y la captura la vista.
4. Si `find` devuelve `null` para el recluta o la división especial, se lanza `ReglaRamaException` con el texto existente «No encontrado».
5. La vista (`MainView`) captura `ReglaRamaException` en `btnCrear` y `btnActualizar`, muestra `Notification.show(e.getMessage())` y retorna sin refrescar la grilla ni el campo ID, conservando los valores del formulario.
6. La llamada interna a `guardar`/`actualizar` se une a la transacción ya abierta (propagación `REQUIRED`); no se usan `REQUIRES_NEW` ni transacciones anidadas.

Justificación: `CrudService.guardar` usa `persist` (solo inserta) y `actualizar` usa `merge`; mantener la validación en métodos específicos evita alterar el comportamiento genérico del que dependen las otras cuatro pestañas.

### 2.4 Build y verificación

- El wrapper `.\mvnw.cmd` falla en esta ruta porque contiene un espacio (`C:\Users\Carmona Duque\...`). Usar el Maven cacheado:

```powershell
& "$HOME\.m2\wrapper\dists\apache-maven-3.9.12\b7b3f974a57b41e8569153aba8dd0414\bin\mvn.cmd" -q -DskipTests compile
& "$HOME\.m2\wrapper\dists\apache-maven-3.9.12\b7b3f974a57b41e8569153aba8dd0414\bin\mvn.cmd" spring-boot:run
& "$HOME\.m2\wrapper\dists\apache-maven-3.9.12\b7b3f974a57b41e8569153aba8dd0414\bin\mvn.cmd" clean package -DskipTests
```

- No hay fuentes de prueba, por lo que `mvn test` no ejecuta nada. La verificación de RF-12 es manual contra la sección 9 (criterios de éxito). Si se acuerdan pruebas automatizadas, se harán con las dependencias ya presentes (JUnit 5 + Mockito), sin ampliar el `pom.xml`.

## 3. Actores y glosario

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
| `ReglaRamaException` | Excepción no comprobada que señala el rechazo por incoherencia de rama (o por referencia inexistente). |

Convención de redacción: palabras clave EARS en inglés (`When`, `While`, `If ... then`, `shall`) y términos de dominio en español, alineados con `docs/08_REQUERIMIENTOS.md`.

## 4. Requisitos EARS

### 4.1 Consistencia de rama en `AptoDivisionEspecial` (RF-12)

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
| REQ-009 | Unwanted-behavior | If the referenced `Recluta` or `DivisionEspecial` does not exist in the database when the service reloads it, then the UNSC system shall reject the operation without writing and shall display the notification «No encontrado». | ID de recluta o división especial eliminado entre la carga del combo y el guardado → sin escritura y notificación «No encontrado». | RF-11, RN-10 |
| REQ-010 | Ubiquitous | The UNSC system shall apply the `Rama` consistency rule regardless of the values of `aptoServicio`, `fechaEvaluacion` and `puntaje`. | Alta y actualización con `aptoServicio` verdadero y falso se validan igual. | RF-12, RN-09 |

### 4.2 Requerimiento funcional asignado

**RF-12 — Consistencia de rama en asignaciones a divisiones especiales.** El sistema debe impedir que se cree o actualice un registro de `AptoDivisionEspecial` cuando el recluta seleccionado no pertenece a la misma rama que la división a la que pertenece la división especial seleccionada, y debe informar el motivo mediante una notificación en español. La regla no se aplica retroactivamente a los registros existentes.

Relación con los requisitos vigentes:

- **Extiende RF-10** (integridad y validación básica): añade una validación de integridad referencial de negocio que RF-10 no cubre.
- **Complementa RF-05** (gestión de asignaciones): restringe qué combinaciones recluta/división especial son admisibles.
- **Usa RF-11** (retroalimentación al usuario) para el canal de notificación.

## 5. Reglas de negocio

| ID | Regla |
|----|-------|
| RN-01 | En un `AptoDivisionEspecial`, la rama del recluta debe ser igual a la rama de la división de la división especial: `idRecluta.idRama.idRama == idDivisionEspecial.idDivision.idRama.idRama`. |
| RN-02 | La regla se evalúa tanto en el alta como en la actualización del `AptoDivisionEspecial`. |
| RN-03 | La regla no es retroactiva: no se migran ni corrigen registros existentes; solo se valida lo que se cree o actualice después de su implementación. |
| RN-04 | La validación de campos obligatorios (recluta y división especial seleccionados) tiene precedencia sobre la validación de consistencia de rama. |
| RN-05 | La igualdad de ramas se determina comparando los `idRama` de tipo `Integer`, de forma null-safe; no se compara por referencia ni por `equals` de entidad. Si cualquiera de las dos ramas es `null`, la comparación se considera no coincidente y la operación se rechaza. |
| RN-06 | El mensaje de rechazo es exactamente: «El recluta debe pertenecer a la misma rama que la división de la división especial seleccionada.». |
| RN-07 | La validación y la persistencia deben ejecutarse en la misma transacción; si la validación falla, no se inserta ni se actualiza ninguna fila. |
| RN-08 | La regla no altera el esquema, las entidades, las relaciones JPA ni el comportamiento de las demás pestañas. |
| RN-09 | La regla se aplica con independencia de los valores de `aptoServicio`, `fechaEvaluacion` y `puntaje`. |
| RN-10 | Si el recluta o la división especial referenciados no existen al recargarse en el servicio, la operación se rechaza sin escribir y se informa con la notificación «No encontrado». |

## 6. Consideraciones de implementación

1. **Dónde validar (servicio + UI).** La UI es el único cliente actual, pero la regla debe aplicarse en la capa de servicio para que cualquier ruta de persistencia quede cubierta. `CrudService.guardar` y `CrudService.actualizar` son genéricos; se añaden métodos específicos y transaccionales para `AptoDivisionEspecial` (contrato en 2.3) en lugar de modificar el comportamiento genérico de `guardar` para todas las entidades. La UI sigue validando campos obligatorios y muestra la notificación devuelta por el servicio.
2. **Recarga y fetch.** Las relaciones se cargan con `EAGER` por defecto (2.2), por lo que navegar los objetos *detached* de los `ComboBox` no lanza `LazyInitializationException` hoy. Aun así, la validación debe ejecutarse dentro del método `@Transactional` recargando las entidades por ID con `EntityManager.find`, para evitar validar contra datos obsoletos y para no depender de los valores por defecto de fetch.
3. **Atomicidad y excepción.** El método que valida y persiste debe ser único y transaccional. Si la validación falla, debe lanzar `ReglaRamaException` **antes** de llamar a `persist`/`merge`; no debe capturarse la excepción dentro del método transaccional dejándola marcada para *rollback* y continuar con la escritura. La captura corresponde a la vista.
4. **IDs `Integer`.** Todos los identificadores son `Integer` con `GenerationType.IDENTITY`. La comparación debe ser null-safe, por ejemplo `Objects.equals(ramaRecluta.getIdRama(), ramaDivision.getIdRama())`. No usar `==` ni comparar entidades completas. `buscarPorId` y `eliminar` reciben `Serializable`, no `Long`; mantener esa firma.
5. **Alta y actualización.** En el alta, el servicio recarga el recluta y la división especial, valida y persiste una nueva entidad con `persist`. En la actualización, recarga el `AptoDivisionEspecial` por `idApto`, revalida el par recluta/división especial y modifica los campos antes de `merge`. No se debe usar `guardar` (`persist`) con un ID ya existente: lanzaría `EntityExistsException`.
6. **Notificación en español.** Usar `Notification.show(...)` con el texto exacto de RN-06. En caso de rechazo no se debe refrescar la grilla, no se debe actualizar el campo ID ni se debe mostrar la notificación de éxito; conviene conservar los valores del formulario para que el usuario corrija la selección. Sin `Bean Validation`: la validación es manual, como el resto del proyecto.
7. **Registros históricos.** Los registros existentes que no cumplan la regla no se corrigen ni se bloquean al consultarse o eliminarse; solo se rechaza su actualización si el par recluta/división especial resultante sigue siendo incoherente. La pestaña «Consultas» puede seguir mostrándolos.
8. **Sin restricción en base de datos.** Al no existir triggers ni restricciones de comprobación (LT-02: sin migraciones versionadas), una escritura directa por SQL podría saltarse la regla. Se asume como limitación conocida, coherente con LT-06, y queda fuera del alcance.
9. **Sin cambios de modelo.** No se añaden colecciones `@OneToMany`, no se modifica el mapeo unidireccional existente ni se toca `src/main/frontend/generated/`.
10. **Restricción de unicidad existente.** `aptos_division_especial.id_recluta` es `unique`; un segundo alta para el mismo recluta falla con `DataIntegrityViolationException` a nivel de base de datos. Esa validación no forma parte de RF-12 y no se modifica.
11. **Sin dependencias nuevas.** No se añade ninguna librería; `spring-boot-starter-test` ya aporta JUnit 5 y Mockito si se acuerdan pruebas automatizadas.

## 7. Supuestos confirmados

Todos los supuestos de esta sección fueron confirmados por el usuario (opción (a) en la sección 8).

- `Recluta.idRama` y `DivisionEspecial.idDivision` son obligatorios por modelo (`optional = false`), por lo que ambas ramas están disponibles cuando el registro es válido.
- Las asociaciones `@ManyToOne`/`@OneToOne` se cargan con `EAGER` por defecto (no hay `fetch` explícito), de modo que la cadena de ramas es navegable desde entidades *detached*; la revalidación transaccional se exige por frescura de datos, no por `LazyInitializationException`.
- El mensaje genérico de RN-06 es suficiente; no se requiere listar los nombres de las ramas en conflicto (PA-01).
- La regla se revalida en toda actualización, incluso si el recluta y la división especial no cambian (PA-02).
- No se requiere validación a nivel de base de datos ni migración de datos existentes (PA-04).
- La pestaña «Consultas» no se modifica: los registros históricos incoherentes se muestran sin marcar ni ocultar (PA-03).
- No se aplica una regla equivalente al cambiar la rama de un `Recluta` o la división de una `DivisionEspecial` ya asignados (PA-05).
- La verificación es manual, contra los criterios de la sección 9; no se añaden pruebas automatizadas (PA-06).
- La etiqueta del `ComboBox` de recluta se mantiene como `nombre + apellido` (PA-07).
- La validación se ejecuta en el servidor; la validación en la UI es solo de campos obligatorios y de presentación del mensaje.

## 8. Decisiones resueltas

Todas las preguntas fueron planteadas al usuario y respondidas el 2026-11-12. En los siete casos se eligió la **opción (a)**, que coincide con el supuesto ya recogido en las secciones 4 a 7; por tanto **no se modifica ningún requisito EARS ni ninguna regla de negocio**.

| ID | Pregunta | Estado | Decisión acordada |
|----|----------|--------|-------------------|
| PA-01 | ¿Debe el mensaje de rechazo incluir los nombres de las ramas en conflicto? | ✅ Resuelta (opción a) | Mensaje genérico y exacto de RN-06; no se listan nombres de ramas. REQ-004 y RN-06 quedan sin cambios. |
| PA-02 | ¿Debe permitirse actualizar un registro histórico incoherente cuando solo se modifican `aptoServicio`, `fechaEvaluacion` o `puntaje`? | ✅ Resuelta (opción a) | No: toda actualización revalida el par recluta/división especial (RN-02, consideración 7). REQ-002 queda sin cambios. |
| PA-03 | ¿Debe la pestaña «Consultas» ocultar o marcar los registros históricos incoherentes? | ✅ Resuelta (opción a) | Sin cambios: los registros se muestran tal cual, sin marcar ni ocultar. |
| PA-04 | ¿Se requiere una restricción o trigger en PostgreSQL para bloquear escrituras fuera de la aplicación? | ✅ Resuelta (opción a) | No. LT-02 no contempla migraciones versionadas y RF-12 acota la validación a la aplicación (RN-08, consideración 8). |
| PA-05 | ¿Debe aplicarse una regla equivalente al cambiar la rama de un `Recluta` o la división de una `DivisionEspecial` ya asignados? | ✅ Resuelta (opción a) | Fuera de alcance de RF-12. Un cambio posterior de rama o de división puede dejar incoherente un registro existente; se documenta como limitación conocida (sección 1, «Fuera del alcance»). |
| PA-06 | ¿Se requieren pruebas automatizadas para esta entrega? | ✅ Resuelta (opción a) | No: verificación manual contra los criterios de la sección 9. El stack de pruebas (JUnit 5 + Mockito) queda disponible sin añadir dependencias. |
| PA-07 | ¿Debe el `ComboBox` de recluta mostrar la rama junto al nombre para facilitar la selección? | ✅ Resuelta (opción a) | Sin cambios de UX: la etiqueta sigue siendo `nombre + apellido`. |

**Estado de la especificación:** cerrada y lista para implementación con el contrato técnico de la sección 2.3 (`CrudService.crearApto` / `CrudService.actualizarApto`, `ReglaRamaException`, validación antes de `persist`/`merge`, dentro de `@Transactional` y con recarga por ID mediante `EntityManager.find`). No quedan preguntas abiertas.

## 9. Criterios de éxito

- [ ] Alta de `AptoDivisionEspecial` con recluta y división especial de la misma rama: el registro se persiste y aparece en la grilla.
- [ ] Alta con recluta y división especial de ramas distintas: no se crea fila y se muestra el mensaje de RN-06.
- [ ] Actualización que mantiene la coherencia de rama: los cambios se persisten.
- [ ] Actualización que introduce una incoherencia de rama: la fila conserva sus valores previos y se muestra el mensaje de RN-06.
- [ ] La validación se ejecuta en el servicio dentro de una transacción y recarga las entidades por ID, sin depender de los objetos *detached* de la UI.
- [ ] Los registros existentes incoherentes no se modifican ni se bloquean al consultarse.
- [ ] La comparación de ramas usa `idRama` de tipo `Integer` de forma null-safe.
- [ ] Alta o actualización con recluta o división especial inexistente en base de datos: rechazo con «No encontrado» y sin escritura (REQ-009).
- [ ] La regla se aplica igual con `aptoServicio` verdadero y falso (REQ-010).
- [ ] No se modifican entidades, esquema, relaciones JPA ni código generado por Vaadin.

> Verificación manual acordada (PA-06): esta lista es el guion de comprobación en la aplicación en ejecución. No se añaden pruebas automatizadas ni dependencias.

## 10. Historial de cambios

| Fecha | Cambio |
|-------|--------|
| 2026-11-12 | Creación de `SPECS.md` con RF-12, ocho requisitos EARS y reglas de negocio para la consistencia de rama en `AptoDivisionEspecial`. |
| 2026-11-12 | Enriquecimiento por `specs-enricher`: sección 2 de tecnologías verificadas, corrección del fetch real (`EAGER`, no `LAZY`), contrato técnico del servicio (`crearApto`/`actualizarApto` + `ReglaRamaException`), REQ-009 y REQ-010, RN-09 y RN-10, y estado de las preguntas abiertas. |
| 2026-11-12 | Cierre de la especificación: PA-01 a PA-07 resueltas con la opción (a); supuestos confirmados; sin cambios en requisitos EARS ni en reglas de negocio. Documento listo para implementación. |
