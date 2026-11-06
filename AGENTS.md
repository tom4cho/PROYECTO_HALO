# AGENTS.md

## Project

Single-module Spring Boot + Vaadin Flow app implementing a UNSC CRUD (Spanish UI and domain names). No repositories, controllers, or services beyond `CrudService`, and no test sources.

- Java 25, Spring Boot 4.1.1, Vaadin 25.3.0 (Aura theme), Jakarta Persistence/Hibernate, PostgreSQL (Neon).
- Entrypoint: `com.example.Application` (`@SpringBootApplication`, `@Push`).
- Only UI route: `com.example.MainView` (`@Route("")`) with a `TabSheet` of five CRUD tabs plus a `Consultas` tab.
- `src/main/frontend/generated/` is Vaadin-generated and gitignored; never edit it.

## Commands

The Maven wrapper fails in this checkout because the absolute path contains a space (`C:\Users\Carmona Duque\...`): `.\mvnw.cmd` resolves it as `"C:\Users\Carmona"`. Use the cached Maven executable directly:

```powershell
& "$HOME\.m2\wrapper\dists\apache-maven-3.9.12\b7b3f974a57b41e8569153aba8dd0414\bin\mvn.cmd" -q -DskipTests compile
& "$HOME\.m2\wrapper\dists\apache-maven-3.9.12\b7b3f974a57b41e8569153aba8dd0414\bin\mvn.cmd" spring-boot:run
& "$HOME\.m2\wrapper\dists\apache-maven-3.9.12\b7b3f974a57b41e8569153aba8dd0414\bin\mvn.cmd" clean package -DskipTests
```

There are no tests, so `mvn test` has nothing to run despite the test dependencies in `pom.xml`.

## Model and persistence conventions

- Exactly five entities in `com.example.model`: `Rama`, `Division`, `DivisionEspecial`, `Recluta`, `AptoDivisionEspecial`.
- Tables are plural snake_case: `ramas`, `divisiones`, `divisiones_especiales`, `reclutas`, `aptos_division_especial`.
- All IDs are `Integer` with `GenerationType.IDENTITY`. `CrudService.buscarPorId` and `eliminar` take `Serializable` (not `Long`) to support them; keep that signature.
- Relations are deliberately unidirectional `@ManyToOne` plus one inverse `@OneToOne`; do not add `@OneToMany` collections:
  - `Division.idRama`, `Recluta.idRama` → `ramas` via `id_rama`
  - `DivisionEspecial.idDivision` → `divisiones` via `id_division`
  - `AptoDivisionEspecial.idRecluta` owns the optional 1:1 (`unique = true`, `id_recluta`)
  - `Recluta.idApto` is `mappedBy = "idRecluta"` and is read-only in the UI; assignments are created/edited from the Aptos tab
  - `AptoDivisionEspecial.idDivisionEspecial` → `divisiones_especiales` via `id_division_especial`
- Types: `Recluta.peso` is `BigDecimal` (`DECIMAL(6,3)`); `altura`/`puntaje` are `Integer`; `fechaEvaluacion` is `LocalDate`.
- All persistence goes through `com.example.service.CrudService` (class-level `@Transactional`, generic `EntityManager`). Reuse it for new entities instead of introducing Spring Data repositories.
- `CrudService` exposes adscripción queries: `listarReclutasPorDivision`, `listarReclutasAptosPorDivision`, `listarReclutasPorDivisionEspecial`, and `listarAptosPorDivision`. The `Consultas` tab consumes all four.
- `CrudService.guardar` uses `EntityManager.persist` (inserts only); use `actualizar` (`merge`) when modifying an existing row. There is no cascade configured, so deleting a parent still referenced by children currently surfaces a database FK error rather than a friendly validation message.
- UI labels and notifications are Spanish. Keep the existing per-tab pattern: form, grid with row selection, and buttons `Crear`, `Consultar`, `Actualizar`, `Eliminar`, `Limpiar`. Combo boxes use a mutable `ListDataProvider` refreshed on tab change.

## Runtime and data gotchas

- `src/main/resources/application.properties` connects to Neon and includes committed fallback credentials; `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables override them. Do not copy those values into logs or commits.
- `spring.jpa.hibernate.ddl-auto=update` creates/updates the five tables at startup; there is no migration tool. `vaadin.launch-browser=true` opens a browser, and the port is `${PORT:8080}`.
- The Neon database may still contain legacy inventory tables and `public.recluta` (singular, old `bigint` schema). The app does not use them; do not confuse `recluta` with `reclutas`.
- `docs/00`–`07` describe the old inventory app and old `Long` IDs. Only `docs/08_REQUERIMIENTOS.md` is current.
- `docs/08` is target behavior, not a description of current code: deletion guards (RF-10) and name-based searches are not implemented. CRUD searches use ID only; the `Consultas` tab provides the division/división especial queries.
- Package/build artifacts under `target/` are gitignored.
